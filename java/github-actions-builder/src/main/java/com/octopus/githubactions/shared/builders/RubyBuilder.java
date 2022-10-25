package com.octopus.githubactions.shared.builders;

import static org.jboss.logging.Logger.Level.DEBUG;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.octopus.builders.PipelineBuilder;
import com.octopus.githubactions.shared.builders.dsl.Build;
import com.octopus.githubactions.shared.builders.dsl.Jobs;
import com.octopus.githubactions.shared.builders.dsl.On;
import com.octopus.githubactions.shared.builders.dsl.Push;
import com.octopus.githubactions.shared.builders.dsl.RunStep;
import com.octopus.githubactions.shared.builders.dsl.Step;
import com.octopus.githubactions.shared.builders.dsl.UsesWith;
import com.octopus.githubactions.shared.builders.dsl.Workflow;
import com.octopus.githubactions.shared.builders.dsl.WorkflowDispatch;
import com.octopus.repoclients.RepoClient;
import lombok.NonNull;
import org.jboss.logging.Logger;

/**
 * Builds a GitHub Actions Workflow for Ruby projects.
 */
public class RubyBuilder implements PipelineBuilder {

  private static final Logger LOG = Logger.getLogger(RubyBuilder.class.toString());
  private static final GitBuilder GIT_BUILDER = new GitBuilder();

  @Override
  public String getName() {
    return "Ruby";
  }

  @Override
  public Boolean canBuild(@NonNull final RepoClient accessor) {
    LOG.log(DEBUG, "RubyBuilder.canBuild(RepoClient)");
    return accessor.testFile("Gemfile");
  }

  @Override
  public String generate(@NonNull final RepoClient accessor) {
    LOG.log(DEBUG, "RubyBuilder.generate(RepoClient)");
    return "# For a detailed breakdown of this workflow, see https://octopus.com/docs/guides/deploy-ruby-app/to-nginx/using-octopus-onprem-github-builtin\n"
        + "#\n"
        + GIT_BUILDER.getInitialComments() + "\n"
        + SnakeYamlFactory.getConfiguredYaml()
        .dump(
            Workflow.builder()
                .name("Ruby Build")
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
                                        .add(UsesWith.builder()
                                            .name("Set up Ruby")
                                            .uses("actions/setup-ruby@v1")
                                            .with(
                                                new ImmutableMap.Builder<String, String>()
                                                    .put("ruby-version", "2.7")
                                                    .build())
                                            .build())
                                        .add(RunStep.builder()
                                            .name("Install Bundler")
                                            .run("gem install bundler")
                                            .build())
                                        .add(GIT_BUILDER.gitVersionInstallStep())
                                        .add(GIT_BUILDER.getVersionCalculate())
                                        .add(GIT_BUILDER.installOctopusCli())
                                        .add(
                                            RunStep.builder()
                                                .name("Install Dependencies")
                                                .shell("bash")
                                                .run("bundle install")
                                                .build())
                                        .add(
                                            RunStep.builder()
                                                .name("List Dependencies")
                                                .shell("bash")
                                                .run(
                                                    "gem dep > dependencies.txt")
                                                .build())
                                        .add(GIT_BUILDER.collectDependencies())
                                        .add(
                                            RunStep.builder()
                                                .name("List Dependency Updates")
                                                .shell("bash")
                                                .run(
                                                    "gem outdated >  dependencyUpdates.txt")
                                                .build())
                                        .add(GIT_BUILDER.collectDependencyUpdates())
                                        .add(
                                            RunStep.builder()
                                                .name("Test")
                                                .shell("bash")
                                                .run(
                                                    "gem install rspec_junit_formatter; rspec --format RspecJunitFormatter --out results.xml")
                                                .build())
                                        .add(GIT_BUILDER.buildJunitReport("Ruby Tests",
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
                                                        + " --include '**/*.rb' \\\n"
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
