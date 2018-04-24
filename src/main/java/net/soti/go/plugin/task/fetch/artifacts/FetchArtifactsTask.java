package net.soti.go.plugin.task.fetch.artifacts;

import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

import net.soti.go.plugin.task.fetch.artifacts.config.PluginConfig;
import net.soti.go.plugin.task.fetch.artifacts.models.ArtifactFile;
import net.soti.go.plugin.task.fetch.artifacts.models.JobRevision;
import net.soti.go.plugin.task.fetch.artifacts.models.Pipeline;
import net.soti.go.plugin.task.fetch.artifacts.models.PipelineRevision;

import com.google.gson.Gson;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.GoPlugin;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.exceptions.UnhandledRequestTypeException;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.go.plugin.api.task.JobConsoleLogger;
import org.apache.http.HttpStatus;

import static net.soti.go.plugin.task.fetch.artifacts.Constants.*;

@Extension
public class FetchArtifactsTask implements GoPlugin {
    private static final Logger LOG = Logger.getLoggerFor(FetchArtifactsTask.class);

    @Override
    public void initializeGoApplicationAccessor(GoApplicationAccessor goApplicationAccessor) {
        // do nothing
    }

    @Override
    public GoPluginApiResponse handle(GoPluginApiRequest requestMessage) throws UnhandledRequestTypeException {
        GoPluginApiResponse response = Utils.createGoResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Should not reachable.");
        LOG.info(String.format("Get request [%s] %n body[%s]", requestMessage.requestName(), requestMessage.requestBody()));

        try {
            if (requestMessage.requestName().equals(REQUEST_CONFIGURATION)) {
                response = PluginConfig.getConfiguration();
            } else if (requestMessage.requestName().equals(REQUEST_VALIDATION)) {
                response = handleValidate(requestMessage.requestBody());
            } else if (requestMessage.requestName().equals(REQUEST_TASK_VIEW)) {
                response = handleView();
            } else if (requestMessage.requestName().equals(REQUEST_EXECUTION)) {
                response = handleExecute(requestMessage.requestBody());
            } else {
                LOG.error(String.format("UnhandledRequestTypeException - requestName::%s", requestMessage.requestName()));
                throw new UnhandledRequestTypeException(requestMessage.requestName());
            }
        } catch (Exception e) {
            LOG.error("Exception on handling", e);
        }

        LOG.info("Response" + response.responseCode());
        return response;
    }

    @Override
    public GoPluginIdentifier pluginIdentifier() {
        return PLUGIN_IDENTIFIER;
    }

    private GoPluginApiResponse handleValidate(String requestBody) {
        Map<String, Map<String, String>> result = new HashMap<>();
        Map<String, String> errors = new HashMap<>();

        try {
            Map<String, Map> requestMap = Utils.getRequestMap(requestBody);
            for (String key : PluginConfig.getConfigKeys()) {
                if (!requestMap.containsKey(key)) {
                    errors.put(key, String.format("%s is mandatory field. Failed to parse request.", key));
                    result.put(ERRORS, errors);
                } else {
                    Map<String, String> configMap = Utils.safeCastMap(requestMap.get(key), String.class, String.class);
                    String value = configMap.get(VALUE);
                    Map<String, String> validation = PluginConfig.getKeyValueMap().get(key).validate(configMap.get(VALUE));
                    for (Map.Entry<String, String> entry : validation.entrySet()) {
                        errors.put(entry.getKey(), entry.getValue());
                    }
                    if (validation.size() > 0) {
                        result.put(ERRORS, errors);
                    }
                }
            }
        } catch (Exception e) {
            result.put(ERRORS, errors);
        }

        return DefaultGoPluginApiResponse.success(new Gson().toJson(result));
    }

    private List<JobRevision> findUpstreamRecursively(
            final PipelineRevision revision,
            final String pipelineName,
            final String stageName,
            final String jobName,
            final GoCdClient client) {
        try {
            Pipeline pipeline = client.getPipelineInstance(revision.getName(), revision.getCounter());

            if (pipeline == null) {
                LOG.error("Empty pipeline.");
                throw new IOException("Failed to get pipeline instance");
            }
            List<JobRevision> jobs = pipeline.getMatchedJobs(pipelineName, stageName, jobName);
            if (jobs.size() > 0) {
                return jobs;
            }

            List<PipelineRevision> upstreams = pipeline.getUpstreamPipelines();
            for (PipelineRevision upstream : upstreams) {
                jobs = findUpstreamRecursively(upstream, pipelineName, stageName, jobName, client);
                if (jobs.size() > 0) {
                    return jobs;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ArrayList<>(0);
    }

    private List<ArtifactFile> getArtifacts(final JobRevision revision, final GoCdClient client) {
        try {
            return client.getAllArtifacts(revision);
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>(0);
        }
    }

    private boolean downloadArtifact(ArtifactFile artifact, String currentDirectory, String jobname, GoCdClient client) {
        try {
            client.getFile(artifact, currentDirectory, jobname);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private GoPluginApiResponse handleExecute(String requestBody) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        Map<String, Object> response = new HashMap<>();
        response.put(SUCCESS, true);

        JobConsoleLogger console = JobConsoleLogger.getConsoleLogger();

        Map<String, Map> requestMap = Utils.getRequestMap(requestBody);
        final String pipelineName = Utils.getConfig(requestMap, PIPELINE_NAME);
        final String stageName = Utils.getConfig(requestMap, STAGE_NAME);
        final String jobName = Utils.getConfig(requestMap, JOB_NAME);
        final boolean isWildcarded = RegexUtil.isWildcard(jobName);
        final List<String> artifactoryPaths = Arrays.stream(
                Utils.getConfig(requestMap, ARTIFACTS_PATH).split(","))
                .map(String::trim)
                .collect(Collectors.toList());

        final String name = Utils.getEnvironmentVariable(requestMap, "GO_PIPELINE_NAME");
        final int counter = Integer.parseInt(Utils.getEnvironmentVariable(requestMap, "GO_PIPELINE_COUNTER"));
        final String currentDirectory = new File(Utils.getWorkingDir(requestMap)).getAbsolutePath();
        final GoCdClient client = new GoCdClient(requestMap);

        Utils.addMessage(console,
                String.format("Find artifacts '%s/%s/%s' that matched with path(s) '%s' and download to '%s'",
                        pipelineName, stageName, jobName,
                        String.join(", ", artifactoryPaths),
                        currentDirectory),
                null);

        PipelineRevision revision = new PipelineRevision(name, counter);
        List<JobRevision> jobs = findUpstreamRecursively(revision, pipelineName, stageName, jobName, client);

        if (jobs.size() <= 0) {
            String message = String.format("Failed to find pipeline that matched with '%s/%s/%s'", pipelineName, stageName, jobName);
            Utils.addMessage(console, message, null);
            throw new IllegalStateException(message);
        } else {
            RegexUtil regexUtil = new RegexUtil(artifactoryPaths);

            for (JobRevision job : jobs) {
                List<ArtifactFile> artifacts = getArtifacts(job, client);
                if (artifacts.size() <= 0) {
                    Utils.addMessage(console,
                            String.format("No artifacts in '%s/%s/%s' that matched with path(s) '%s'",
                                    pipelineName, stageName, jobName,
                                    String.join(", ", artifactoryPaths)),
                            null);
                }
                for (ArtifactFile artifact : artifacts) {
                    if (regexUtil.accept(artifact.getPath())) {
                        Utils.addMessage(console, String.format("Downloading '%s'", artifact.getPath()), null);
                        if (!downloadArtifact(artifact, currentDirectory, isWildcarded ? job.getJobName() : null, client)) {
                            String message = String.format("Failed downloading '%s'", artifact.getPath());
                            Utils.addMessage(console, message, null);
                            throw new IOException(message);
                        } else {
                            Utils.addMessage(console, String.format("Downloaded successfully '%s'", artifact.getPath()), null);
                        }
                    }
                }
            }
        }

        return Utils.createGoResponse(HttpStatus.SC_OK, response);
    }

    private GoPluginApiResponse handleView() throws IOException {
        Map<String, Object> response = new HashMap<>();
        response.put("displayValue", "SotiFetchArtifacts");
        response.put("template", Utils.readResource("/task.template.html"));
        return Utils.createGoResponse(HttpStatus.SC_OK, response);
    }
}
