package com.octopus.builders.ruby;

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
import io.vavr.control.Try;
import java.util.List;
import lombok.NonNull;
import org.jboss.logging.Logger;

/**
 * Ruby builder.
 */
public class RubyGemBuilder implements PipelineBuilder {

  private static final Logger LOG = Logger.getLogger(RubyGemBuilder.class.toString());
  private static final JavaGitBuilder GIT_BUILDER = new JavaGitBuilder();

  @Override
  public Boolean canBuild(@NonNull final RepoClient accessor) {
    return accessor.testFile("Gemfile");
  }

  @Override
  public String generate(@NonNull final RepoClient accessor) {
    return FunctionTrailingLambda.builder()
        .name("pipeline")
        .children(new ImmutableList.Builder<Element>()
            .addAll(GIT_BUILDER.createTopComments())
            .add(Comment.builder()
                .content(
                    "* JUnit: https://plugins.jenkins.io/junit/")
                .build())
            .add(GIT_BUILDER.createParameters(accessor))
            .add(Function1Arg.builder().name("agent").value("any").build())
            .add(FunctionTrailingLambda.builder()
                .name("stages")
                .children(new ImmutableList.Builder<Element>()
                    .add(GIT_BUILDER.createEnvironmentStage())
                    .add(GIT_BUILDER.createCheckoutStep(accessor))
                    .add(createDependenciesStep())
                    .add(createTestStep(accessor))
                    .add(createPackageStep(accessor))
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
                        "bundle install",
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
                        "gem dep > dependencies.txt",
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
                        "gem outdated > dependencieupdates.txt",
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

  private Element createTestStep(@NonNull final RepoClient accessor) {
    final Try<List<String>> files = accessor.getWildcardFiles("**/run-test.rb");
    if (files.isSuccess() && !files.get().isEmpty()) {
      return createTestUnitTestStep(files.get().get(0));
    }

    return createRspecTestStep();
  }

  private Element createTestUnitTestStep(@NonNull final String runner) {
    return Function1ArgTrailingLambda.builder()
        .name("stage")
        .arg("Test")
        .children(GIT_BUILDER.createStepsElement(new ImmutableList.Builder<Element>()
            .add(FunctionManyArgs.builder()
                .name("sh")
                .args(new ImmutableList.Builder<Argument>()
                    .add(new Argument(
                        "script",
                        "ruby " + runner,
                        ArgType.STRING))
                    .add(new Argument("returnStdout", "true", ArgType.BOOLEAN))
                    .build())
                .build())
            .build()))
        .build();
  }

  private Element createRspecTestStep() {
    return Function1ArgTrailingLambda.builder()
        .name("stage")
        .arg("Test")
        .children(GIT_BUILDER.createStepsElement(new ImmutableList.Builder<Element>()
            .add(FunctionManyArgs.builder()
                .name("sh")
                .args(new ImmutableList.Builder<Argument>()
                    .add(new Argument(
                        "script",
                        "gem install rspec_junit_formatter",
                        ArgType.STRING))
                    .add(new Argument("returnStdout", "true", ArgType.BOOLEAN))
                    .build())
                .build())
            .add(FunctionManyArgs.builder()
                .name("sh")
                .args(new ImmutableList.Builder<Argument>()
                    .add(new Argument(
                        "script",
                        "rspec --format RspecJunitFormatter --out results.xml || true",
                        ArgType.STRING))
                    .add(new Argument("returnStdout", "true", ArgType.BOOLEAN))
                    .build())
                .build())
            .add(FunctionManyArgs.builder()
                .name("junit")
                .args(new ImmutableList.Builder<Argument>()
                    .add(new Argument("testResults", "results.xml", ArgType.STRING))
                    .add(new Argument("allowEmptyResults ", "true", ArgType.BOOLEAN))
                    .build())
                .build())
            .build()))
        .build();
  }

  private Element createPackageStep(@NonNull final RepoClient accessor) {
    final Try<List<String>> gemSpecs = accessor.getWildcardFiles("*.gemspec");

    if (gemSpecs.isFailure() || gemSpecs.get().isEmpty()) {
      return createZipStep(accessor);
    }

    return createSetup(gemSpecs.get().get(0));
  }

  private Element createZipStep(@NonNull final RepoClient accessor) {
    return Function1ArgTrailingLambda.builder()
        .name("stage")
        .arg("Package")
        .children(GIT_BUILDER.createStepsElement(new ImmutableList.Builder<Element>()
            .addAll(GIT_BUILDER.createGitVersionSteps())
            .add(FunctionTrailingLambda.builder()
                .name("script")
                .children(new ImmutableList.Builder<Element>()
                    .add(StringContent.builder()
                        .content("octopusPack(\n"
                            + "\tadditionalArgs: '',\n"
                            + "\tsourcePath: '.',\n"
                            + "\toutputPath : \".\",\n"
                            + "\tincludePaths: \"**/*.rb\\n**/*.html\\n**/*.htm\\n**/*.css\\n**/*.js\\n**/*.min\\n**/*.map\\n**/*.sql\\n**/*.png\\n**/*.jpg\\n**/*.jpeg\\n**/*.gif\\n**/*.json\\n**/*.env\\n**/*.txt\\n**/Procfile\",\n"
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

  private Element createSetup(@NonNull final String gemspec) {
    return Function1ArgTrailingLambda.builder()
        .name("stage")
        .arg("Package")
        .children(GIT_BUILDER.createStepsElement(new ImmutableList.Builder<Element>()
            .add(FunctionManyArgs.builder()
                .name("sh")
                .args(new ImmutableList.Builder<Argument>()
                    .add(new Argument(
                        "script",
                        "gem build " + gemspec,
                        ArgType.STRING))
                    .add(new Argument("returnStdout", "true", ArgType.BOOLEAN))
                    .build())
                .build())
            .build()))
        .build();
  }
}
