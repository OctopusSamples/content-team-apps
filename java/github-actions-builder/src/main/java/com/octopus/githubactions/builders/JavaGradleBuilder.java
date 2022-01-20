package com.octopus.githubactions.builders;

import static org.jboss.logging.Logger.Level.DEBUG;

import com.google.common.collect.ImmutableList;
import com.octopus.builders.PipelineBuilder;
import com.octopus.githubactions.builders.dsl.Build;
import com.octopus.githubactions.builders.dsl.Jobs;
import com.octopus.githubactions.builders.dsl.On;
import com.octopus.githubactions.builders.dsl.Push;
import com.octopus.githubactions.builders.dsl.RunStep;
import com.octopus.githubactions.builders.dsl.Step;
import com.octopus.githubactions.builders.dsl.Workflow;
import com.octopus.githubactions.builders.dsl.WorkflowDispatch;
import com.octopus.repoclients.RepoClient;
import java.util.Arrays;
import lombok.NonNull;
import org.jboss.logging.Logger;

/**
 * Builds a GitHub Actions Workflow for Gradle projects.
 */
public class JavaGradleBuilder implements PipelineBuilder {

  private static final Logger LOG = Logger.getLogger(JavaGradleBuilder.class.toString());
  private static final GitBuilder GIT_BUILDER = new GitBuilder();
  private static final String[] GRADLE_BUILD_FILES = {"build.gradle", "build.gradle.kts"};
  private boolean usesWrapper = false;

  @Override
  public Boolean canBuild(@NonNull final RepoClient accessor) {
    LOG.log(DEBUG, "JavaGradleBuilder.canBuild(RepoClient)");

    if (Arrays.stream(GRADLE_BUILD_FILES).anyMatch(accessor::testFile)) {
      LOG.log(DEBUG, String.join(" or ", GRADLE_BUILD_FILES) + " was found");
      usesWrapper = usesWrapper(accessor);
      LOG.log(DEBUG, "Wrapper script was " + (usesWrapper ? "" : "not ") + "found");
      return true;
    }

    return false;
  }

  @Override
  public String generate(@NonNull final RepoClient accessor) {
    LOG.log(DEBUG, "JavaMavenBuilder.generate(RepoClient)");
    return GIT_BUILDER.getInitialComments() + "\n"
        + SnakeYamlFactory.getConfiguredYaml()
        .dump(
            Workflow.builder()
                .name("Java Gradle Build")
                .on(On.builder().push(new Push()).workflowDispatch(new WorkflowDispatch())
                    .build())
                .jobs(
                    Jobs.builder()
                        .build(
                            Build.builder()
                                .runsOn("ubuntu-latest")
                                .steps(
                                    new ImmutableList.Builder<Step>()
                                        .add(GIT_BUILDER.checkOutStep())
                                        .add(GIT_BUILDER.gitVersionInstallStep())
                                        .add(GIT_BUILDER.getVersionCalculate())
                                        .add(GIT_BUILDER.installOctopusCli())
                                        .add(GIT_BUILDER.installJava())
                                        .add(
                                            RunStep.builder()
                                                .name("List Dependencies")
                                                .shell("bash")
                                                .run(
                                                    gradleExecutable()
                                                        + " dependencies --console=plain > dependencies.txt")
                                                .build())
                                        .add(GIT_BUILDER.collectDependencies())
                                        .add(
                                            RunStep.builder()
                                                .name("Test")
                                                .shell("bash")
                                                .run(
                                                    gradleExecutable()
                                                        + " check --console=plain || true")
                                                .build())
                                        .add(
                                            GIT_BUILDER.buildJunitReport(
                                                "Gradle Tests", "build/test-results/**/*.xml"))
                                        .add(
                                            RunStep.builder()
                                                .name("Package")
                                                .shell("bash")
                                                .run(
                                                    gradleExecutable()
                                                        + " clean assemble --console=plain")
                                                .build())
                                        .add(
                                            RunStep.builder()
                                                .name("Get Artifact Path")
                                                .id("get_artifact")
                                                .shell("bash")
                                                .run(
                                                    "# Find the largest WAR or JAR, and assume that was what we intended to build.\n"
                                                        + "echo \"::set-output name=artifact::$(find build -type f \\( -iname \\*.jar -o -iname \\*.war \\) -printf \"%p\\n\" | sort -n | head -1)\"")
                                                .build())
                                        .add(
                                            RunStep.builder()
                                                .name("Get Artifact Name")
                                                .id("get_artifact_name")
                                                .shell("bash")
                                                .run(
                                                    "# Get the filename without a path\n"
                                                        + "path=\"${{ steps.get_artifact.outputs.artifact }}\"\n"
                                                        + "echo \"::set-output name=artifact::${path##*/}\"")
                                                .build())
                                        .add(GIT_BUILDER.tagRepo())
                                        .add(GIT_BUILDER.createGitHubRelease())
                                        .add(
                                            GIT_BUILDER.uploadToGitHubRelease(
                                                "${{ steps.get_artifact.outputs.artifact }}",
                                                "${{ steps.get_artifact_name.outputs.artifact }}"))
                                        .add(
                                            RunStep.builder()
                                                .name("Create Octopus Artifact")
                                                .id("get_octopus_artifact")
                                                .shell("bash")
                                                .run(
                                                    "# Octopus expects artifacts to have a specific file format\n"
                                                        + "file=\"${{ steps.get_artifact.outputs.artifact }}\"\n"
                                                        + "extension=\"${file##*.}\"\n"
                                                        + "octofile=\""
                                                        + accessor
                                                        .getRepoName()
                                                        .getOrElse("application")
                                                        + ".${{ steps.determine_version.outputs.semVer }}.${extension}\"\n"
                                                        + "cp ${file} ${octofile}\n"
                                                        + "echo \"::set-output name=artifact::${octofile}\"\n"
                                                        + "# The version used when creating a release is the package id, colon, and version\n"
                                                        + "octoversion=\""
                                                        + accessor
                                                        .getRepoName()
                                                        .getOrElse("application")
                                                        + ":${{ steps.determine_version.outputs.semVer }}\"\n"
                                                        + "echo \"::set-output name=octoversion::${octoversion}\"\n")
                                                .build())
                                        .add(
                                            GIT_BUILDER.pushToOctopus(
                                                "${{ steps.get_octopus_artifact.outputs.artifact }}"))
                                        .add(GIT_BUILDER.uploadOctopusBuildInfo(accessor))
                                        .add(GIT_BUILDER.createOctopusRelease(accessor, "${{ steps.get_octopus_artifact.outputs.octoversion }}"))
                                        .build())
                                .build())
                        .build())
                .build());
  }

  private String gradleExecutable() {
    return usesWrapper ? "./gradlew" : "gradle";
  }

  private boolean usesWrapper(@NonNull final RepoClient accessor) {
    return accessor.testFile("gradlew");
  }
}
