package net.soti.go.plugin.task.fetch.artifacts;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.google.gson.GsonBuilder;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.go.plugin.api.task.JobConsoleLogger;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import static net.soti.go.plugin.task.fetch.artifacts.Constants.*;

public final class Utils {

    private Utils() {

    }

    public static String getGoCdApiHost(Map<String, Map> requestBodyMap) {
        final String key = "GOCD_API_URL";
        String overwrittenValue = getEnvironments(requestBodyMap).get(key);

        if (overwrittenValue != null) {
            return overwrittenValue;
        }

        String env = System.getenv(key);
        return StringUtils.isEmpty(env) ? GOCD_API_URL : env;
    }

    public static String getGoCdUser(Map<String, Map> requestBodyMap) {
        final String key = "GOCD_API_USER";

        String overwrittenValue = getEnvironments(requestBodyMap).get(key);

        if (overwrittenValue != null) {
            return overwrittenValue;
        }

        String env = System.getenv(key);
        return StringUtils.isEmpty(env) ? GOCD_API_USER : env;
    }

    public static String getGoCdPassword(Map<String, Map> requestBodyMap) {
        final String key = "GOCD_API_PASSWORD";

        String overwrittenValue = getEnvironments(requestBodyMap).get(key);

        if (overwrittenValue != null) {
            return overwrittenValue;
        }

        String env = System.getenv(key);
        return StringUtils.isEmpty(env) ? GOCD_API_PASSWORD : env;
    }

    public static String readResource(String resourceFile) {
        try (InputStreamReader reader = new InputStreamReader(Utils.class.getResourceAsStream(resourceFile), StandardCharsets.UTF_8)) {
            return IOUtils.toString(reader);
        } catch (IOException e) {
            throw new RuntimeException("Could not find resource " + resourceFile, e);
        }
    }

    static <K, V> Map<K, V> safeCastMap(Object map, Class<K> keyType, Class<V> valueType) {
        checkMap(map);
        //checkMapContents(keyType, valueType, (Map<?, ?>) map);
        return (Map<K, V>) map;
    }

    private static void checkMap(Object map) {
        checkType(Map.class, map);
    }

    private static <K, V> void checkMapContents(Class<K> keyType, Class<V> valueType, Map<?, ?> map) {
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            checkType(keyType, entry.getKey());
            checkType(valueType, entry.getValue());
        }
    }

    private static <K> void checkType(Class<K> expectedType, Object obj) {
        if (!expectedType.isInstance(obj)) {
            throw new IllegalArgumentException("Expected " + expectedType + " but was " + obj.getClass() + ": " + obj);
        }
    }


    static String getConfig(Map<String, Map> requestMap, String key) {
        return Utils.safeCastMap(getConfigMap(requestMap).get(key), String.class, String.class).get(VALUE);
    }

    static String getEnvironmentVariable(Map<String, Map> requestBodyMap, String key) {
        String overwrittenValue = getEnvironments(requestBodyMap).get(key);

        if (overwrittenValue != null) {
            return overwrittenValue;
        }

        return System.getenv(key);
    }

    static String getWorkingDir(Map<String, Map> requestMap) {
        return (String) getContext(requestMap).get(WORKING_DIR);
    }

    static void addMessage(JobConsoleLogger console, String message, Exception e) {
        console.printLine(String.format("[Fetch] %s", message));
        if (e != null) {
            console.printLine(e.toString() + " : " + e.getMessage());
        }
    }

    static GoPluginApiResponse createGoResponse(final int responseCode, Object response) {
        final String json = response == null ? null : new GsonBuilder().create().toJson(response);
        return new GoPluginApiResponse() {
            @Override
            public int responseCode() {
                return responseCode;
            }

            @Override
            public Map<String, String> responseHeaders() {
                return null;
            }

            @Override
            public String responseBody() {
                return json;
            }
        };
    }

    static Map<String, Map> getRequestMap(String requestBody) {
        return Utils.safeCastMap(new GsonBuilder().create().fromJson(requestBody, Map.class), String.class, Map.class);
    }

    static Map<String, String> getConfigMap(Map<String, Map> requestMap) {
        return Utils.safeCastMap(requestMap.get(CONFIG), String.class, String.class);
    }

    private static Map<String, String> getEnvironments(Map<String, Map> requestBodyMap) {
        return Utils.safeCastMap(getContext(requestBodyMap).get(ENVIRONMENT_VARIABLES), String.class, String.class);
    }

    private static Map<String, Object> getContext(Map<String, Map> requestMap) {
        return Utils.safeCastMap(requestMap.get(CONTEXT), String.class, Object.class);
    }
}
