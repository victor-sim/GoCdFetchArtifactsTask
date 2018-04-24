package net.soti.go.plugin.task.fetch.artifacts;

import java.util.Collections;

import com.thoughtworks.go.plugin.api.GoPluginIdentifier;

public interface Constants {
    // The type of this extension
    String EXTENSION_TYPE = "task";

    // The extension point API version that this plugin understands
    String API_VERSION = "1.0";

    String REQUEST_CONFIGURATION = "configuration";
    String REQUEST_VALIDATION = "validate";
    String REQUEST_TASK_VIEW = "view";
    String REQUEST_EXECUTION = "execute";

    GoPluginIdentifier PLUGIN_IDENTIFIER = new GoPluginIdentifier(EXTENSION_TYPE, Collections.singletonList(API_VERSION));

    String PIPELINE_NAME = "PipelineName";
    String STAGE_NAME = "StageName";
    String JOB_NAME = "JobName";
    String ARTIFACTS_PATH = "ArtifactsPath";
    String DOWNLOAD_PATH = "DownloadPath";

    String SUCCESS = "success";
    String ERRORS = "errors";

    String ENVIRONMENT_VARIABLES = "environmentVariables";
    String WORKING_DIR = "workingDirectory";
    String VALUE = "value";
    String CONTEXT = "context";
    String CONFIG = "config";

    String GOCD_API_URL = "https://cagomc100.corp.soti.net:8154";
    String GOCD_API_USER = "user";
    String GOCD_API_PASSWORD = "Welcome1234";

    String PIPELINE_MATERIAL = "Pipeline";
}
