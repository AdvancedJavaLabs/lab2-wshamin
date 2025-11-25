package aggregator;

import common.ResultMessage;

import java.util.*;
import java.util.stream.Collectors;

public class ResultAggregator {
    private final Map<String, JobData> jobs = new HashMap<>();

    private static class JobData {
        long totalWordCount = 0;
        Map<String, Integer> globalWordFreq = new HashMap<>();
        long totalPos = 0;
        long totalNeg = 0;
        long totalTokens = 0;

        Map<Integer, String> replacedSections = new TreeMap<>();
        List<String> allSentences = new ArrayList<>();

        Set<Integer> receivedSectionIds = new HashSet<>();

        int expectedSections = -1;
        boolean endReceived = false;
    }

    public void addResult(ResultMessage result) {
        JobData job = jobs.computeIfAbsent(result.getJobId(), k -> new JobData());

        int sectionId = result.getSectionId();

        if (job.receivedSectionIds.contains(sectionId)) {
            return;
        }

        job.totalWordCount += result.getWordCount();

        if (result.getWordFreq() != null) {
            result.getWordFreq().forEach(
                    (word, count) -> job.globalWordFreq.merge(word, count, Integer::sum));
        }

        job.totalPos += result.getSentimentPos();
        job.totalNeg += result.getSentimentNeg();
        job.totalTokens += result.getWordCount();

        if (result.getReplacedText() != null) {
            job.replacedSections.put(sectionId, result.getReplacedText());
        }

        if (result.getSortedSentences() != null) {
            job.allSentences.addAll(result.getSortedSentences());
        }

        job.receivedSectionIds.add(sectionId);
    }

    public void onEnd(String jobId, int totalSections) {
        JobData job = jobs.computeIfAbsent(jobId, k -> new JobData());
        job.expectedSections = totalSections;
        job.endReceived = true;
        System.out.println("Джоба " + jobId + " закончила работу, totalSections=: " + totalSections);
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

        finalResult.setReplacedText(null);

        job.allSentences.sort(Comparator.comparingInt(String::length));
        finalResult.setSortedSentences(job.allSentences);

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
