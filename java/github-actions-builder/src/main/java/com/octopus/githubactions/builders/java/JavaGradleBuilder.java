package com.octopus.githubactions.builders.java;

import static org.jboss.logging.Logger.Level.DEBUG;

import com.google.common.collect.ImmutableList;
import com.octopus.builders.PipelineBuilder;
import com.octopus.githubactions.builders.GitBuilder;
import com.octopus.githubactions.builders.SnakeYamlFactory;
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

/** Builds a GitHub Actions Workflow for Gradle projects. */
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
    return SnakeYamlFactory.getConfiguredYaml()
        .dump(
            Workflow.builder()
                .name("Java Gradle Build")
                .on(On.builder().push(new Push()).workflowDispatch(new WorkflowDispatch()).build())
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
                                                "build/test-results/**/*.xml"))
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
                                                        + "echo \"::set-output name=artifact::$(find target -type f \\( -iname \\*.jar -o -iname \\*.war \\) -printf \"%p\\n\" | sort -n | head -1)\"")
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
                                                    "file=\"${{ steps.get_artifact.outputs.artifact }}\"\nextension=\"${file##*.}\"\noctofile=\""
                                                        + accessor
                                                            .getRepoName()
                                                            .getOrElse("application")
                                                        + ".${{ steps.determine_version.outputs.semVer }}.${extension}\"\ncp ${file} ${octofile}\necho \"::set-output name=artifact::${octofile}\"\nls -la")
                                                .build())
                                        .add(
                                            GIT_BUILDER.pushToOctopus(
                                                "${{ steps.get_octopus_artifact.outputs.artifact }}"))
                                        .add(GIT_BUILDER.uploadOctopusBuildInfo(accessor))
                                        .add(GIT_BUILDER.createOctopusRelease(accessor))
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
