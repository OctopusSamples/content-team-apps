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
import lombok.NonNull;
import org.jboss.logging.Logger;

/**
 * Builds a GitHub Actions Workflow for PHP projects.
 */
public class PhpComposerBuilder implements PipelineBuilder {

  private static final Logger LOG = Logger.getLogger(PhpComposerBuilder.class.toString());
  private static final GitBuilder GIT_BUILDER = new GitBuilder();

  @Override
  public String getName() {
    return "PHP";
  }

  @Override
  public Boolean canBuild(@NonNull final RepoClient accessor) {
    LOG.log(DEBUG, "PhpComposerBuilder.canBuild(RepoClient)");
    return accessor.testFile("composer.json");
  }

  @Override
  public String generate(@NonNull final RepoClient accessor) {
    LOG.log(DEBUG, "PhpComposerBuilder.generate(RepoClient)");
    return GIT_BUILDER.getInitialComments() + "\n"
        + SnakeYamlFactory.getConfiguredYaml()
        .dump(
            Workflow.builder()
                .name("PHP Build")
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
                                                .run("composer install")
                                                .build())
                                        .add(
                                            RunStep.builder()
                                                .name("List Dependencies")
                                                .shell("bash")
                                                .run(
                                                    "composer show --all > dependencies.txt")
                                                .build())
                                        .add(GIT_BUILDER.collectDependencies())
                                        .add(
                                            RunStep.builder()
                                                .name("List Dependency Updates")
                                                .shell("bash")
                                                .run(
                                                    "composer outdated > dependencyUpdates.txt")
                                                .build())
                                        .add(GIT_BUILDER.collectDependencyUpdates())
                                        .add(
                                            RunStep.builder()
                                                .name("Test")
                                                .shell("bash")
                                                .run(
                                                    "vendor/bin/phpunit --log-junit results.xml tests || true")
                                                .build())
                                        .add(GIT_BUILDER.buildJunitReport("PHP Tests",
                                            "results.xml"))
                                        .add(
                                            RunStep.builder()
                                                .name("Package")
                                                .shell("bash")
                                                .run(
                                                    "SOURCEPATH=.\n"
                                                        + "OUTPUTPATH=.\n"
                                                        + "octo pack \\\n"
                                                        + " --basePath ${SOURCEPATH} \\\n"
                                                        + " --outFolder ${OUTPUTPATH} \\\n"
                                                        + " --id "
                                                        + accessor
                                                        .getRepoName()
                                                        .getOrElse("application")
                                                        + " \\\n"
                                                        + " --version ${{ steps.determine_version.outputs.semVer }} \\\n"
                                                        + " --format zip \\\n"
                                                        + " --overwrite \\\n"
                                                        + " --include '**/*.php' \\\n"
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
                                        .add(GIT_BUILDER.tagRepo())
                                        .add(GIT_BUILDER.createGitHubRelease())
                                        .add(
                                            GIT_BUILDER.uploadToGitHubRelease(
                                                accessor.getRepoName().getOrElse("application")
                                                    + ".${{ steps.determine_version.outputs.semVer }}.zip",
                                                accessor.getRepoName().getOrElse("application")
                                                    + ".${{ steps.determine_version.outputs.semVer }}.zip"))
                                        .add(
                                            GIT_BUILDER.pushToOctopus(
                                                accessor.getRepoName().getOrElse("application")
                                                    + ".${{ steps.determine_version.outputs.semVer }}.zip"))
                                        .add(GIT_BUILDER.uploadOctopusBuildInfo(accessor))
                                        .add(GIT_BUILDER.createOctopusRelease(
                                            accessor,
                                            accessor.getRepoName().getOrElse("application")
                                                + ":"
                                                + "${{ steps.determine_version.outputs.semVer }}"))
                                        .build())
                                .build())
                        .build())
                .build());
  }
}
