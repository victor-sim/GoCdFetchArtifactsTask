package net.soti.go.plugin.task.fetch.artifacts.models;

import net.soti.go.plugin.task.fetch.artifacts.RegexUtil;


/**
 * User: wsim
 * Date: 2018-04-24
 */
public class Job {
    private static final long serialVersionUID = 7845090560321515454L;
    private String name;
    private String result;
    private String state;

    public boolean isMatched(String jobName) {
        RegexUtil regexUtil = new RegexUtil(jobName);
        return regexUtil.accept(name);
    }

    public String getState(String state) {
        return state;
    }

    public String getName() {
        return name;
    }

    public String getResult() {
        return result;
    }

    public boolean isPassed() {
        return "Completed".equals(state) && "Passed".equals(state);
    }
}
