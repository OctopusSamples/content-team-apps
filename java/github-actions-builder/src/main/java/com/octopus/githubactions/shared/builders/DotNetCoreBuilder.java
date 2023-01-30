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
import java.util.ArrayList;
import java.util.Arrays;
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

  private String workingDirectory = null;

  /**
   * This builder is very permissive, finding any solution files anywhere in the repo. If there are
   * more specific builders at the top level, use those first.
   *
   * @return A lower priority than the more specific builders.
   */
  @Override
  public Integer getPriority() {
    return -10;
  }

  @Override
  public String getName() {
    return "DotNET Core";
  }

  @Override
  public Boolean canBuild(@NonNull final RepoClient accessor) {
    LOG.log(DEBUG, "DotnetCoreBuilder.canBuild(RepoClient)");

    final List<String> solutionFiles = hasSolutionFiles(accessor);

    if (!solutionFiles.isEmpty()) {
      setWorkingDir(solutionFiles);
      return hasDotNetCoreProjectFiles(accessor);
    }

    return false;
  }

  @Override
  public String generate(@NonNull final RepoClient accessor) {
    LOG.log(DEBUG, "DotNetCoreBuilder.generate(RepoClient)");
    return "# For a detailed breakdown of this workflow, see https://octopus.com/docs/guides/deploy-aspnetcore-app/to-iis/using-octopus-onprem-github-builtin\n"
        + "#\n"
        + GIT_BUILDER.getInitialComments() + "\n"
        + SnakeYamlFactory.getConfiguredYaml()
        .dump(
            Workflow.builder()
                .permissions(GIT_BUILDER.buildPermissions())
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
                                            .uses("actions/setup-dotnet@v3")
                                            .with(
                                                new ImmutableMap.Builder<String, String>()
                                                    .put("dotnet-version", "3.1.x\n5.0.x\n6.0.x")
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
                                                .workingDirectory(workingDirectory)
                                                .build())
                                        .add(
                                            RunStep.builder()
                                                .name("List Dependencies")
                                                .shell("bash")
                                                .run("dotnet list package > dependencies.txt")
                                                .workingDirectory(workingDirectory)
                                                .build())
                                        .add(GIT_BUILDER.collectDependencies(workingDirectory))
                                        .add(
                                            RunStep.builder()
                                                .name("List Dependency Updates")
                                                .shell("bash")
                                                .run(
                                                    "dotnet list package --outdated > dependencyUpdates.txt")
                                                .workingDirectory(workingDirectory)
                                                .build())
                                        .add(GIT_BUILDER.collectDependencyUpdates(workingDirectory))
                                        .add(
                                            RunStep.builder()
                                                .name("Test")
                                                .shell("bash")
                                                .run(
                                                    "dotnet test -l:trx")
                                                .workingDirectory(workingDirectory)
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
                                            .workingDirectory(workingDirectory)
                                            .build())
                                        .add(RunStep.builder()
                                            .name("Package")
                                            .id("package")
                                            .run(
                                                "# \"dotnet publish\" generates binary files in a specific directory called ./bin/<BUILD-CONFIGURATION>/<TFM>/publish/.\n"
                                                    + "# See https://docs.microsoft.com/en-us/dotnet/core/deploying/deploy-with-cli for more details.\n"
                                                    + "# We start by finding the publish directories, which we assume hold dll files.\n"
                                                    + "shopt -s globstar\n"
                                                    + "paths=()\n"
                                                    + "for i in **/publish/*.dll; do\n"
                                                    + "  dir=${i%/*}\n"
                                                    + "  echo ${dir}\n"
                                                    + "  paths=(${paths[@]} ${dir})\n"
                                                    + "done\n"
                                                    + "# Find the unique set of directories holding the dll files.\n"
                                                    + "eval uniquepaths=($(printf \"%s\\n\" \"${paths[@]}\" | sort -u))\n"
                                                    + "for i in \"${uniquepaths[@]}\"; do\n"
                                                    + "  echo $i\n"
                                                    + "done\n"
                                                    + "# For each publish dir, create a package.\n"
                                                    + "packages=()\n"
                                                    + "versions=()\n"
                                                    + "for path in \"${uniquepaths[@]}\"; do\n"
                                                    + "  # Get the directory name four deep, which is typically the project folder.\n"
                                                    + "  # The directory name is used to name the package.\n"
                                                    + "  dir=${path}/../../../..\n"
                                                    + "  parentdir=$(builtin cd $dir; pwd)\n"
                                                    + "  projectname=${parentdir##*/}\n"
                                                    + "  # Package the published files.\n"
                                                    + "  octo pack \\\n"
                                                    + "  --basePath ${path} \\\n"
                                                    + "  --id ${projectname} \\\n"
                                                    + "  --version ${{ steps.determine_version.outputs.semVer }} \\\n"
                                                    + "  --format zip \\\n"
                                                    + "  --overwrite\n"
                                                    + "  packages=(${packages[@]} \"${projectname}.${{ steps.determine_version.outputs.semVer }}.zip\")\n"
                                                    + "  versions=(${versions[@]} \"${projectname}:${{ steps.determine_version.outputs.semVer }}\")\n"
                                                    + "done\n"
                                                    + "# We now need to output the list of generated packages so subsequent steps can access them.\n"
                                                    + "# We create multiple output variables with line and comma separated vales to support the inputs of subsequent steps.\n"
                                                    + "# Join the array with commas.\n"
                                                    + "printf -v joined \"%s,\" \"${packages[@]}\"\n"
                                                    + "# Save the list of packages as an output variable\n"
                                                    + "echo \"::set-output name=artifacts::${joined%,}\"\n"
                                                    + "# Do the same again, but use new lines as the separator. These will be used when uploading packages to the GitHub release.\n"
                                                    + "printf -v joinednewline \"%s\\n\" \"${packages[@]}\"\n"
                                                    + "# https://trstringer.com/github-actions-multiline-strings/\n"
                                                    + "# Multiline strings require some care in a workflow.\n"
                                                    + "joinednewline=\"${joinednewline//'%'/'%25'}\"\n"
                                                    + "joinednewline=\"${joinednewline//$'\\n'/'%0A'}\"\n"
                                                    + "joinednewline=\"${joinednewline//$'\\r'/'%0D'}\"\n"
                                                    + "# Now build a new line separated list of versions. These will be used when creating an Octopus release.\n"
                                                    + "printf -v versionsjoinednewline \"%s\\n\" \"${versions[@]}\"\n"
                                                    + "versionsjoinednewline=\"${versionsjoinednewline//'%'/'%25'}\"\n"
                                                    + "versionsjoinednewline=\"${versionsjoinednewline//$'\\n'/'%0A'}\"\n"
                                                    + "versionsjoinednewline=\"${versionsjoinednewline//$'\\r'/'%0D'}\"\n"
                                                    + "# Save the list of packages newline separated as an output variable.\n"
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
                                        .add(GIT_BUILDER.createOctopusRelease(accessor,
                                            "${{ steps.package.outputs.versions_new_line }}"))
                                        .build())
                                .build())
                        .build())
                .build());
  }

  private List<String> hasSolutionFiles(@NonNull final RepoClient accessor) {
    final List<String> files = accessor.getWildcardFiles("**/*.sln", 1).getOrElse(List.of());
    LOG.log(DEBUG, "Found " + files.size() + " solution files");
    files.forEach(s -> LOG.log(DEBUG, "  " + s));
    return files;
  }

  private boolean hasDotNetCoreProjectFiles(@NonNull final RepoClient accessor) {
    final List<String> projectFiles = accessor.getWildcardFiles("**/*.csproj", 1)
        .getOrElse(List.of());
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

  private void setWorkingDir(final List<String> solutionFiles) {
    final List<String> split = new ArrayList<>(Arrays.asList(solutionFiles.get(0).split("/")));
    if (split.size() > 1) {
      split.remove(split.size() - 1);
      workingDirectory = String.join("/", split);
    } else {
      workingDirectory = null;
    }
  }
}
