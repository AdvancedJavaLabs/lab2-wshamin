package common;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class ResultMessage implements Serializable {
    private String jobId;
    private int sectionId;
    private int totalSections;
    private long wordCount;
    private Map<String, Integer> wordFreq;
    private Map<String, Integer> topN;
    private double sentimentScore;
    private int sentimentPos;
    private int sentimentNeg;
    private String replacedText;
    private List<String> sortedSentences;

    private long processingTimeMillis;
    private String corpusName;
    private long corpusSizeBytes;
    private int workerCount;

    public ResultMessage() {
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public int getSectionId() {
        return sectionId;
    }

    public void setSectionId(int sectionId) {
        this.sectionId = sectionId;
    }

    public int getTotalSections() {
        return totalSections;
    }

    public void setTotalSections(int totalSections) {
        this.totalSections = totalSections;
    }

    public long getWordCount() {
        return wordCount;
    }

    public void setWordCount(long wordCount) {
        this.wordCount = wordCount;
    }

    public Map<String, Integer> getWordFreq() {
        return wordFreq;
    }

    public void setWordFreq(Map<String, Integer> wordFreq) {
        this.wordFreq = wordFreq;
    }

    public Map<String, Integer> getTopN() {
        return topN;
    }

    public void setTopN(Map<String, Integer> topN) {
        this.topN = topN;
    }

    public double getSentimentScore() {
        return sentimentScore;
    }

    public void setSentimentScore(double sentimentScore) {
        this.sentimentScore = sentimentScore;
    }

    public int getSentimentPos() {
        return sentimentPos;
    }

    public void setSentimentPos(int sentimentPos) {
        this.sentimentPos = sentimentPos;
    }

    public int getSentimentNeg() {
        return sentimentNeg;
    }

    public void setSentimentNeg(int sentimentNeg) {
        this.sentimentNeg = sentimentNeg;
    }

    public String getReplacedText() {
        return replacedText;
    }

    public void setReplacedText(String replacedText) {
        this.replacedText = replacedText;
    }

    public List<String> getSortedSentences() {
        return sortedSentences;
    }

    public void setSortedSentences(List<String> sortedSentences) {
        this.sortedSentences = sortedSentences;
    }

    public long getProcessingTimeMillis() {
        return processingTimeMillis;
    }

    public void setProcessingTimeMillis(long processingTimeMillis) {
        this.processingTimeMillis = processingTimeMillis;
    }

    public String getCorpusName() {
        return corpusName;
    }

    public void setCorpusName(String corpusName) {
        this.corpusName = corpusName;
    }

    public long getCorpusSizeBytes() {
        return corpusSizeBytes;
    }

    public void setCorpusSizeBytes(long corpusSizeBytes) {
        this.corpusSizeBytes = corpusSizeBytes;
    }

    public int getWorkerCount() {
        return workerCount;
    }

    public void setWorkerCount(int workerCount) {
        this.workerCount = workerCount;
    }
}
