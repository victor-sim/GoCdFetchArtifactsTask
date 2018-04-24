package net.soti.go.plugin.task.fetch.artifacts.models;

import java.util.List;

import net.soti.go.plugin.task.fetch.artifacts.Constants;

/**
 * User: wsim
 * Date: 2018-04-24
 */
public class MaterialRevision {
    private List<Modification> modifications;
    private Material material;

    public List<Modification> getModifications() {
        return modifications;
    }

    public Material getMaterial() {
        return material;
    }

    public boolean isPipeline() {
        return Constants.PIPELINE_MATERIAL.equals(material.type);
    }
}
