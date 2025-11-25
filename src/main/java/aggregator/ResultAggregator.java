package aggregator;

import common.ControlMessage;
import common.ResultMessage;

import java.util.*;
import java.util.stream.Collectors;

public class ResultAggregator {
    private final Map<String, JobData> jobs = new HashMap<>();
    private static final int MAX_SENTENCES = 10000;

    private final int workerCount;

    public ResultAggregator(int workerCount) {
        this.workerCount = workerCount;
    }

    public ResultAggregator() {
        this(-1);
    }

    private static class JobData {
        long totalWordCount = 0;
        Map<String, Integer> globalWordFreq = new HashMap<>();
        long totalPos = 0;
        long totalNeg = 0;
        long totalTokens = 0;

        List<String> allSentences = new ArrayList<>();
        StringBuilder replacedTextBuilder = new StringBuilder();
        Set<Integer> receivedSectionIds = new HashSet<>();

        int expectedSections = -1;
        boolean endReceived = false;

        String corpusPath;
        long corpusSizeBytes;
        long startTimeMillis;
        long endTimeMillis;
    }

    public void addResult(ResultMessage result) {
        JobData job = jobs.computeIfAbsent(result.getJobId(), k -> new JobData());
        int sectionId = result.getSectionId();
        if (job.receivedSectionIds.contains(sectionId)) {
            return;
        }

        job.totalWordCount += result.getWordCount();

        if (result.getTopN() != null) {
            result.getTopN().forEach(
                    (word, count) -> job.globalWordFreq.merge(word, count, Integer::sum)
            );
        }

        job.totalPos += result.getSentimentPos();
        job.totalNeg += result.getSentimentNeg();
        job.totalTokens += result.getWordCount();

        if (result.getReplacedText() != null) {
            job.replacedTextBuilder
                    .append(result.getReplacedText())
                    .append("\n\n");
        }

        if (result.getSortedSentences() != null) {
            job.allSentences.addAll(result.getSortedSentences());

            if (job.allSentences.size() > MAX_SENTENCES) {
                job.allSentences = job.allSentences.subList(0, MAX_SENTENCES);
            }
        }

        job.receivedSectionIds.add(sectionId);
    }

    public void onEnd(ControlMessage end) {
        String jobId = end.getJobId();
        JobData job = jobs.computeIfAbsent(jobId, k -> new JobData());

        job.expectedSections = end.getTotalSections();
        job.endReceived = true;

        job.corpusPath = end.getCorpusPath();
        job.corpusSizeBytes = end.getCorpusSizeBytes();
        job.startTimeMillis = end.getStartTimeMillis();

        System.out.println("Джоба " + jobId + " закончила работу, totalSections=: " + job.expectedSections);
    }

    public boolean isJobComplete(String jobId) {
        JobData job = jobs.get(jobId);
        if (job == null)
            return false;
        if (!job.endReceived)
            return false;
        if (job.expectedSections < 0)
            return false;

        return job.receivedSectionIds.size() == job.expectedSections;
    }

    public ResultMessage getFinalResult(String jobId) {
        JobData job = jobs.get(jobId);
        if (job == null)
            return null;

        job.endTimeMillis = System.currentTimeMillis();
        long duration = (job.startTimeMillis > 0) ? job.endTimeMillis - job.startTimeMillis : -1L;

        ResultMessage finalResult = new ResultMessage();
        finalResult.setJobId(jobId);
        finalResult.setWordCount(job.totalWordCount);

        Map<String, Integer> topN = job.globalWordFreq.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(10)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new));
        finalResult.setTopN(topN);

        finalResult.setWordFreq(null);

        double score = job.totalTokens > 0
                ? (double) (job.totalPos - job.totalNeg) / job.totalTokens
                : 0.0;
        finalResult.setSentimentScore(score);
        finalResult.setSentimentPos((int) job.totalPos);
        finalResult.setSentimentNeg((int) job.totalNeg);

        finalResult.setReplacedText(
                job.replacedTextBuilder.length() > 0
                        ? job.replacedTextBuilder.toString()
                        : null
        );
//        finalResult.setReplacedText(null);

        job.allSentences.sort(Comparator.comparingInt(String::length));
        finalResult.setSortedSentences(job.allSentences);

        finalResult.setProcessingTimeMillis(duration);
        finalResult.setCorpusName(job.corpusPath);
        finalResult.setCorpusSizeBytes(job.corpusSizeBytes);
        finalResult.setWorkerCount(workerCount);
        finalResult.setTotalSections(job.expectedSections);

        return finalResult;
    }

    public void removeJob(String jobId) {
        jobs.remove(jobId);
    }

    public int getReceivedCount(String jobId) {
        JobData job = jobs.get(jobId);
        return job != null ? job.receivedSectionIds.size() : 0;
    }

    public int getExpectedCount(String jobId) {
        JobData job = jobs.get(jobId);
        return job != null ? job.expectedSections : -1;
    }
}
