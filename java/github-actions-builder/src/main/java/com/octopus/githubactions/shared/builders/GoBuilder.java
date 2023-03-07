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
import io.vavr.control.Try;
import java.util.List;
import lombok.NonNull;
import org.jboss.logging.Logger;

/**
 * Builds a GitHub Actions Workflow for Go projects.
 */
public class GoBuilder implements PipelineBuilder {

  private static final Logger LOG = Logger.getLogger(GoBuilder.class.toString());
  private static final GitBuilder GIT_BUILDER = new GitBuilder();

  @Override
  public String getName() {
    return "Go";
  }

  @Override
  public Boolean canBuild(@NonNull final RepoClient accessor) {
    final Try<List<String>> files = accessor.getWildcardFiles("*.go", 1);

    return accessor.testFile("go.mod")
        || (files.isSuccess() && !files.get().isEmpty());
  }

  @Override
  public String generate(@NonNull final RepoClient accessor) {
    LOG.log(DEBUG, "GoBuilder.generate(RepoClient)");
    return GIT_BUILDER.getInitialComments() + "\n"
        + SnakeYamlFactory.getConfiguredYaml()
        .dump(
            Workflow.builder()
                .permissions(GIT_BUILDER.buildPermissions())
                .name("Go Build")
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
                                            .name("Set up Go")
                                            .uses("actions/setup-go@v2")
                                            .with(
                                                new ImmutableMap.Builder<String, String>()
                                                    .put("go-version", "^1.17")
                                                    .build())
                                            .build())
                                        .add(GIT_BUILDER.gitVersionInstallStep())
                                        .add(GIT_BUILDER.getVersionCalculate())
                                        .add(GIT_BUILDER.installOctopusCli())
                                        .add(
                                            RunStep.builder()
                                                .name("Install Dependencies")
                                                .shell("bash")
                                                .run("go get ./...")
                                                .build())
                                        .add(
                                            RunStep.builder()
                                                .name("List Dependencies")
                                                .shell("bash")
                                                .run("go list -m all > dependencies.txt")
                                                .build())
                                        .add(GIT_BUILDER.collectDependencies())
                                        .add(
                                            RunStep.builder()
                                                .name("List Dependency Updates")
                                                .shell("bash")
                                                .run(
                                                    "go list -u -m -f \"{{if .Update}}{{.}}{{end}}\" all > dependencyUpdates.txt")
                                                .build())
                                        .add(GIT_BUILDER.collectDependencyUpdates())
                                        .add(
                                            RunStep.builder()
                                                .name("Test")
                                                .shell("bash")
                                                .run(
                                                    "go install gotest.tools/gotestsum@latest; gotestsum --junitfile results.xml")
                                                .build())
                                        .add(GIT_BUILDER.buildJunitReport("Go Tests",
                                            "results.xml"))
                                        .add(RunStep.builder().run("go build").build())
                                        .build())
                                .build())
                        .build())
                .build());
  }
}
