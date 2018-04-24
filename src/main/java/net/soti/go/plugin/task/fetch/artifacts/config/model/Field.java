package net.soti.go.plugin.task.fetch.artifacts.config.model;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.StringUtils;

public class Field {
    private final String key;

    @Expose
    @SerializedName("display-name")
    String displayName;

    @Expose
    @SerializedName("default-value")
    private String defaultValue;

    @Expose
    @SerializedName("required")
    protected Boolean required;

    @Expose
    @SerializedName("secure")
    private Boolean secure;

    @Expose
    @SerializedName("display-order")
    private String displayOrder;

    public Field(String key, String displayName, String defaultValue, Boolean required, Boolean secure, String displayOrder) {
        this.key = key;
        this.displayName = displayName;
        this.defaultValue = defaultValue;
        this.required = required;
        this.secure = secure;
        this.displayOrder = displayOrder;
    }

    public Map<String, String> validate(String input) {
        HashMap<String, String> result = new HashMap<>();
        String validationError = doValidate(input);
        if (StringUtils.isNotBlank(validationError)) {
            result.put("key", key);
            result.put("message", validationError);
        }
        return result;
    }

    protected String doValidate(String input) {
        if (required && StringUtils.isBlank(input)) {
            return displayName + " must not be blank.";
        }
        return null;
    }

    public String getKey() {
        return key;
    }
}