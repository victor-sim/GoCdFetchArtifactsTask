package net.soti.go.plugin.task.fetch.artifacts.models;

/**
 * User: wsim
 * Date: 2018-04-24
 */
public class JobRevision {
    private final String pipelineName;
    private final int pipelineCounter;
    private final String stageName;
    private final int stageCounter;
    private final String jobName;

    public JobRevision(String pipelineName, int pipelineCounter, String stageName, int stageCounter, String jobName) {
        this.pipelineName = pipelineName;
        this.pipelineCounter = pipelineCounter;
        this.stageName = stageName;
        this.stageCounter = stageCounter;
        this.jobName = jobName;
    }

    public String getPipelineName() {
        return pipelineName;
    }

    public int getPipelineCounter() {
        return pipelineCounter;
    }

    public String getStageName() {
        return stageName;
    }

    public int getStageCounter() {
        return stageCounter;
    }

    public String getJobName() {
        return jobName;
    }
}
