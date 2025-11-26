package common;

public class ControlMessage {
    public String jobId;
    public String type;
    public int totalSections;

    private String corpusPath;
    private long corpusSizeBytes;
    private long startTimeMillis;

    public ControlMessage() {
    }

    public ControlMessage(String jobId, int totalSections) {
        this(jobId, totalSections, null, 0L, 0L);
    }

    public ControlMessage(String jobId,
                          int totalSections,
                          String corpusPath,
                          long corpusSizeBytes,
                          long startTimeMillis) {
        this.jobId = jobId;
        this.type = "END";
        this.totalSections = totalSections;
        this.corpusPath = corpusPath;
        this.corpusSizeBytes = corpusSizeBytes;
        this.startTimeMillis = startTimeMillis;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getTotalSections() {
        return totalSections;
    }

    public void setTotalSections(int totalSections) {
        this.totalSections = totalSections;
    }

    public String getCorpusPath() {
        return corpusPath;
    }

    public void setCorpusPath(String corpusPath) {
        this.corpusPath = corpusPath;
    }

    public long getCorpusSizeBytes() {
        return corpusSizeBytes;
    }

    public void setCorpusSizeBytes(long corpusSizeBytes) {
        this.corpusSizeBytes = corpusSizeBytes;
    }

    public long getStartTimeMillis() {
        return startTimeMillis;
    }

    public void setStartTimeMillis(long startTimeMillis) {
        this.startTimeMillis = startTimeMillis;
    }
}
