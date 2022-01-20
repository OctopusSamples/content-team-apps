package com.octopus.githubactions.builders;

import static org.jboss.logging.Logger.Level.DEBUG;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Map;
import lombok.NonNull;
import org.jboss.logging.Logger;

/**
 * Builds a GitHub Actions Workflow for Node.js projects.
 */
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
    LOG.log(DEBUG, "NodeJsBuilder.generate(RepoClient)");
    return GIT_BUILDER.getInitialComments() + "\n"
        + SnakeYamlFactory.getConfiguredYaml()
        .dump(
            Workflow.builder()
                .name("Node.js Build")
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
                                                        + " outdated > dependencyUpdates.txt || true")
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
                                                .run(
                                                    (!scriptExists(accessor, "build")
                                                        ? "# package.json does not define a build script, so the build command is commented out.\n# "
                                                        : "")
                                                        + getPackageManager()
                                                        + " run build")
                                                .build())
                                        .add(
                                            RunStep.builder()
                                                .name("Package")
                                                .shell("bash")
                                                .run(
                                                    "SOURCEPATH=.\n"
                                                        + "OUTPUTPATH=.\n"
                                                        + "# If there is a build directory, assume that is what we want to package\n"
                                                        + "if [[ -d \"build\" ]]; then\n"
                                                        + "  SOURCEPATH=build\n"
                                                        + "  OUTPUTPATH=...\n"
                                                        + "fi\n"
                                                        + "octo pack \\\n"
                                                        + " --basePath ${SOURCEPATH} \\\n"
                                                        + " --outFolder ${OUTPUTPATH} \\\n"
                                                        + " --id "
                                                        + accessor
                                                        .getRepoName()
                                                        .getOrElse("application")
                                                        + " \\\n"
                                                        + " --version ${{ steps.determine_version.outputs.fullSemVer }} \\\n"
                                                        + " --format zip \\\n"
                                                        + " --overwrite \\\n"
                                                        + " --include '**/*.html' \\\n"
                                                        + " --include '**/*.htm' \\\n"
                                                        + " --include '**/*.css' \\\n"
                                                        + " --include '**/*.js' \\\n"
                                                        + " --include '**/*.min' \\\n"
                                                        + " --include '**/*.map' \\\n"
                                                        + " --include '**/*.sql' \\\n"
                                                        + " --include '**/*.png' \\\n"
                                                        + " --include '**/*.jpg' \\\n"
                                                        + " --include '**/*.jpeg' \\\n"
                                                        + " --include '**/*.gif' \\\n"
                                                        + " --include '**/*.json' \\\n"
                                                        + " --include '**/*.env' \\\n"
                                                        + " --include '**/*.txt' \\\n"
                                                        + " --include '**/*.Procfile'")
                                                .build())
                                        .add(GIT_BUILDER.createGitHubRelease())
                                        .add(
                                            GIT_BUILDER.uploadToGitHubRelease(
                                                accessor.getRepoName().getOrElse("application")
                                                    + ".${{ steps.determine_version.outputs.fullSemVer }}.zip",
                                                accessor.getRepoName().getOrElse("application")
                                                    + ".${{ steps.determine_version.outputs.fullSemVer }}.zip"))
                                        .add(
                                            GIT_BUILDER.pushToOctopus(
                                                accessor.getRepoName().getOrElse("application")
                                                    + ".${{ steps.determine_version.outputs.fullSemVer }}.zip"))
                                        .add(GIT_BUILDER.uploadOctopusBuildInfo(accessor))
                                        .add(GIT_BUILDER.createOctopusRelease(
                                            accessor,
                                            accessor.getRepoName().getOrElse("application")
                                                + ":"
                                                + "${{ steps.determine_version.outputs.fullSemVer }}"))
                                        .build())
                                .build())
                        .build())
                .build());
  }

  private boolean scriptExists(@NonNull final RepoClient accessor, @NonNull final String script) {
    return accessor
        .getFile("package.json")
        .mapTry(j -> new ObjectMapper().readValue(j, Map.class))
        .mapTry(m -> (Map) (m.get("scripts")))
        .mapTry(s -> s.containsKey(script))
        .getOrElse(false);
  }

  private String getPackageManager() {
    return useYarn ? "yarn" : "npm";
  }
}
