package com.octopus.githubactions.builders.java;

import static org.jboss.logging.Logger.Level.DEBUG;

import com.google.common.collect.ImmutableList;
import com.octopus.builders.PipelineBuilder;
import com.octopus.githubactions.builders.GitBuilder;
import com.octopus.githubactions.builders.SnakeYamlFactory;
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

/** Builds a GitHub Actions Workflow for Maven projects. */
public class JavaMavenBuilder implements PipelineBuilder {

  private static final Logger LOG = Logger.getLogger(JavaMavenBuilder.class.toString());
  private static final GitBuilder GIT_BUILDER = new GitBuilder();
  private boolean usesWrapper = false;

  @Override
  public Boolean canBuild(@NonNull final RepoClient accessor) {
    LOG.log(DEBUG, "JavaMavenBuilder.canBuild(RepoClient)");
    if (accessor.testFile("pom.xml")) {
      usesWrapper = usesWrapper(accessor);
      return true;
    }

    return false;
  }

  @Override
  public String generate(@NonNull final RepoClient accessor) {
    LOG.log(DEBUG, "JavaMavenBuilder.generate(RepoClient)");
    return SnakeYamlFactory.getConfiguredYaml()
        .dump(
            Workflow.builder()
                .name("Java Maven Build")
                .on(On.builder().push(new Push()).workflowDispatch(new WorkflowDispatch()).build())
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
                                        .add(GIT_BUILDER.installJava())
                                        .add(
                                            RunStep.builder()
                                                .name("Set Version")
                                                .shell("bash")
                                                .run(
                                                    mavenExecutable()
                                                        + " --batch-mode versions:set -DnewVersion=${{ steps.determine_version.outputs.semVer }}")
                                                .build())
                                        .add(
                                            RunStep.builder()
                                                .name("List Dependencies")
                                                .shell("bash")
                                                .run(
                                                    mavenExecutable()
                                                        + " --batch-mode dependency:tree --no-transfer-progress > dependencies.txt")
                                                .build())
                                        .add(GIT_BUILDER.collectDependencies())
                                        .add(
                                            RunStep.builder()
                                                .name("List Dependency Updates")
                                                .shell("bash")
                                                .run(
                                                    mavenExecutable()
                                                        + " --batch-mode versions:display-dependency-updates > dependencyUpdates.txt")
                                                .build())
                                        .add(GIT_BUILDER.collectDependencyUpdates())
                                        .add(
                                            RunStep.builder()
                                                .name("Test")
                                                .shell("bash")
                                                .run(
                                                    mavenExecutable()
                                                        + " --batch-mode -Dmaven.test.failure.ignore=true test")
                                                .build())
                                        .add(
                                            GIT_BUILDER.buildJunitReport(
                                                "target/surefire-reports/*.xml"))
                                        .add(
                                            RunStep.builder()
                                                .name("Package")
                                                .shell("bash")
                                                .run(
                                                    mavenExecutable()
                                                        + " --batch-mode -DskipTests=true package")
                                                .build())
                                        .add(
                                            RunStep.builder()
                                                .name("Get Artifact Path")
                                                .id("get_artifact")
                                                .shell("bash")
                                                .run(
                                                    "# Find the largest WAR or JAR, and assume that was what we intended to build.\n"
                                                        + "echo \"::set-output name=artifact::$(find target -type f \\( -iname \\*.jar -o -iname \\*.war \\) -printf \"%p\\n\" | sort -n | head -1)\"")
                                                .build())
                                        .add(
                                            RunStep.builder()
                                                .name("Get Artifact Name")
                                                .id("get_artifact_name")
                                                .shell("bash")
                                                .run(
                                                    "path=\"${{ steps.get_artifact.outputs.artifact }}\"; echo \"::set-output name=artifact::${path##*/}\"")
                                                .build())
                                        .add(GIT_BUILDER.createGitHubRelease())
                                        .add(GIT_BUILDER.uploadToGitHubRelease())
                                        .add(
                                            RunStep.builder()
                                                .name("Create Octopus Artifact")
                                                .id("get_octopus_artifact")
                                                .shell("bash")
                                                .run(
                                                    "file=\"${{ steps.get_artifact.outputs.artifact }}\"\nextension=\"${file##*.}\"\noctofile=\""
                                                        + accessor
                                                            .getRepoName()
                                                            .getOrElse("application")
                                                        + ".${{ steps.determine_version.outputs.semVer }}.${extension}\"\ncp ${file} ${octofile}\necho \"::set-output name=artifact::${octofile}\"\nls -la")
                                                .build())
                                        .add(
                                            GIT_BUILDER.pushToOctopus(
                                                "${{ steps.get_octopus_artifact.outputs.artifact }}"))
                                        .add(GIT_BUILDER.uploadOctopusBuildInfo(accessor))
                                        .add(GIT_BUILDER.createOctopusRelease(accessor))
                                        .build())
                                .build())
                        .build())
                .build());
  }

  private String mavenExecutable() {
    return usesWrapper ? "./mvnw" : "mvn";
  }

  private boolean usesWrapper(@NonNull final RepoClient accessor) {
    return accessor.testFile("mvnw");
  }
}
