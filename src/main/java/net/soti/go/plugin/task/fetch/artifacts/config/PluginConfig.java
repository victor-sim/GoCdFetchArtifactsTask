package net.soti.go.plugin.task.fetch.artifacts.config;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import net.soti.go.plugin.task.fetch.artifacts.Constants;
import net.soti.go.plugin.task.fetch.artifacts.config.model.Field;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

public class PluginConfig {
    private static final Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    static final Map<String, Field> FIELDS = new LinkedHashMap<>();

    private static final Field PIPELINE_FIELD = new Field(Constants.PIPELINE_NAME, "Pipeline name to fetch artifact from [dot (.) means current pipeline]",
            "", true, false, "1");
    private static final Field STAGE_FIELD = new Field(Constants.STAGE_NAME, "Stage name to fetch artifact from",
            "", true, false, "2");
    private static final Field JOB_FIELD = new Field(Constants.JOB_NAME, "Job name to fetch artifact from [wildcard allowed]",
            "", true, false, "3");
    private static final Field ARTIFACTS_FIELD = new Field(Constants.ARTIFACTS_PATH,
            "Path(s) of artifact(s) to fetch [Separated by comma (,) for multiple paths, and wildcard allowed (*, **, ?)]",
            "**/*", true, false, "4");
    private static final Field PATH_FIELD = new Field(Constants.DOWNLOAD_PATH,
            "Target directory to store download file(s). [Files will be stored to given path + job name(if job name used wildcard) + artifact path.]",
            ".", true, false, "5");

    static {
        FIELDS.put(PIPELINE_FIELD.getKey(), PIPELINE_FIELD);
        FIELDS.put(STAGE_FIELD.getKey(), STAGE_FIELD);
        FIELDS.put(JOB_FIELD.getKey(), JOB_FIELD);
        FIELDS.put(ARTIFACTS_FIELD.getKey(), ARTIFACTS_FIELD);
        FIELDS.put(PATH_FIELD.getKey(), PATH_FIELD);
    }

    public static GoPluginApiResponse getConfiguration() throws Exception {
        Map<String, Field> map = getKeyValueMap();
        return DefaultGoPluginApiResponse.success(map.size() > 0 ? GSON.toJson(map) : null);
    }

    public static Set<String> getConfigKeys() {
        return FIELDS.keySet();
    }

    public static Map<String, Field> getKeyValueMap() {
        Map<String, Field> keyValueMap = new HashMap<>();
        keyValueMap.putAll(FIELDS);
        return keyValueMap;
    }
}
