package net.soti.go.plugin.task.fetch.artifacts.models;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thoughtworks.go.plugin.api.logging.Logger;
import org.apache.commons.lang3.StringUtils;

/**
 * User: wsim
 * Date: 2018-04-24
 */
public class Pipeline {
    private String name;
    private int counter;
    private List<Stage> stages;
    private BuildCause buildCause;

    public static Pipeline fromJson(String json) {
        final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
        return gson.fromJson(json, Pipeline.class);
    }

    public boolean isMatched(final String pipelineName, final String stageName, final String jobName) {
        return StringUtils.equalsIgnoreCase(pipelineName, name) && stages.stream().anyMatch(stage -> stage.isMatched(stageName, jobName));
    }

    public List<JobRevision> getMatchedJobs(final String pipelineName, final String stageName, final String jobName) {
        if (!StringUtils.equals(pipelineName, name)) {
            return new ArrayList<>();
        }

        return stages.stream()
                .flatMap(stage -> stage.getMatchedJobs(stageName, jobName).stream())
                .map(revision -> new JobRevision(name, counter, revision.getStageName(), revision.getStageCounter(), revision.getJobName()))
                .collect(Collectors.toList());
    }

    public String getName() {
        return name;
    }

    public int getCounter() {
        return counter;
    }

    public List<Stage> getStages() {
        return stages;
    }

    public List<PipelineRevision> getUpstreamPipelines() {
        return buildCause.getMaterialRevisions().stream()
                .filter(MaterialRevision::isPipeline)
                .map(revision -> PipelineRevision.parseRevisionString(revision.getModifications().get(0).getRevision()))
                .collect(Collectors.toList());
    }
}
