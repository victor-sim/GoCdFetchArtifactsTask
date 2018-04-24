package net.soti.go.plugin.task.fetch.artifacts;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.core.Is.is;

public class RegexUtilTests {
    private static final String[] TEST_VALUES = new String[]{
            "result/test.txt",
            "result/log/testlog.txt",
            "result/data/input/accepted/raw.txt",
            "result/.txt",
            "result/txt",
            "result/screen.dat",
            "unit/value/input.log",
            "unit/outval.txt",
            "unit/value/specials/data.json",
            "log/serverlog.log",
            "log/interface.log",
            "log/events.log",
            "scriptexecution.txt",
            "orchestration.log",
            "gocd.git"
    };

    @Test
    public void shouldFindRecursively() {
        String wildcard = "result/**/*.txt";

        RegexUtil regexUtil = new RegexUtil(wildcard);

        List<String> filtered = regexUtil.filter(Arrays.stream(TEST_VALUES).collect(Collectors.toList()));

        Assert.assertThat(filtered.size(), is(4));
        Assert.assertThat(regexUtil.accept(TEST_VALUES[0]), is(true));
        Assert.assertThat(filtered.contains(TEST_VALUES[0]), is(true));
        Assert.assertThat(regexUtil.accept(TEST_VALUES[1]), is(true));
        Assert.assertThat(filtered.contains(TEST_VALUES[1]), is(true));
        Assert.assertThat(regexUtil.accept(TEST_VALUES[2]), is(true));
        Assert.assertThat(filtered.contains(TEST_VALUES[2]), is(true));
        Assert.assertThat(regexUtil.accept(TEST_VALUES[3]), is(true));
        Assert.assertThat(filtered.contains(TEST_VALUES[3]), is(true));
    }

    @Test
    public void shouldFindNonRecursively() {
        String wildcard = "result/*";

        RegexUtil regexUtil = new RegexUtil(wildcard);

        List<String> filtered = regexUtil.filter(Arrays.stream(TEST_VALUES).collect(Collectors.toList()));

        Assert.assertThat(filtered.size(), is(4));
        Assert.assertThat(regexUtil.accept(TEST_VALUES[0]), is(true));
        Assert.assertThat(filtered.contains(TEST_VALUES[0]), is(true));
        Assert.assertThat(regexUtil.accept(TEST_VALUES[3]), is(true));
        Assert.assertThat(filtered.contains(TEST_VALUES[3]), is(true));
        Assert.assertThat(regexUtil.accept(TEST_VALUES[4]), is(true));
        Assert.assertThat(filtered.contains(TEST_VALUES[4]), is(true));
        Assert.assertThat(regexUtil.accept(TEST_VALUES[5]), is(true));
        Assert.assertThat(filtered.contains(TEST_VALUES[5]), is(true));
    }

    @Test
    public void shouldFindRecursivelyFromRoot() {
        String wildcard = "**/*.log";

        RegexUtil regexUtil = new RegexUtil(wildcard);

        List<String> filtered = regexUtil.filter(Arrays.stream(TEST_VALUES).collect(Collectors.toList()));

        Assert.assertThat(filtered.size(), is(5));
        Assert.assertThat(regexUtil.accept(TEST_VALUES[6]), is(true));
        Assert.assertThat(filtered.contains(TEST_VALUES[6]), is(true));
        Assert.assertThat(regexUtil.accept(TEST_VALUES[9]), is(true));
        Assert.assertThat(filtered.contains(TEST_VALUES[9]), is(true));
        Assert.assertThat(regexUtil.accept(TEST_VALUES[10]), is(true));
        Assert.assertThat(filtered.contains(TEST_VALUES[10]), is(true));
        Assert.assertThat(regexUtil.accept(TEST_VALUES[11]), is(true));
        Assert.assertThat(filtered.contains(TEST_VALUES[11]), is(true));
        Assert.assertThat(regexUtil.accept(TEST_VALUES[13]), is(true));
        Assert.assertThat(filtered.contains(TEST_VALUES[13]), is(true));
    }

    @Test
    public void shouldFindExactMatches() {
        Arrays.stream(TEST_VALUES).forEach((String value) -> {
            RegexUtil regexUtil = new RegexUtil(value);
            List<String> filtered = regexUtil.filter(Arrays.stream(TEST_VALUES).collect(Collectors.toList()));
            Assert.assertThat(filtered.size(), is(1));
            Assert.assertThat(filtered.contains(value), is(true));
            Assert.assertThat(regexUtil.accept(value), is(true));
        });
    }

    @Test
    public void shouldFindSingleCharacterWildcard() {
        String wildcard = "**/????.*";

        RegexUtil regexUtil = new RegexUtil(wildcard);

        List<String> filtered = regexUtil.filter(Arrays.stream(TEST_VALUES).collect(Collectors.toList()));

        Assert.assertThat(filtered.size(), is(3));
        Assert.assertThat(regexUtil.accept(TEST_VALUES[0]), is(true));
        Assert.assertThat(filtered.contains(TEST_VALUES[0]), is(true));
        Assert.assertThat(regexUtil.accept(TEST_VALUES[8]), is(true));
        Assert.assertThat(filtered.contains(TEST_VALUES[8]), is(true));
        Assert.assertThat(regexUtil.accept(TEST_VALUES[14]), is(true));
        Assert.assertThat(filtered.contains(TEST_VALUES[14]), is(true));
    }
}
