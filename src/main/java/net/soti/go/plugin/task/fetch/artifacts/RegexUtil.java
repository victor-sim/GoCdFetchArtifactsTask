package net.soti.go.plugin.task.fetch.artifacts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

/**
 * User: wsim
 * Date: 2018-04-23
 */
public class RegexUtil {
    private final List<Pattern> patterns = new ArrayList<>();
    private static final String[] WILD_CARDS = {"*", "?"};

    public RegexUtil(String wildcard) {
        addPattern(wildcard);
    }

    public static boolean isWildcard(String value) {
        return value != null && Arrays.stream(WILD_CARDS).anyMatch(value::contains);
    }

    public RegexUtil(String[] wildcards) {
        Arrays.stream(wildcards).forEach(this::addPattern);
    }

    public RegexUtil(List<String> wildcards) {
        wildcards.forEach(this::addPattern);
    }

    public List<String> filter(List<String> values) {
        return values.stream().filter(this::accept).collect(Collectors.toList());
    }

    public boolean accept(final String value) {
        return patterns.size() != 0 && patterns.stream().anyMatch(pattern -> pattern.matcher(value).find());

    }

    private static String replaceToRegex(String wildcard) {
        if (StringUtils.isEmpty(wildcard)) {
            throw new IllegalArgumentException("Wildcard string must be specified");
        }

        String regexPattern = wildcard.replace("\\", "/")
                .replace(".", "\\.")
                .replace("[", "\\[")
                .replace("]", "\\]")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("$", "\\$")
                .replace("^", "\\^")
                .replace("-", "\\-")
                .replace("{", "\\{")
                .replace("}", "\\}");

        regexPattern = regexPattern.replace("?", "[^/]")
                .replace("**/", "((.+/)?)")
                .replace("*", "[^/]*")
                .replace("/", "\\/");

        return String.format("^%s$", regexPattern);
    }

    private void addPattern(String wildcard) {
        String regexPattern = replaceToRegex(wildcard);
        Pattern pattern = Pattern.compile(regexPattern);
        patterns.add(pattern);
    }
}
