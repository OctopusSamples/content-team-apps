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
import com.octopus.dsl.StringContent;
import com.octopus.repoclients.RepoClient;
import java.util.List;
import lombok.NonNull;
import org.jboss.logging.Logger;

/**
 * A pipeline builder for Maven projects.
 */
public class JavaMavenBuilder implements PipelineBuilder {

  private static final Logger LOG = Logger.getLogger(JavaMavenBuilder.class.toString());
  private static final JavaGitBuilder GIT_BUILDER = new JavaGitBuilder();
  private boolean usesWrapper = false;

  @Override
  public Boolean canBuild(@NonNull final RepoClient accessor) {
    LOG.log(DEBUG, "JavaMavenBuilder.canBuild(RepoClient)");

    if (accessor.testFile("pom.xml")) {
      LOG.log(DEBUG, "pom.xml file was found");
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
                        + "* Maven Integration: https://plugins.jenkins.io/maven-plugin/")
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
                    .add(createPackageStep())
                    .add(GIT_BUILDER.createDeployStep("target", accessor))
                    .add(GIT_BUILDER.createDeployStage(accessor))
                    .build())
                .build())
            .build()
        )
        .build()
        .toString();
  }

  private boolean usesWrapper(@NonNull final RepoClient accessor) {
    return accessor.testFile("mvnw");
  }

  private String mavenExecutable() {
    return usesWrapper ? "./mvnw" : "mvn";
  }

  private List<Element> createTools() {
    final ImmutableList.Builder<Element> list = new ImmutableList.Builder<Element>()
        .add(Function1Arg.builder().name("jdk").value("Java").build());

    if (!usesWrapper) {
      list.add(Function1Arg.builder().name("maven").value("Maven").build());
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
                    "Download the dependencies and plugins before we attempt to do any further actions")
                .build())
            .add(FunctionManyArgs.builder()
                .name("sh")
                .args(new ImmutableList.Builder<Argument>()
                    .add(new Argument("script", mavenExecutable()
                        + " --batch-mode dependency:resolve-plugins dependency:go-offline",
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
                        mavenExecutable() + " --batch-mode dependency:tree > dependencies.txt",
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
                    .add(new Argument("script", mavenExecutable()
                        + " --batch-mode versions:display-dependency-updates > dependencieupdates.txt",
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
            .add(Comment.builder()
                .content("Set the build number on the generated artifact.")
                .build())
            .add(Function1Arg.builder()
                .name("sh")
                .value(mavenExecutable()
                    + " --batch-mode build-helper:parse-version versions:set \\\n"
                    + "-DnewVersion=\\\\${parsedVersion.majorVersion}.\\\\${parsedVersion.minorVersion}.\\\\${parsedVersion.incrementalVersion}.${BUILD_NUMBER}")
                .build())
            .add(FunctionManyArgs.builder()
                .name("sh")
                .args(new ImmutableList.Builder<Argument>()
                    .add(new Argument("script", mavenExecutable() + " --batch-mode -DskipTests clean compile",
                        ArgType.STRING))
                    .add(new Argument("returnStdout", "true", ArgType.BOOLEAN))
                    .build())
                .build())
            .add(FunctionTrailingLambda.builder()
                .name("script")
                .children(new ImmutableList.Builder<Element>()
                    .add(StringContent.builder()
                        .content("env.VERSION_SEMVER = sh (script: '" + mavenExecutable()
                            + " -q -Dexec.executable=echo -Dexec.args=\\'${project.version}\\' --non-recursive exec:exec', returnStdout: true)\n"
                          + "env.VERSION_SEMVER = env.VERSION_SEMVER.trim()")
                        .build())
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
                        mavenExecutable() + " --batch-mode -Dmaven.test.failure.ignore=true test",
                        ArgType.STRING))
                    .build())
                .build())
            .add(FunctionManyArgs.builder()
                .name("junit")
                .args(new ImmutableList.Builder<Argument>()
                    .add(new Argument("testResults", "target/surefire-reports/*.xml",
                        ArgType.STRING))
                    .add(new Argument("allowEmptyResults ", "true", ArgType.BOOLEAN))
                    .build())
                .build())
            .build()))
        .build();
  }

  private Element createPackageStep() {
    return Function1ArgTrailingLambda.builder()
        .name("stage")
        .arg("Package")
        .children(GIT_BUILDER.createStepsElement(new ImmutableList.Builder<Element>()
            .add(FunctionManyArgs.builder()
                .name("sh")
                .args(new ImmutableList.Builder<Argument>()
                    .add(new Argument("script",
                        mavenExecutable() + " --batch-mode package -DskipTests", ArgType.STRING))
                    .build())
                .build())
            .build()))
        .build();
  }
}
