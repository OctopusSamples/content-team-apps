package com.octopus.builders.nodejs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.octopus.builders.PipelineBuilder;
import com.octopus.builders.java.JavaGitBuilder;
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
import java.util.Map;
import lombok.NonNull;
import org.jboss.logging.Logger;

/**
 * Node JS builder.
 */
public class NodejsBuilder implements PipelineBuilder {

  private static final Logger LOG = Logger.getLogger(NodejsBuilder.class.toString());
  private static final JavaGitBuilder GIT_BUILDER = new JavaGitBuilder();
  private boolean useYarn = false;

  @Override
  public Boolean canBuild(@NonNull final RepoClient accessor) {
    useYarn = accessor.testFile("yarn.lock");
    return accessor.testFile("package.json");
  }

  @Override
  public String generate(@NonNull final RepoClient accessor) {
    return FunctionTrailingLambda.builder()
        .name("pipeline")
        .children(new ImmutableList.Builder<Element>()
            .addAll(GIT_BUILDER.createTopComments())
            .add(GIT_BUILDER.createParameters(accessor))
            .add(Function1Arg.builder().name("agent").value("any").build())
            .add(FunctionTrailingLambda.builder()
                .name("stages")
                .children(new ImmutableList.Builder<Element>()
                    .add(GIT_BUILDER.createEnvironmentStage())
                    .add(GIT_BUILDER.createCheckoutStep(accessor))
                    .add(createDependenciesStep())
                    .add(createTestStep())
                    .add(createBuildStep(accessor))
                    .add(createPackageStep(accessor))
                    .add(GIT_BUILDER.createDeployStage(accessor))
                    .build())
                .build())
            .build()
        )
        .build()
        .toString();
  }

  private String getPackageManager() {
    return useYarn ? "yarn" : "npm";
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
                        getPackageManager() + " install",
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
                        getPackageManager() + " list --all > dependencies.txt",
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
                .content("List any dependency updates.")
                .build())
            .add(FunctionManyArgs.builder()
                .name("sh")
                .args(new ImmutableList.Builder<Argument>()
                    .add(new Argument(
                        "script",
                        getPackageManager() + " outdated > dependencieupdates.txt || true",
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

  private Element createTestStep() {
    return Function1ArgTrailingLambda.builder()
        .name("stage")
        .arg("Test")
        .children(GIT_BUILDER.createStepsElement(new ImmutableList.Builder<Element>()
            .add(FunctionManyArgs.builder()
                .name("sh")
                .args(new ImmutableList.Builder<Argument>()
                    .add(new Argument(
                        "script",
                        getPackageManager() + " test || true",
                        ArgType.STRING))
                    .add(new Argument("returnStdout", "true", ArgType.BOOLEAN))
                    .build())
                .build())
            .build()))
        .build();
  }

  private Element createBuildStep(@NonNull final RepoClient accessor) {
    if (scriptExists(accessor, "build")) {
      return Function1ArgTrailingLambda.builder()
          .name("stage")
          .arg("Build")
          .children(GIT_BUILDER.createStepsElement(new ImmutableList.Builder<Element>()
              .add(FunctionManyArgs.builder()
                  .name("sh")
                  .args(new ImmutableList.Builder<Argument>()
                      .add(new Argument(
                          "script",
                          getPackageManager() + " run build",
                          ArgType.STRING))
                      .add(new Argument("returnStdout", "true", ArgType.BOOLEAN))
                      .build())
                  .build())
              .build()))
          .build();
    }

    return Element.builder().build();
  }

  private boolean scriptExists(@NonNull final RepoClient accessor, @NonNull final String script) {
    return accessor.getFile("package.json")
        .mapTry(j -> new ObjectMapper().readValue(j, Map.class))
        .mapTry(m -> (Map) (m.get("scripts")))
        .mapTry(s -> s.containsKey(script))
        .getOrElse(false);
  }

  private Element createPackageStep(@NonNull final RepoClient accessor) {
    return Function1ArgTrailingLambda.builder()
        .name("stage")
        .arg("Package")
        .children(GIT_BUILDER.createStepsElement(new ImmutableList.Builder<Element>()
            .addAll(GIT_BUILDER.createGitVersionSteps())
            .add(FunctionTrailingLambda.builder()
                .name("script")
                .children(new ImmutableList.Builder<Element>()
                    .add(StringContent.builder()
                        .content("def sourcePath = \".\"\n"
                            + "def outputPath = \".\"\n"
                            + "\n"
                            + "if (fileExists(\"build\")) {\n"
                            + "\tsourcePath = \"build\"\n"
                            + "\toutputPath = \"..\"\n"
                            + "}\n"
                            + "\n"
                            + "octopusPack(\n"
                            + "\tadditionalArgs: '',\n"
                            + "\tsourcePath: sourcePath,\n"
                            + "\toutputPath : outputPath,\n"
                            + "\tincludePaths: \"**/*.html\\n**/*.htm\\n**/*.css\\n**/*.js\\n**/*.min\\n**/*.map\\n**/*.sql\\n**/*.png\\n**/*.jpg\\n**/*.jpeg\\n**/*.gif\\n**/*.json\\n**/*.env\\n**/*.txt\\n**/Procfile\",\n"
                            + "\toverwriteExisting: true, \n"
                            + "\tpackageFormat: 'zip', \n"
                            + "\tpackageId: '" + accessor.getRepoName().getOrElse("application")
                            + "', \n"
                            + "\tpackageVersion: env.VERSION_SEMVER, \n"
                            + "\ttoolId: 'Default', \n"
                            + "\tverboseLogging: false)\n"
                            + "env.ARTIFACTS = \"" + accessor.getRepoName().getOrElse("application")
                            + ".${env.VERSION_SEMVER}.zip\"")
                        .build())
                    .build())
                .build())
            .build()))
        .build();
  }
}
