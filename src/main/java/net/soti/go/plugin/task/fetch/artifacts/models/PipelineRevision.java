package net.soti.go.plugin.task.fetch.artifacts.models;

import org.apache.commons.lang3.StringUtils;

/**
 * User: wsim
 * Date: 2018-04-24
 */
public class PipelineRevision {
    private final String name;
    private final int counter;

    public PipelineRevision(String name, int counter) {
        this.name = name;
        this.counter = counter;
    }

    public static PipelineRevision parseRevisionString(String revision) throws IllegalArgumentException {
        if (StringUtils.isEmpty(revision)) {
            throw new IllegalArgumentException("Revision string must be specified.");
        }

        String[] items = revision.split("/");
        if(items.length != 4) {
            throw new IllegalArgumentException(String.format("Revision string '%s' is not expected format.", revision));
        }

        int counter = Integer.parseInt(items[1]);

        if(counter <= 0) {
            throw new IllegalArgumentException(String.format("Invalid counter number in given revision string '%s'.", revision));
        }

        return new PipelineRevision(items[0], counter);
    }

    public String getName() {
        return name;
    }

    public int getCounter() {
        return counter;
    }
}
