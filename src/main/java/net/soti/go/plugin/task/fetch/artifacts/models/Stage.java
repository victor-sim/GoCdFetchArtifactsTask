package net.soti.go.plugin.task.fetch.artifacts.models;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

/**
 * User: wsim
 * Date: 2018-04-24
 */
public class Stage {
    private static final long serialVersionUID = 5849532045985950210L;
    private String name;
    private int counter;
    private List<Job> jobs;

    public boolean isMatched(final String stageName, final String jobName) {
        return StringUtils.equalsIgnoreCase(stageName, name) && jobs.stream().anyMatch(job -> job.isMatched(jobName));

    }

    public List<JobRevision> getMatchedJobs(final String stageName, final String jobName) {
        if (!StringUtils.equals(stageName, name)) {
            return new ArrayList<>();
        }

        return jobs.stream()
                .filter(job -> job.isMatched(jobName))
                .map(job -> new JobRevision(null, 0, name, counter, job.getName()))
                .collect(Collectors.toList());
    }

    public String getName() {
        return name;
    }

    public int getCounter() {
        return counter;
    }

    public List<Job> getJobs() {
        return jobs;
    }
}
