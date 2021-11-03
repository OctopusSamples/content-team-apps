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
import lombok.NonNull;
import org.jboss.logging.Logger;

/** Builds a GitHub Actions Workflow for Node.js projects. */
public class NodeJsBuilder implements PipelineBuilder {

  private static final Logger LOG = Logger.getLogger(NodeJsBuilder.class.toString());
  private static final GitBuilder GIT_BUILDER = new GitBuilder();
  private boolean useYarn = false;

  @Override
  public Boolean canBuild(@NonNull final RepoClient accessor) {
    LOG.log(DEBUG, "NodeJsBuilder.canBuild(RepoClient)");
    useYarn = accessor.testFile("yarn.lock");
    return accessor.testFile("package.json");
  }

  @Override
  public String generate(@NonNull final RepoClient accessor) {
    LOG.log(DEBUG, "JavaMavenBuilder.generate(RepoClient)");
    return SnakeYamlFactory.getConfiguredYaml()
        .dump(
            Workflow.builder()
                .name("Java Maven Build")
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
                                        .add(
                                            RunStep.builder()
                                                .name("Install Dependencies")
                                                .shell("bash")
                                                .run(getPackageManager() + " install")
                                                .build())
                                        .add(
                                            RunStep.builder()
                                                .name("List Dependencies")
                                                .shell("bash")
                                                .run(
                                                    getPackageManager()
                                                        + " list --all > dependencies.txt")
                                                .build())
                                        .add(GIT_BUILDER.collectDependencies())
                                        .add(
                                            RunStep.builder()
                                                .name("List Dependency Updates")
                                                .shell("bash")
                                                .run(
                                                    getPackageManager()
                                                        + " outdated > dependencieupdates.txt || true")
                                                .build())
                                        .add(GIT_BUILDER.collectDependencyUpdates())
                                        .add(
                                            RunStep.builder()
                                                .name("Test")
                                                .shell("bash")
                                                .run(getPackageManager() + " test || true")
                                                .build())
                                        .add(
                                            RunStep.builder()
                                                .name("Build")
                                                .shell("bash")
                                                .run(getPackageManager() + " run build")
                                                .build())
                                        .add(
                                            RunStep.builder()
                                                .name("Get Artifact")
                                                .id("get_artifact")
                                                .shell("bash")
                                                .run(
                                                    "\"::set-output name=artifact::"
                                                        + accessor.getRepoName()
                                                        + "${{ steps.determine_version.outputs.semVer }}.zip"
                                                        + "\"")
                                                .build())
                                        .add(
                                            RunStep.builder()
                                                .name("Get Artifact Name")
                                                .id("get_artifact_name")
                                                .shell("bash")
                                                .run(
                                                    "echo \"::set-output name=artifact::"
                                                        + accessor.getRepoName()
                                                        + "${{ steps.determine_version.outputs.semVer }}.zip"
                                                        + "\"")
                                                .build())
                                        .add(GIT_BUILDER.createGitHubRelease())
                                        .add(GIT_BUILDER.uploadToGitHubRelease())
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

  private String getPackageManager() {
    return useYarn ? "yarn" : "npm";
  }
}
