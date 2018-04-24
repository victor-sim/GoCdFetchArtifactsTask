package net.soti.go.plugin.task.fetch.artifacts.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.internal.LinkedTreeMap;
import org.apache.commons.lang3.StringUtils;

/**
 * User: wsim
 * Date: 2018-04-24
 */
public class ArtifactFile extends HashMap<String, Object> {
    public List<ArtifactFile> getChildren(boolean fileOnly) {
        if (isFile()) {
            return new ArrayList<>();
        }
        List<LinkedTreeMap> files = (List<LinkedTreeMap>) this.get("files");
        List<ArtifactFile> result = new ArrayList<>();

        for (LinkedTreeMap file : files) {
            ArtifactFile art = parse(file);
            if (!fileOnly || art.isFile()) {
                result.add(art);
            }
        }

        return result;
    }

    public ArtifactFile parse(LinkedTreeMap map) {
        ArtifactFile result = new ArtifactFile();
        for (String key : (Set<String>) map.keySet()) {
            result.put(key, map.get(key));
        }

        return result;
    }

    public String getName() {
        return (String) this.get("name");
    }

    public String getUrl() {
        return (String) this.get("url");
    }

    public boolean isFile() {
        return "file".equals(this.get("type"));
    }

    public String getPath() {
        Pattern artifactsUrlPattern = Pattern.compile("^(?<GoCdHost>http.+)\\/go\\/files\\/(?<PipelineName>[^\\/]+)\\/" +
                "(?<PipelineCounter>\\d+)\\/(?<StageName>[^\\/]+)\\/(?<StageCounter>\\d+)\\/(?<JobName>[^\\/]+)\\/(?<ArtifactPath>.+)$");
        String url = getUrl();

        Matcher matcher = artifactsUrlPattern.matcher(url);

        if (!matcher.matches()) {
            return StringUtils.EMPTY;
        }

        return matcher.group("ArtifactPath");
    }

    public List<ArtifactFile> getChildrenRecursively() {
        List<ArtifactFile> result = new ArrayList<>();
        if (isFile()) {
            return result;
        }

        List<ArtifactFile> children = getChildren(false);
        for (ArtifactFile child : children) {
            if (child.isFile()) {
                result.add(child);
            } else {
                result.addAll(child.getChildrenRecursively());
            }
        }

        return result;
    }
}
