package common;

import java.io.Serializable;

public class TaskMessage implements Serializable {
    private String jobId;
    private int sectionId;
    private int totalSections;
    private String text;

    public TaskMessage() {
    }

    public TaskMessage(String jobId, int sectionId, int totalSections, String text) {
        this.jobId = jobId;
        this.sectionId = sectionId;
        this.totalSections = totalSections;
        this.text = text;
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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
