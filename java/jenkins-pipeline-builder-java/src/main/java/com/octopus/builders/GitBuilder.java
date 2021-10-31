package com.octopus.builders;

import com.google.common.collect.ImmutableList;
import com.octopus.dsl.ArgType;
import com.octopus.dsl.Argument;
import com.octopus.dsl.Comment;
import com.octopus.dsl.Element;
import com.octopus.dsl.Function1ArgTrailingLambda;
import com.octopus.dsl.FunctionManyArgs;
import com.octopus.dsl.FunctionTrailingLambda;
import com.octopus.dsl.StringContent;
import com.octopus.repoclients.RepoClient;
import java.util.List;
import lombok.NonNull;

/**
 * The base class containing common functions to build shared parts of the pipeline.
 */
public class GitBuilder {

  protected static final String DEFAULT_APPLICATION = "application";

  /**
   * Creates the comments that appear at the top of the pipeline.
   *
   * @return A list of Comment elements.
   */
  public List<Element> createTopComments() {
    return new ImmutableList.Builder<Element>()
        .add(Comment.builder()
            .content(
                "This pipeline requires the following plugins:\n"
                    + "* Pipeline Utility Steps Plugin: https://wiki.jenkins.io/display/JENKINS/Pipeline+Utility+Steps+Plugin\n"
                    + "* Git: https://plugins.jenkins.io/git/\n"
                    + "* Workflow Aggregator: https://plugins.jenkins.io/workflow-aggregator/\n"
                    + "* Octopus Deploy: https://plugins.jenkins.io/octopusdeploy/")
            .build())
        .build();
  }

  /**
   * Displays some details about the environment.
   *
   * @return The stage element with a script displaying environment variables.
   */
  public Element createEnvironmentStage() {
    return Function1ArgTrailingLambda.builder()
        .name("stage")
        .arg("Environment")
        .children(createStepsElement(new ImmutableList.Builder<Element>()
            .add(StringContent.builder()
                .content("echo \"PATH = ${env.PATH}\"")
                .build())
            .build()))
        .build();
  }

  /**
   * Creates the steps to perform a git checkout.
   *
   * @param accessor The repo accessor defining the repo to checkout.
   * @return A list of Elements that build the checkout steps.
   */
  public Element createCheckoutStep(@NonNull final RepoClient accessor) {
    return Function1ArgTrailingLambda.builder().name("stage")
        .arg("Checkout")
        .children(createStepsElement(new ImmutableList.Builder<Element>()
            .add(Comment.builder()
                .content(
                    "If this pipeline is saved as a Jenkinsfile in a git repo, the checkout stage can be deleted as\n"
                        + "Jenkins will check out the code for you.")
                .build())
            .add(FunctionTrailingLambda.builder()
                .name("script")
                .children(new ImmutableList.Builder<Element>()
                    .add(StringContent.builder()
                        .content("/*\n"
                            + "  This is from the Jenkins \"Global Variable Reference\" documentation:\n"
                            + "  SCM-specific variables such as GIT_COMMIT are not automatically defined as environment variables; rather you can use the return value of the checkout step.\n"
                            + "*/\n"
                            + "def checkoutVars = checkout([$class: 'GitSCM', branches: [[name: '*/"
                            + accessor.getDefaultBranches().get(0)
                            + "']], userRemoteConfigs: [[url: '" + accessor.getRepoPath()
                            + "']]])\n"
                            + "env.GIT_URL = checkoutVars.GIT_URL\n"
                            + "env.GIT_COMMIT = checkoutVars.GIT_COMMIT\n"
                            + "env.GIT_BRANCH = checkoutVars.GIT_BRANCH"
                        )
                        .build())
                    .build())
                .build())
            .build())
        ).build();
  }

  /**
   * Creates the steps to process test results.
   *
   * @param files The junit files.
   * @return A list of Elements that build the checkout steps.
   */
  public Element createTestProcessStep(@NonNull final String files) {
    return FunctionTrailingLambda.builder().name("post")
        .children(new ImmutableList.Builder<Element>()
            .add(FunctionTrailingLambda.builder()
                .name("always")
                .children(new ImmutableList.Builder<Element>()
                    .add(FunctionManyArgs.builder()
                        .name("junit")
                        .args(new ImmutableList.Builder<Argument>()
                            .add(new Argument("testResults", files, ArgType.STRING))
                            .add(new Argument("allowEmptyResults ", "true", ArgType.BOOLEAN))
                            .build())
                        .build())
                    .build())
                .build())
            .build())
        .build();
  }

  /**
   * Creates a steps element holding the supplied children.
   *
   * @param children The child elements to place in the step.
   * @return A list with the single steps element.
   */
  public List<Element> createStepsElement(@NonNull final List<Element> children) {
    return new ImmutableList.Builder<Element>().add(
            FunctionTrailingLambda.builder()
                .name("steps")
                .children(children)
                .build())
        .build();

  }

  /**
   * Create the steps required to run gitversion and capture the results in environment vars.
   *
   * @return A list of steps executing and processing gitversion.
   */
  public List<Element> createGitVersionSteps() {
    return new ImmutableList.Builder<Element>()
        .add(Comment.builder()
            .content(
                "Gitversion is available from https://github.com/GitTools/GitVersion/releases.\n"
                    + "We attempt to run gitversion if the executable is available.")
            .build())
        .add(FunctionManyArgs.builder()
            .name("sh")
            .args(new ImmutableList.Builder<Argument>()
                .add(new Argument(
                    "script",
                    "which gitversion && gitversion /output buildserver || true",
                    ArgType.STRING))
                .build())
            .build())
        .add(Comment.builder()
            .content(
                "Capture the git version as an environment variable, or use a default version if gitversion wasn't available.\n"
                    + "https://gitversion.net/docs/reference/build-servers/jenkins")
            .build())
        .add(FunctionTrailingLambda.builder()
            .name("script")
            .children(new ImmutableList.Builder<Element>()
                .add(StringContent.builder()
                    .content(
                        "if (fileExists('gitversion.properties')) {\n"
                            + "  def props = readProperties file: 'gitversion.properties'\n"
                            + "  env.VERSION_SEMVER = props.GitVersion_SemVer\n"
                            + "  env.VERSION_BRANCHNAME = props.GitVersion_BranchName\n"
                            + "  env.VERSION_ASSEMBLYSEMVER = props.GitVersion_AssemblySemVer\n"
                            + "  env.VERSION_MAJORMINORPATCH = props.GitVersion_MajorMinorPatch\n"
                            + "  env.VERSION_SHA = props.GitVersion_Sha\n"
                            + "} else {\n"
                            + "  env.VERSION_SEMVER = \"1.0.0.\" + env.BUILD_NUMBER\n"
                            + "}"
                    )
                    .build())
                .build())
            .build())
        .build();
  }

  /**
   * Build the parameters block.
   *
   * @param accessor The git client.
   * @return The parameters block DSL element.
   */
  public Element createParameters(@NonNull final RepoClient accessor) {
    return FunctionTrailingLambda.builder().name("parameters")
        .children(new ImmutableList.Builder<Element>()
            .add(FunctionManyArgs.builder()
                .name("string")
                .args(new ImmutableList.Builder<Argument>()
                    .add(new Argument(
                        "defaultValue",
                        "Spaces-1",
                        ArgType.STRING))
                    .add(new Argument(
                        "description",
                        "",
                        ArgType.STRING))
                    .add(new Argument(
                        "name",
                        "SpaceId",
                        ArgType.STRING))
                    .add(new Argument(
                        "trim",
                        "true",
                        ArgType.BOOLEAN))
                    .build())
                .build())
            .add(FunctionManyArgs.builder()
                .name("string")
                .args(new ImmutableList.Builder<Argument>()
                    .add(new Argument(
                        "defaultValue",
                        accessor.getRepoName().getOrElse(DEFAULT_APPLICATION),
                        ArgType.STRING))
                    .add(new Argument(
                        "description",
                        "",
                        ArgType.STRING))
                    .add(new Argument(
                        "name",
                        "ProjectName",
                        ArgType.STRING))
                    .add(new Argument(
                        "trim",
                        "true",
                        ArgType.BOOLEAN))
                    .build())
                .build())
            .add(FunctionManyArgs.builder()
                .name("string")
                .args(new ImmutableList.Builder<Argument>()
                    .add(new Argument(
                        "defaultValue",
                        "Dev",
                        ArgType.STRING))
                    .add(new Argument(
                        "description",
                        "",
                        ArgType.STRING))
                    .add(new Argument(
                        "name",
                        "EnvironmentName",
                        ArgType.STRING))
                    .add(new Argument(
                        "trim",
                        "true",
                        ArgType.BOOLEAN))
                    .build())
                .build())
            .add(FunctionManyArgs.builder()
                .name("string")
                .args(new ImmutableList.Builder<Argument>()
                    .add(new Argument(
                        "defaultValue",
                        "Octopus",
                        ArgType.STRING))
                    .add(new Argument(
                        "description",
                        "",
                        ArgType.STRING))
                    .add(new Argument(
                        "name",
                        "ServerId",
                        ArgType.STRING))
                    .add(new Argument(
                        "trim",
                        "true",
                        ArgType.BOOLEAN))
                    .build())
                .build())
            .build())
        .build();
  }

  /**
   * Build the deploy stage.
   *
   * @param accessor The git client.
   * @return The deploy stage block DSL element.
   */
  public Element createDeployStage(@NonNull final RepoClient accessor) {
    return Function1ArgTrailingLambda.builder().name("stage")
        .arg("Deployment")
        .children(createStepsElement(new ImmutableList.Builder<Element>()
            .add(FunctionManyArgs.builder()
                .name("octopusPushPackage")
                .args(new ImmutableList.Builder<Argument>()
                    .add(new Argument(
                        "additionalArgs",
                        "",
                        ArgType.STRING))
                    .add(new Argument(
                        "packagePaths",
                        "env.ARTIFACTS.split(\":\").join(\"\\n\")",
                        ArgType.CODE))
                    .add(new Argument(
                        "overwriteMode",
                        "OverwriteExisting",
                        ArgType.STRING))
                    .add(new Argument(
                        "serverId",
                        "params.ServerId",
                        ArgType.CODE))
                    .add(new Argument(
                        "spaceId",
                        "params.SpaceId",
                        ArgType.CODE))
                    .add(new Argument(
                        "toolId",
                        "Default",
                        ArgType.STRING))
                    .build())
                .build())
            .add(FunctionManyArgs.builder()
                .name("octopusPushBuildInformation")
                .args(new ImmutableList.Builder<Argument>()
                    .add(new Argument(
                        "additionalArgs",
                        "",
                        ArgType.STRING))
                    .add(new Argument(
                        "commentParser",
                        "GitHub",
                        ArgType.STRING))
                    .add(new Argument(
                        "overwriteMode",
                        "OverwriteExisting",
                        ArgType.STRING))
                    .add(new Argument(
                        "packageId",
                        "env.ARTIFACTS.split(\":\")[0].substring(env.ARTIFACTS.split(\":\")[0].lastIndexOf(\"/\") + 1, env.ARTIFACTS.split(\":\")[0].length()).replaceAll(\"\\\\.\" + env.VERSION_SEMVER + \"\\\\..+\", \"\")",
                        ArgType.CODE))
                    .add(new Argument(
                        "packageVersion",
                        "env.VERSION_SEMVER",
                        ArgType.CODE))
                    .add(new Argument(
                        "serverId",
                        "params.ServerId",
                        ArgType.CODE))
                    .add(new Argument(
                        "spaceId",
                        "params.SpaceId",
                        ArgType.CODE))
                    .add(new Argument(
                        "toolId",
                        "Default",
                        ArgType.STRING))
                    .add(new Argument(
                        "verboseLogging",
                        "false",
                        ArgType.BOOLEAN))
                    .add(new Argument(
                        "gitUrl",
                        "env.GIT_URL",
                        ArgType.CODE))
                    .add(new Argument(
                        "gitCommit",
                        "env.GIT_COMMIT",
                        ArgType.CODE))
                    .add(new Argument(
                        "gitBranch",
                        "env.GIT_BRANCH",
                        ArgType.CODE))
                    .build())
                .build())
            .add(FunctionManyArgs.builder()
                .name("octopusCreateRelease")
                .args(new ImmutableList.Builder<Argument>()
                    .add(new Argument(
                        "additionalArgs",
                        "",
                        ArgType.STRING))
                    .add(new Argument(
                        "cancelOnTimeout",
                        "false",
                        ArgType.BOOLEAN))
                    .add(new Argument(
                        "channel",
                        "",
                        ArgType.STRING))
                    .add(new Argument(
                        "defaultPackageVersion",
                        "",
                        ArgType.STRING))
                    .add(new Argument(
                        "deployThisRelease",
                        "false",
                        ArgType.BOOLEAN))
                    .add(new Argument(
                        "deploymentTimeout",
                        "",
                        ArgType.STRING))
                    .add(new Argument(
                        "environment",
                        "params.EnvironmentName",
                        ArgType.CODE))
                    .add(new Argument(
                        "jenkinsUrlLinkback",
                        "false",
                        ArgType.BOOLEAN))
                    .add(new Argument(
                        "project",
                        "params.ProjectName",
                        ArgType.CODE))
                    .add(new Argument(
                        "releaseNotes",
                        "false",
                        ArgType.BOOLEAN))
                    .add(new Argument(
                        "releaseNotesFile",
                        "",
                        ArgType.STRING))
                    .add(new Argument(
                        "releaseVersion",
                        "env.VERSION_SEMVER",
                        ArgType.CODE))
                    .add(new Argument(
                        "serverId",
                        "params.ServerId",
                        ArgType.CODE))
                    .add(new Argument(
                        "spaceId",
                        "params.SpaceId",
                        ArgType.CODE))
                    .add(new Argument(
                        "tenant",
                        "",
                        ArgType.STRING))
                    .add(new Argument(
                        "tenantTag",
                        "",
                        ArgType.STRING))
                    .add(new Argument(
                        "toolId",
                        "Default",
                        ArgType.STRING))
                    .add(new Argument(
                        "verboseLogging",
                        "false",
                        ArgType.BOOLEAN))
                    .add(new Argument(
                        "waitForDeployment",
                        "false",
                        ArgType.BOOLEAN))
                    .build())
                .build())
            .add(FunctionManyArgs.builder()
                .name("octopusDeployRelease")
                .args(new ImmutableList.Builder<Argument>()
                    .add(new Argument(
                        "cancelOnTimeout",
                        "false",
                        ArgType.BOOLEAN))
                    .add(new Argument(
                        "deploymentTimeout",
                        "",
                        ArgType.STRING))
                    .add(new Argument(
                        "environment",
                        "params.EnvironmentName",
                        ArgType.CODE))
                    .add(new Argument(
                        "project",
                        "params.ProjectName",
                        ArgType.CODE))
                    .add(new Argument(
                        "releaseVersion",
                        "env.VERSION_SEMVER",
                        ArgType.CODE))
                    .add(new Argument(
                        "serverId",
                        "params.ServerId",
                        ArgType.CODE))
                    .add(new Argument(
                        "spaceId",
                        "params.SpaceId",
                        ArgType.CODE))
                    .add(new Argument(
                        "tenant",
                        "",
                        ArgType.STRING))
                    .add(new Argument(
                        "tenantTag",
                        "",
                        ArgType.STRING))
                    .add(new Argument(
                        "toolId",
                        "Default",
                        ArgType.STRING))
                    .add(new Argument(
                        "variables",
                        "",
                        ArgType.STRING))
                    .add(new Argument(
                        "verboseLogging",
                        "false",
                        ArgType.BOOLEAN))
                    .add(new Argument(
                        "waitForDeployment",
                        "true",
                        ArgType.BOOLEAN))
                    .build())
                .build())
            .build()))
        .build();
  }
}
