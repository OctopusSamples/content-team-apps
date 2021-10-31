package com.octopus.builders.java;

import static org.jboss.logging.Logger.Level.DEBUG;

import com.google.common.collect.ImmutableList;
import com.octopus.builders.PipelineBuilder;
import com.octopus.dsl.ArgType;
import com.octopus.dsl.Argument;
import com.octopus.dsl.Comment;
import com.octopus.dsl.Element;
import com.octopus.dsl.Function1Arg;
import com.octopus.dsl.Function1ArgTrailingLambda;
import com.octopus.dsl.FunctionManyArgs;
import com.octopus.dsl.FunctionTrailingLambda;
import com.octopus.repoclients.RepoClient;
import java.util.Arrays;
import java.util.List;
import lombok.NonNull;
import org.jboss.logging.Logger;

/**
 * A pipeline builder for Gradle projects.
 */
public class JavaGradleBuilder implements PipelineBuilder {

  private static final Logger LOG = Logger.getLogger(JavaGradleBuilder.class.toString());
  private static final JavaGitBuilder GIT_BUILDER = new JavaGitBuilder();
  private static final String[] GRADLE_BUILD_FILES = {"build.gradle", "build.gradle.kts"};
  private static final String GRADLE_OUTPUT_DIR = "build/libs";
  private boolean usesWrapper = false;

  @Override
  public Boolean canBuild(@NonNull final RepoClient accessor) {
    LOG.log(DEBUG, "JavaGradleBuilder.canBuild(RepoClient)");

    if (Arrays.stream(GRADLE_BUILD_FILES).anyMatch(accessor::testFile)) {
      LOG.log(DEBUG, String.join(" or ", GRADLE_BUILD_FILES) + " was found");
      usesWrapper = usesWrapper(accessor);
      LOG.log(DEBUG, "Wrapper script was " + (usesWrapper ? "" : "not ") + "found");
      return true;
    }

    return false;
  }

  @Override
  public String generate(@NonNull final RepoClient accessor) {
    return FunctionTrailingLambda.builder()
        .name("pipeline")
        .children(new ImmutableList.Builder<Element>()
            .addAll(GIT_BUILDER.createTopComments())
            .add(Comment.builder()
                .content(
                    "* JUnit: https://plugins.jenkins.io/junit/\n"
                        + "* Gradle: https://plugins.jenkins.io/gradle/")
                .build())
            .add(GIT_BUILDER.createParameters(accessor))
            .add(FunctionTrailingLambda.builder()
                .name("tools")
                .children(createTools())
                .build())
            .add(Function1Arg.builder().name("agent").value("any").build())
            .add(FunctionTrailingLambda.builder()
                .name("stages")
                .children(new ImmutableList.Builder<Element>()
                    .add(GIT_BUILDER.createEnvironmentStage())
                    .add(GIT_BUILDER.createCheckoutStep(accessor))
                    .add(createDependenciesStep())
                    .add(createBuildStep())
                    .add(createTestStep())
                    .add(GIT_BUILDER.createDeployStep(GRADLE_OUTPUT_DIR, accessor))
                    .add(GIT_BUILDER.createDeployStage(accessor))
                    .build())
                .build())
            .build()
        )
        .build()
        .toString();
  }

  private Boolean usesWrapper(@NonNull final RepoClient accessor) {
    return accessor.testFile("gradlew");
  }

  private String gradleExecutable() {
    return usesWrapper ? "./gradlew" : "gradle";
  }

  private List<Element> createTools() {
    final ImmutableList.Builder<Element> list = new ImmutableList.Builder<Element>()
        .add(Function1Arg.builder().name("jdk").value("Java").build());

    if (!usesWrapper) {
      list.add(Function1Arg.builder().name("gradle").value("Gradle").build());
    }

    return list.build();
  }

  private Element createDependenciesStep() {
    return Function1ArgTrailingLambda.builder()
        .name("stage")
        .arg("Dependencies")
        .children(GIT_BUILDER.createStepsElement(new ImmutableList.Builder<Element>()
            .add(Comment.builder()
                .content(
                    "Save the dependencies that went into this build into an artifact. This allows you to review any builds for vulnerabilities later on.")
                .build())
            .add(FunctionManyArgs.builder()
                .name("sh")
                .args(new ImmutableList.Builder<Argument>()
                    .add(new Argument("script",
                        gradleExecutable() + " dependencies --console=plain > dependencies.txt",
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
            .build()))
        .build();
  }

  private Element createBuildStep() {
    return Function1ArgTrailingLambda.builder()
        .name("stage")
        .arg("Build")
        .children(GIT_BUILDER.createStepsElement(new ImmutableList.Builder<Element>()
            .addAll(GIT_BUILDER.createGitVersionSteps())
            .add(FunctionManyArgs.builder()
                .name("sh")
                .args(new ImmutableList.Builder<Argument>()
                    .add(new Argument("script",
                        gradleExecutable() + " clean assemble --console=plain", ArgType.STRING))
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
                    .add(new Argument("script",
                        gradleExecutable() + " check --console=plain || true",
                        ArgType.STRING))
                    .build())
                .build())
            .add(FunctionManyArgs.builder()
                .name("junit")
                .args(new ImmutableList.Builder<Argument>()
                    .add(new Argument("testResults", "build/test-results/**/*.xml", ArgType.STRING))
                    .add(new Argument("allowEmptyResults ", "true", ArgType.BOOLEAN))
                    .build())
                .build())
            .build()))
        .build();
  }
}
