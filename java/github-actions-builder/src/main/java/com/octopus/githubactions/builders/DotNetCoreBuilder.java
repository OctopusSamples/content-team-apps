package com.octopus.githubactions.builders;

import static org.jboss.logging.Logger.Level.DEBUG;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.octopus.builders.PipelineBuilder;
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
import java.util.List;
import java.util.regex.Pattern;
import lombok.NonNull;
import org.jboss.logging.Logger;

/**
 * Builds a GitHub Actions Workflow for DotNET Core projects.
 */
public class DotNetCoreBuilder implements PipelineBuilder {

  private static final Logger LOG = Logger.getLogger(DotNetCoreBuilder.class.toString());
  private static final GitBuilder GIT_BUILDER = new GitBuilder();
  private static final Pattern DOT_NET_CORE_REGEX = Pattern.compile(
      "Sdk\\s*=\\s*\"Microsoft\\.NET\\.Sdk");

  @Override
  public Boolean canBuild(@NonNull final RepoClient accessor) {
    LOG.log(DEBUG, "DotnetCoreBuilder.canBuild(RepoClient)");

    return hasSolutionFiles(accessor) && hasDotNetCoreProjectFiles(accessor);
  }

  @Override
  public String generate(@NonNull final RepoClient accessor) {
    LOG.log(DEBUG, "DotNetCoreBuilder.generate(RepoClient)");
    return GIT_BUILDER.getInitialComments() + "\n"
        + SnakeYamlFactory.getConfiguredYaml()
        .dump(
            Workflow.builder()
                .name("DotNET Core Build")
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
                                            .name("Set up DotNET Core")
                                            .uses("actions/setup-dotnet@v1")
                                            .with(
                                                new ImmutableMap.Builder<String, String>()
                                                    .put("dotnet-version", "3.1.402")
                                                    .build())
                                            .build())
                                        .add(GIT_BUILDER.gitVersionInstallStep())
                                        .add(GIT_BUILDER.getVersionCalculate())
                                        .add(GIT_BUILDER.installOctopusCli())
                                        .add(
                                            RunStep.builder()
                                                .name("Install Dependencies")
                                                .shell("bash")
                                                .run("dotnet restore")
                                                .build())
                                        .add(
                                            RunStep.builder()
                                                .name("List Dependencies")
                                                .shell("bash")
                                                .run("dotnet list package > dependencies.txt")
                                                .build())
                                        .add(GIT_BUILDER.collectDependencies())
                                        .add(
                                            RunStep.builder()
                                                .name("List Dependency Updates")
                                                .shell("bash")
                                                .run(
                                                    "dotnet list package --outdated > dependencyUpdates.txt")
                                                .build())
                                        .add(GIT_BUILDER.collectDependencyUpdates())
                                        .add(
                                            RunStep.builder()
                                                .name("Test")
                                                .shell("bash")
                                                .run(
                                                    "dotnet test -l:trx || true")
                                                .build())
                                        .add(UsesWith.builder()
                                            .name("Report")
                                            .uses("dorny/test-reporter@v1")
                                            .ifProperty("always()")
                                            .with(
                                                new ImmutableMap.Builder<String, String>()
                                                    .put("name", "DotNET Tests")
                                                    .put("path", "**/*.trx")
                                                    .put("reporter", "dotnet-trx")
                                                    .put("fail-on-error", "false")
                                                    .build())
                                            .build())
                                        .add(RunStep.builder()
                                            .name("Publish")
                                            .run(
                                                "dotnet publish --configuration Release /p:AssemblyVersion=${{ steps.determine_version.outputs.assemblySemVer }}")
                                            .build())
                                        .add(RunStep.builder()
                                            .name("Package")
                                            .id("package")
                                            .run(
                                                "# Find the publish directories\n"
                                                    + "shopt -s globstar\n"
                                                    + "paths=()\n"
                                                    + "for i in **/publish/*.dll; do\n"
                                                    + "  dir=${i%/*}\n"
                                                    + "  echo ${dir}\n"
                                                    + "  paths=(${paths[@]} ${dir})\n"
                                                    + "done\n"
                                                    + "eval uniquepaths=($(printf \"%s\\n\" \"${paths[@]}\" | sort -u))\n"
                                                    + "for i in \"${uniquepaths[@]}\"; do\n"
                                                    + "  echo $i\n"
                                                    + "done\n"
                                                    + "# For each publish dir, create a package\n"
                                                    + "packages=()\n"
                                                    + "versions=()\n"
                                                    + "for path in \"${uniquepaths[@]}\"; do\n"
                                                    + "  # Get the directory name four deep, which is typically the project folder\n"
                                                    + "  dir=${path}/../../../..\n"
                                                    + "  parentdir=$(builtin cd $dir; pwd)\n"
                                                    + "  projectname=${parentdir##*/}\n"
                                                    + "  # Package the published files\n"
                                                    + "  octo pack \\\n"
                                                    + "  --basePath ${path} \\\n"
                                                    + "  --id ${projectname} \\\n"
                                                    + "  --version ${{ steps.determine_version.outputs.semVer }} \\\n"
                                                    + "  --format zip \\\n"
                                                    + "  --overwrite\n"
                                                    + "  packages=(${packages[@]} \"${projectname}.${{ steps.determine_version.outputs.semVer }}.zip\")\n"
                                                    + "  versions=(${versions[@]} \"${projectname}:${{ steps.determine_version.outputs.semVer }}\")\n"
                                                    + "done\n"
                                                    + "# Join the array with commas\n"
                                                    + "printf -v joined \"%s,\" \"${packages[@]}\"\n"
                                                    + "# Save the list of packages as an output variable\n"
                                                    + "echo \"::set-output name=artifacts::${joined%,}\"\n"
                                                    + "# Do the same again, but use new lines as the separator. These will be used when uploading packages to the GitHub release.\n"
                                                    + "printf -v joinednewline \"%s\\n\" \"${packages[@]}\"\n"
                                                    + "# https://trstringer.com/github-actions-multiline-strings/\n"
                                                    + "# Multiline strings require some care in a workflow\n"
                                                    + "joinednewline=\"${joinednewline//'%'/'%25'}\"\n"
                                                    + "joinednewline=\"${joinednewline//$'\\n'/'%0A'}\"\n"
                                                    + "joinednewline=\"${joinednewline//$'\\r'/'%0D'}\"\n"
                                                    + "# Now build a new line separated list of versions. These will be used when creating an Octopus release.\n"
                                                    + "printf -v versionsjoinednewline \"%s\\n\" \"${versions[@]}\"\n"
                                                    + "versionsjoinednewline=\"${versionsjoinednewline//'%'/'%25'}\"\n"
                                                    + "versionsjoinednewline=\"${versionsjoinednewline//$'\\n'/'%0A'}\"\n"
                                                    + "versionsjoinednewline=\"${versionsjoinednewline//$'\\r'/'%0D'}\"\n"
                                                    + "# Save the list of packages newline separated as an output variable\n"
                                                    + "echo \"::set-output name=artifacts_new_line::${joinednewline%\\n}\"\n"
                                                    + "echo \"::set-output name=versions_new_line::${versionsjoinednewline%\\n}\"\n"
                                            )
                                            .build())
                                        .add(GIT_BUILDER.tagRepo())
                                        .add(UsesWith.builder()
                                            .name("Create Release")
                                            .uses("softprops/action-gh-release@v1")
                                            .with(
                                                new ImmutableMap.Builder<String, String>()
                                                    .put("files",
                                                        "${{ steps.package.outputs.artifacts_new_line }}")
                                                    .put("tag_name",
                                                        "${{ steps.determine_version.outputs.semVer }}+run${{ github.run_number }}-attempt${{ github.run_attempt }}")
                                                    .put("draft", "false")
                                                    .put("prerelease", "false")
                                                    .put("target_commitish", "${{ github.sha }}")
                                                    .build())
                                            .build())
                                        .add(
                                            GIT_BUILDER.pushToOctopus(
                                                "${{ steps.package.outputs.artifacts }}"))
                                        .add(GIT_BUILDER.uploadOctopusBuildInfo(accessor))
                                        .add(GIT_BUILDER.createOctopusRelease(accessor, "${{ steps.package.outputs.versions_new_line }}"))
                                        .build())
                                .build())
                        .build())
                .build());
  }

  private boolean hasSolutionFiles(@NonNull final RepoClient accessor) {
    final List<String> files = accessor.getWildcardFiles("*.sln", 1).getOrElse(List.of());
    LOG.log(DEBUG, "Found " + files.size() + " solution files");
    files.forEach(s -> LOG.log(DEBUG, "  " + s));
    return !files.isEmpty();
  }

  private boolean hasDotNetCoreProjectFiles(@NonNull final RepoClient accessor) {
    final List<String> projectFiles = accessor.getWildcardFiles("**/*.csproj", 1).getOrElse(List.of());
    LOG.log(DEBUG, "Found " + projectFiles.size() + " project files");
    projectFiles.forEach(s -> LOG.log(DEBUG, "  " + s));

    /*
     https://natemcmaster.com/blog/2017/03/09/vs2015-to-vs2017-upgrade/ provides some great insights
     into the various project file formats.
     */
    return projectFiles
        .stream()
        .anyMatch(f -> DOT_NET_CORE_REGEX.matcher(accessor.getFile(f).getOrElse("")).find());
  }
}
