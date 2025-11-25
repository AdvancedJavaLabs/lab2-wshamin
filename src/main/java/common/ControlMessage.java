package common;

public class ControlMessage {
    public String jobId;
    public String type;
    public int totalSections;

    public ControlMessage() {
    }

    public ControlMessage(String jobId, int totalSections) {
        this.jobId = jobId;
        this.type = "END";
        this.totalSections = totalSections;
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
}
