package com.octopus.githubactions.builders.java;

import static org.jboss.logging.Logger.Level.DEBUG;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.octopus.builders.PipelineBuilder;
import com.octopus.githubactions.builders.SnakeYamlFactory;
import com.octopus.githubactions.builders.dsl.Build;
import com.octopus.githubactions.builders.dsl.Jobs;
import com.octopus.githubactions.builders.dsl.On;
import com.octopus.githubactions.builders.dsl.Push;
import com.octopus.githubactions.builders.dsl.RunStep;
import com.octopus.githubactions.builders.dsl.Step;
import com.octopus.githubactions.builders.dsl.UsesWith;
import com.octopus.githubactions.builders.dsl.Workflow;
import com.octopus.githubactions.builders.dsl.WorkflowDispatch;
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
    LOG.log(DEBUG, "JavaMavenBuilder.canBuild(RepoClient)");
    if (accessor.testFile("pom.xml")) {
      usesWrapper = usesWrapper(accessor);
      return true;
    }

    return false;
  }

  @Override
  public String generate(@NonNull final RepoClient accessor) {
    LOG.log(DEBUG, "JavaMavenBuilder.generate(RepoClient)");
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
                        .add(UsesWith.builder()
                            .uses("actions/checkout@v1")
                            .with(new ImmutableMap.Builder<String, String>()
                                .put("fetch-depth", "0")
                                .build())
                            .build())
                        .add(UsesWith.builder()
                            .name("Install GitVersion")
                            .uses("gittools/actions/gitversion/setup@v0.9.7")
                            .with(new ImmutableMap.Builder<String, String>()
                                .put("versionSpec", "5.x")
                                .build())
                            .build())
                        .add(UsesWith.builder()
                            .name("Determine Version")
                            .id("determine_version")
                            .uses("gittools/actions/gitversion/execute@v0.9.7")
                            .build())
                        .add(UsesWith.builder()
                            .name("Install Octopus Deploy CLI")
                            .uses("OctopusDeploy/install-octocli@v1.1.1")
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
                            .name("Set Version")
                            .shell("bash")
                            .run(
                                mavenExecutable()
                                    + " --batch-mode versions:set -DnewVersion=${{ steps.determine_version.outputs.semVer }}")
                            .build())
                        .add(RunStep.builder()
                            .name("List Dependencies")
                            .shell("bash")
                            .run(
                                mavenExecutable()
                                    + " --batch-mode dependency:tree --no-transfer-progress > dependencies.txt")
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
                                mavenExecutable()
                                    + " --batch-mode versions:display-dependency-updates > dependencyUpdates.txt")
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
                            .run(mavenExecutable()
                                + " --batch-mode -Dmaven.test.failure.ignore=true test")
                            .build())
                        .add(UsesWith.builder()
                            .name("Report")
                            .uses("dorny/test-reporter@v1")
                            .ifProperty("always()")
                            .with(new ImmutableMap.Builder<String, String>()
                                .put("name", "Maven Tests")
                                .put("path", "target/surefire-reports/*.xml")
                                .put("reporter", "java-junit")
                                .put("fail-on-error", "false")
                                .build())
                            .build())
                        .add(RunStep.builder()
                            .name("Package")
                            .shell("bash")
                            .run(mavenExecutable() + " --batch-mode -DskipTests=true package")
                            .build())
                        .add(RunStep.builder()
                            .name("Get Artifact")
                            .id("get_artifact")
                            .shell("bash")
                            .run(
                                "# Find the largest WAR or JAR, and assume that was what we intended to build.\n"
                                    + "echo \"::set-output name=artifact::$(find target -type f \\( -iname \\*.jar -o -iname \\*.war \\) -printf \\\"%p\\n\\\" | sort -n | head -1)\"")
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
                                .put("tag_name", "${{ steps.determine_version.outputs.semVer }}")
                                .put("release_name",
                                    "Release ${{ steps.determine_version.outputs.semVer }}")
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
                        .add(RunStep.builder()
                            .name("Create Octopus Artifact")
                            .id("get_octopus_artifact")
                            .shell("bash")
                            .run(
                                "file=\"${{ steps.get_artifact.outputs.artifact }}\"\nextension=\"${file##*.}\"\noctofile=\""
                                    + accessor.getRepoName().get()
                                    + ".${{ steps.determine_version.outputs.semVer }}.${extension}\"\ncp ${file} ${octofile}; echo \"::set-output name=artifact::${octofile}\"\nls -la")
                            .build())
                        .add(UsesWith.builder()
                            .name("Push to Octopus")
                            .uses("OctopusDeploy/push-package-action@v1.1.1")
                            .with(new ImmutableMap.Builder<String, String>()
                                .put("api_key", "${{ secrets.OCTOPUS_API_TOKEN }}")
                                .put("packages",
                                    "${{ steps.get_octopus_artifact.outputs.artifact }}")
                                .put("server", "${{ secrets.OCTOPUS_SERVER_URL }}")
                                .build())
                            .build())
                        .add(UsesWith.builder()
                            .name("Generate Octopus Deploy build information")
                            .uses("xo-energy/action-octopus-build-information@v1.1.2")
                            .with(new ImmutableMap.Builder<String, String>()
                                .put("octopus_api_key", "${{ secrets.OCTOPUS_API_TOKEN }}")
                                .put("octopus_project", accessor.getRepoName().get())
                                .put("octopus_server", "${{ secrets.OCTOPUS_SERVER_URL }}")
                                .put("push_version",
                                    "${{ steps.determine_version.outputs.semVer }}")
                                .put("push_package_ids", accessor.getRepoName().get())
                                .put("output_path", "octopus")
                                .build())
                            .build())
                        .add(UsesWith.builder()
                            .name("Create Octopus Release")
                            .uses("OctopusDeploy/create-release-action@v1.1.1")
                            .with(new ImmutableMap.Builder<String, String>()
                                .put("api_key", "${{ secrets.OCTOPUS_API_TOKEN }}")
                                .put("project", accessor.getRepoName().get())
                                .put("server", "${{ secrets.OCTOPUS_SERVER_URL }}")
                                .put("deploy_to", "Development")
                                .build())
                            .build())
                        .build())
                    .build())
                .build())
            .build());
  }

  private String mavenExecutable() {
    return usesWrapper ? "./mvnw" : "mvn";
  }

  private boolean usesWrapper(@NonNull final RepoClient accessor) {
    return accessor.testFile("mvnw");
  }

}
