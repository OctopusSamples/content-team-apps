package com.octopus.githubactions.builders.java;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.octopus.githubactions.builders.SnakeYamlFactory;
import com.octopus.githubactions.builders.dsl.Build;
import com.octopus.githubactions.builders.dsl.Jobs;
import com.octopus.githubactions.builders.dsl.On;
import com.octopus.githubactions.builders.dsl.Push;
import com.octopus.githubactions.builders.dsl.RunStep;
import com.octopus.githubactions.builders.dsl.Step;
import com.octopus.githubactions.builders.dsl.UsesStep;
import com.octopus.githubactions.builders.dsl.UsesWith;
import com.octopus.githubactions.builders.dsl.Workflow;
import com.octopus.githubactions.builders.dsl.WorkflowDispatch;
import com.octopus.jenkins.builders.PipelineBuilder;
import com.octopus.repoclients.RepoClient;
import lombok.NonNull;
import org.jboss.logging.Logger;

/**
 * Builds a GitHub Actions Workflow for Maven projects.
 */
public class JavaMavenBuilder implements PipelineBuilder {

  private static final Logger LOG = Logger.getLogger(JavaMavenBuilder.class.toString());
  private boolean usesWrapper = false;

  @Override
  public Boolean canBuild(@NonNull final RepoClient accessor) {
    if (accessor.testFile("pom.xml")) {
      usesWrapper = usesWrapper(accessor);
      return true;
    }

    return false;
  }

  @Override
  public String generate(@NonNull final RepoClient accessor) {
    return SnakeYamlFactory.getConfiguredYaml().dump(
        Workflow.builder()
            .name("Java Maven Build")
            .on(On.builder()
                .push(new Push())
                .workflowDispatch(new WorkflowDispatch())
                .build()
            )
            .jobs(Jobs.builder()
                .build(Build.builder()
                    .runsOn("ubuntu-latest")
                    .steps(new ImmutableList.Builder<Step>()
                        .add(UsesStep.builder().uses("actions/checkout@v1").build())
                        .add(UsesWith.builder()
                            .name("Install Octopus Deploy CLI")
                            .uses("OctopusDeploy/install-octocli@v1")
                            .with(new ImmutableMap.Builder<String, String>()
                                .put("version", "latest")
                                .build())
                            .build())
                        .add(UsesWith.builder()
                            .name("Set up JDK 1.17")
                            .uses("actions/setup-java@v2")
                            .with(new ImmutableMap.Builder<String, String>()
                                .put("java-version", "17")
                                .put("distribution", "adopt")
                                .build())
                            .build())
                        .add(RunStep.builder()
                            .name("List Dependencies")
                            .shell("bash")
                            .run(
                                "mvn --batch-mode dependency:tree --no-transfer-progress > dependencies.txt")
                            .build())
                        .add(UsesWith.builder()
                            .name("Collect Dependencies")
                            .uses("actions/upload-artifact@v2")
                            .with(new ImmutableMap.Builder<String, String>()
                                .put("name", "Dependencies")
                                .put("path", "dependencies.txt")
                                .build())
                            .build())
                        .add(RunStep.builder()
                            .name("List Dependency Updates")
                            .shell("bash")
                            .run(
                                "mvn --batch-mode versions:display-dependency-updates > dependencyUpdates.txt")
                            .build())
                        .add(UsesWith.builder()
                            .name("Collect Dependency Updates")
                            .uses("actions/upload-artifact@v2")
                            .with(new ImmutableMap.Builder<String, String>()
                                .put("name", "Dependencies Updates")
                                .put("path", "dependencyUpdates.txt")
                                .build())
                            .build())
                        .add(RunStep.builder()
                            .name("Test")
                            .shell("bash")
                            .run("mvn --batch-mode -Dmaven.test.failure.ignore=true test")
                            .build())
                        .add(RunStep.builder()
                            .name("Package")
                            .shell("bash")
                            .run("mvn --batch-mode -DskipTests=true package")
                            .build())
                        .add(RunStep.builder()
                            .name("Get Artifact")
                            .id("get_artifact")
                            .shell("bash")
                            .run(
                                "echo \"::set-output name=artifact::$(find target -type f \\( -iname \\*.jar -o -iname \\*.war \\) -printf \"%p\\n\" | sort -n | head -1)\"")
                            .build())
                        .add(RunStep.builder()
                            .name("Get Artifact Name")
                            .id("get_artifact_name")
                            .shell("bash")
                            .run(
                                "path=\"${{ steps.get_artifact.outputs.artifact }}\"; echo \"::set-output name=artifact::${path##*/}\"")
                            .build())
                        .add(UsesWith.builder()
                            .name("Create Release")
                            .id("create_release")
                            .uses("actions/create-release@v1")
                            .env(new ImmutableMap.Builder<String, String>()
                                .put("GITHUB_TOKEN", "${{ secrets.GITHUB_TOKEN }}")
                                .build())
                            .with(new ImmutableMap.Builder<String, String>()
                                .put("tag_name", "0.1.${{ github.run_number }}")
                                .put("release_name", "Release 0.1.${{ github.run_number }}")
                                .put("draft", "false")
                                .put("prerelease", "false")
                                .build())
                            .build())
                        .add(UsesWith.builder()
                            .name("Upload Release Asset")
                            .uses("actions/upload-release-asset@v1")
                            .env(new ImmutableMap.Builder<String, String>()
                                .put("GITHUB_TOKEN", "${{ secrets.GITHUB_TOKEN }}")
                                .build())
                            .with(new ImmutableMap.Builder<String, String>()
                                .put("upload_url", "${{ steps.create_release.outputs.upload_url }}")
                                .put("asset_path", "${{ steps.get_artifact.outputs.artifact }}")
                                .put("asset_name",
                                    "${{ steps.get_artifact_name.outputs.artifact }}")
                                .put("asset_content_type", "application/octet-stream")
                                .build())
                            .build())
                        .build())
                    .build())
                .build())
            .build());
  }

  private boolean usesWrapper(@NonNull final RepoClient accessor) {
    return accessor.testFile("mvnw");
  }

}
