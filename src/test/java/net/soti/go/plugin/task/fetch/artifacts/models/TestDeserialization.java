package net.soti.go.plugin.task.fetch.artifacts.models;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * User: wsim
 * Date: 2018-04-24
 */
public class TestDeserialization {
    @Test
    public void validatePipelineDeserialization() {
        String pipelineInstanceString = readResource("pipeline_instance.json");

        Pipeline result = Pipeline.fromJson(pipelineInstanceString);
        List<JobRevision> oneWildcard = result.getMatchedJobs("Coverage_v1414", "Gather", "*-runInstance-1");
        List<JobRevision> allWildcard = result.getMatchedJobs("Coverage_v1414", "Gather", "*");
        List<JobRevision> ondDigitWildcard = result.getMatchedJobs("Coverage_v1414", "Gather", "BDD-W2012-SQL2012-runInstance-?");

        assertThat(result, notNullValue());
        assertThat(result.getName(), is("Coverage_v1414"));
        assertThat(result.getCounter(), is(11));
        assertThat(oneWildcard.size(), is(1));
        assertThat(oneWildcard.get(0).getJobName(), is("BDD-W2012-SQL2012-runInstance-1"));
        assertThat(allWildcard.size(), is(80));
        assertThat(ondDigitWildcard.size(), is(9));
    }

    private String readResource(String resourceFile) {
        try (InputStreamReader reader = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(resourceFile), StandardCharsets
                .UTF_8)) {
            return IOUtils.toString(reader);
        } catch (IOException e) {
            throw new RuntimeException("Could not find resource " + resourceFile, e);
        }
    }
}
