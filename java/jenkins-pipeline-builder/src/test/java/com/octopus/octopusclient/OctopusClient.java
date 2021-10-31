package com.octopus.octopusclient;

import com.google.common.collect.ImmutableMap;
import com.octopus.http.StringHttpClient;
import io.vavr.control.Try;
import java.util.List;
import java.util.Map;
import org.apache.http.message.BasicHeader;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

public class OctopusClient {

  private static final StringHttpClient STRING_HTTP_CLIENT = new StringHttpClient();
  private String apiKey;
  private String url;

  public OctopusClient() {

  }

  public OctopusClient(final String apiKey, final String url) {
    this.setApiKey(apiKey);
    this.setUrl(url);
  }

  public String getDefaultProjectGroupId() {
    return STRING_HTTP_CLIENT.get(getUrl() + "/api/Spaces-1/projectgroups/all",
            List.of(new BasicHeader("X-Octopus-ApiKey", getApiKey())))
        .mapTry(j -> (List<Map<String, Object>>) new ObjectMapper().readValue(j, List.class))
        .mapTry(l -> l.stream()
            .filter(p -> p.get("Name").toString().equals("Default Project Group"))
            .map(p -> p.get("Id").toString())
            .findFirst()
            .get())
        .get();
  }

  public String getDefaultLifecycleId() {
    return STRING_HTTP_CLIENT.get(getUrl() + "/api/Spaces-1/lifecycles/all",
            List.of(new BasicHeader("X-Octopus-ApiKey", getApiKey())))
        .mapTry(j -> (List<Map<String, Object>>) new ObjectMapper().readValue(j, List.class))
        .mapTry(l -> l.stream()
            .filter(p -> p.get("Name").toString().equals("Default Lifecycle"))
            .map(p -> p.get("Id").toString())
            .findFirst()
            .get())
        .get();
  }

  public Try<String> createEnvironment(final String name) {
    return STRING_HTTP_CLIENT.post(url + "/api/Spaces-1/environments",
            "{\"Name\": \"" + name + "\"}",
            List.of(new BasicHeader("X-Octopus-ApiKey", getApiKey())));
  }

  public Try<String> createProject(final String name, final String projectGroupId,
      final String lifecycleId) {
    return STRING_HTTP_CLIENT.post(url + "/api/Spaces-1/projects",
            "{\"Name\": \"" + name + "\", \"ProjectGroupId\": \"" + projectGroupId
                + "\", \"LifeCycleId\": \""
                + lifecycleId + "\"}",
            List.of(new BasicHeader("X-Octopus-ApiKey", getApiKey())));
  }

  public String addStepToProject(final String projectName) {
    final Map<String, Object> projectId = STRING_HTTP_CLIENT.get(
            getUrl() + "/api/Spaces-1/projects/all",
            List.of(new BasicHeader("X-Octopus-ApiKey", getApiKey())))
        .mapTry(j -> (List<Map<String, Object>>) new ObjectMapper().readValue(j, List.class))
        .mapTry(l -> l.stream()
            .filter(p -> p.get("Name").toString().equals(projectName))
            .findFirst()
            .get())
        .get();

    final Map<String, Object> deploymentProcess = STRING_HTTP_CLIENT.get(
            getUrl() + "/api/Spaces-1/deploymentprocesses/" + projectId.get("DeploymentProcessId"),
            List.of(new BasicHeader("X-Octopus-ApiKey", getApiKey())))
        .mapTry(j -> (Map<String, Object>) new ObjectMapper().readValue(j, Map.class))
        .get();

    final Map<String, Object> newStep = new ImmutableMap.Builder<String, Object>()
        .put("Name", "Run a script")
        .put("Condition", "Success")
        .put("StartTrigger", "StartAfterPrevious")
        .put("PackageRequirement", "LetOctopusDecide")
        .put("Properties", new ImmutableMap.Builder<String, Object>().build())
        .put("Actions", List.of(new ImmutableMap.Builder<String, Object>()
                .put("ActionType", "Octopus.Script")
                .put("Name", "Run a script")
                .put("Environments", List.of())
                .put("ExcludedEnvironments", List.of())
                .put("Channels", List.of())
                .put("TenantTags", List.of())
                .put("Packages", List.of())
                .put("IsDisabled", false)
                .put("WorkerPoolId", "")
                .put("WorkerPoolVariable", "")
                .put("Condition", "Success")
                .put("Container ", new ImmutableMap.Builder<String, Object>()
                    .put("FeedId", "")
                    .put("Image", "")
                    .build()
                )
                .put("CanBeUsedForProjectVersioning", false)
                .put("IsRequired", false)
                .put("Properties", new ImmutableMap.Builder<String, Object>()
                    .put("Octopus.Action.RunOnServer", "true")
                    .put("Octopus.Action.EnabledFeatures", "")
                    .put("Octopus.Action.Script.ScriptSource", "Inline")
                    .put("Octopus.Action.Script.Syntax", "Bash")
                    .put("Octopus.Action.Script.ScriptFilename", "")
                    .put("Octopus.Action.Script.ScriptBody", "echo 'hi'")
                    .build()
                )
                .build()
            )
        )
        .build();

    final List<Map<String, Object>> steps = (List<Map<String, Object>>) deploymentProcess.get(
        "Steps");
    steps.add(newStep);

    final Try<String> body = Try.of(() -> new ObjectMapper().writeValueAsString(deploymentProcess));

    return STRING_HTTP_CLIENT.put(
            url + "/api/Spaces-1/deploymentprocesses/" + projectId.get("DeploymentProcessId"),
            body.get(),
            List.of(
                new BasicHeader("X-Octopus-ApiKey", getApiKey()),
                new BasicHeader("Content-Type", "application/javascript")))
        .get();
  }

  public String getApiKey() {
    return apiKey;
  }

  public void setApiKey(String apiKey) {
    this.apiKey = apiKey;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }
}
