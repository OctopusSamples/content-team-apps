package com.octopus.builders.dotnet;

import static org.jboss.logging.Logger.Level.DEBUG;

import com.google.common.collect.ImmutableList;
import com.octopus.builders.GitBuilder;
import com.octopus.builders.PipelineBuilder;
import com.octopus.dsl.ArgType;
import com.octopus.dsl.Argument;
import com.octopus.dsl.Comment;
import com.octopus.dsl.Element;
import com.octopus.dsl.Function1Arg;
import com.octopus.dsl.Function1ArgTrailingLambda;
import com.octopus.dsl.FunctionManyArgs;
import com.octopus.dsl.FunctionTrailingLambda;
import com.octopus.dsl.StringContent;
import com.octopus.repoclients.RepoClient;
import java.util.List;
import java.util.regex.Pattern;
import lombok.NonNull;
import org.jboss.logging.Logger;

/**
 * The pipeline builder for dotnet core apps.
 */
public class DotnetCoreBuilder implements PipelineBuilder {

  private static final Logger LOG = Logger.getLogger(DotnetCoreBuilder.class.toString());
  private static final GitBuilder GIT_BUILDER = new GitBuilder();
  private static final Pattern DOT_NET_CORE_REGEX = Pattern.compile(
      "Sdk\\s*=\\s*\"Microsoft\\.NET\\.Sdk\"");

  @Override
  public Boolean canBuild(@NonNull final RepoClient accessor) {
    LOG.log(DEBUG, "DotnetCoreBuilder.canBuild(RepoClient)");

    return hasSolutionFiles(accessor) && hasDotNetCoreProjectFiles(accessor);
  }

  private boolean hasDotNetCoreProjectFiles(@NonNull final RepoClient accessor) {
    final List<String> projectFiles = accessor.getWildcardFiles("**/*.csproj").getOrElse(List.of());
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

  private boolean hasSolutionFiles(@NonNull final RepoClient accessor) {
    final List<String> files = accessor.getWildcardFiles("*.sln").getOrElse(List.of());
    LOG.log(DEBUG, "Found " + files.size() + " solution files");
    files.forEach(s -> LOG.log(DEBUG, "  " + s));
    return !files.isEmpty();
  }

  @Override
  public String generate(@NonNull final RepoClient accessor) {
    return FunctionTrailingLambda.builder()
        .name("pipeline")
        .children(new ImmutableList.Builder<Element>()
            .addAll(GIT_BUILDER.createTopComments())
            .add(Comment.builder()
                .content(
                    "* MSTest: https://plugins.jenkins.io/mstest/")
                .build())
            .add(GIT_BUILDER.createParameters(accessor))
            .add(Function1Arg.builder().name("agent").value("any").build())
            .add(FunctionTrailingLambda.builder()
                .name("stages")
                .children(new ImmutableList.Builder<Element>()
                    .add(GIT_BUILDER.createEnvironmentStage())
                    .add(GIT_BUILDER.createCheckoutStep(accessor))
                    .add(createDependenciesStep())
                    .add(createBuildStep())
                    .add(createTestStep())
                    .add(createPublishStep())
                    .add(GIT_BUILDER.createDeployStage(accessor))
                    .build())
                .build())
            .build()
        )
        .build()
        .toString();
  }

  private Element createDependenciesStep() {
    return Function1ArgTrailingLambda.builder()
        .name("stage")
        .arg("Dependencies")
        .children(GIT_BUILDER.createStepsElement(new ImmutableList.Builder<Element>()
            .add(FunctionManyArgs.builder()
                .name("sh")
                .args(new ImmutableList.Builder<Argument>()
                    .add(new Argument("script",
                        "dotnet restore",
                        ArgType.STRING))
                    .build())
                .build())
            .add(Comment.builder()
                .content(
                    "Save the dependencies that went into this build into an artifact. This allows you to review any builds for vulnerabilities later on.")
                .build())
            .add(FunctionManyArgs.builder()
                .name("sh")
                .args(new ImmutableList.Builder<Argument>()
                    .add(new Argument("script",
                        "dotnet list package > dependencies.txt",
                        ArgType.STRING))
                    .build())
                .build())
            .add(FunctionManyArgs.builder()
                .name("archiveArtifacts")
                .args(new ImmutableList.Builder<Argument>()
                    .add(new Argument("artifacts", "dependencies.txt", ArgType.STRING))
                    .add(new Argument("fingerprint", "true", ArgType.BOOLEAN))
                    .build())
                .build())
            .add(Comment.builder()
                .content(
                    "List any dependency updates.")
                .build())
            .add(FunctionManyArgs.builder()
                .name("sh")
                .args(new ImmutableList.Builder<Argument>()
                    .add(new Argument("script",
                        "dotnet list package --outdated > dependencieupdates.txt",
                        ArgType.STRING))
                    .build())
                .build())
            .add(FunctionManyArgs.builder()
                .name("archiveArtifacts")
                .args(new ImmutableList.Builder<Argument>()
                    .add(new Argument("artifacts", "dependencieupdates.txt", ArgType.STRING))
                    .add(new Argument("fingerprint", "true", ArgType.BOOLEAN))
                    .build())
                .build())
            .build()))
        .build();
  }

  private Element createBuildStep() {
    return Function1ArgTrailingLambda.builder()
        .name("stage")
        .arg("Build")
        .children(GIT_BUILDER.createStepsElement(new ImmutableList.Builder<Element>()
            .add(FunctionManyArgs.builder()
                .name("sh")
                .args(new ImmutableList.Builder<Argument>()
                    .add(new Argument("script",
                        "dotnet build --configuration Release",
                        ArgType.STRING))
                    .add(new Argument("returnStdout", "true", ArgType.BOOLEAN))
                    .build())
                .build())
            .build()))
        .build();
  }

  private Element createTestStep() {
    return Function1ArgTrailingLambda.builder()
        .name("stage")
        .arg("Test")
        .children(GIT_BUILDER.createStepsElement(new ImmutableList.Builder<Element>()
            .add(FunctionManyArgs.builder()
                .name("sh")
                .args(new ImmutableList.Builder<Argument>()
                    .add(new Argument("script", "dotnet test -l:trx || true",
                        ArgType.STRING))
                    .build())
                .build())
            .add(FunctionManyArgs.builder()
                .name("mstest")
                .args(new ImmutableList.Builder<Argument>()
                    .add(new Argument("testResultsFile", "**/*.trx", ArgType.STRING))
                    .add(new Argument("failOnError", "false", ArgType.BOOLEAN))
                    .add(new Argument("keepLongStdio", "true", ArgType.BOOLEAN))
                    .build())
                .build())
            .build()))
        .build();
  }

  private Element createPublishStep() {
    return Function1ArgTrailingLambda.builder()
        .name("stage")
        .arg("Publish")
        .children(GIT_BUILDER.createStepsElement(new ImmutableList.Builder<Element>()
            .addAll(GIT_BUILDER.createGitVersionSteps())
            .add(FunctionManyArgs.builder()
                .name("sh")
                .args(new ImmutableList.Builder<Argument>()
                    .add(new Argument(
                        "script",
                        "dotnet publish --configuration Release /p:AssemblyVersion=${VERSION_SEMVER}",
                        ArgType.STRING))
                    .build())
                .build())
            .add(FunctionTrailingLambda.builder()
                .name("script")
                .children(new ImmutableList.Builder<Element>()
                    .add(StringContent.builder()
                        .content(
                            "// Find published DLL files.\n"
                                + "def files = findFiles(glob: '**/publish/*.dll')\n"
                                + "  .collect{it.path.substring(0, it.path.lastIndexOf(\"/\"))}\n"
                                + "  .unique(false)\n"
                                + "echo 'Found ' + files.size() + ' publish dirs'\n"
                                + "files.each{echo it}\n"
                                + "// Join the paths containing published application with colons.\n"
                                + "env.PUBLISH_PATHS = files.collect{it}.join(':')\n"
                                + "echo 'These paths are available from the PUBLISH_PATHS environment variable, separated by colons.'"
                        )
                        .build())
                    .build())
                .build())
            .add(FunctionTrailingLambda.builder()
                .name("script")
                .children(new ImmutableList.Builder<Element>()
                    .add(StringContent.builder()
                        .content("env.PUBLISH_PATHS.split(\":\").each {\n"
                            + "\tdef packageId = \"application\"\n"
                            + "\tdir(\"${env.WORKSPACE}/${it}/../../../..\") {\n"
                            + "\t\t def projFiles = findFiles(glob: '*.csproj')\n"
                            + "\t\t if (projFiles.size() != 0) packageId = projFiles[0].path.substring(0, projFiles[0].path.lastIndexOf(\".\"))\t\t\n"
                            + "\t}\n"
                            + "\tdir(\"${env.WORKSPACE}/${it}\") {\n"
                            + "\t\toctopusPack(\n"
                            + "\t\t\tadditionalArgs: '', \n"
                            + "\t\t\toutputPath : \"..\",\n"
                            + "\t\t\tincludePaths: \"**\",\n"
                            + "\t\t\toverwriteExisting: true, \n"
                            + "\t\t\tpackageFormat: 'zip', \n"
                            + "\t\t\tpackageId: packageId, \n"
                            + "\t\t\tpackageVersion: env.VERSION_SEMVER, \n"
                            + "\t\t\tsourcePath: '', \n"
                            + "\t\t\ttoolId: 'Default', \n"
                            + "\t\t\tverboseLogging: false)\n"
                            + "\t}\n"
                            + "\tdir(\"${env.WORKSPACE}/${it}/..\") {\n"
                            + "\t\tdef artifact = \"${pwd()}/${packageId}.${env.VERSION_SEMVER}.zip\"\n"
                            + "\t\tenv.ARTIFACTS = artifact + \":\" + env.ARTIFACTS\n"
                            + "\t\techo \"Generated artifact at ${artifact}\"\n"
                            + "\t}\n"
                            + "}\n"
                            + "echo \"Artifact paths have been saved in the ARTIFACTS environment variable\"")
                        .build())
                    .build())
                .build())
            .build()))
        .build();
  }
}
