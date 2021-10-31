package com.octopus.githubactions.builders.java;

import com.google.common.collect.ImmutableList;
import com.octopus.githubactions.builders.SnakeYamlFactory;
import com.octopus.githubactions.builders.dsl.Build;
import com.octopus.githubactions.builders.dsl.Jobs;
import com.octopus.githubactions.builders.dsl.On;
import com.octopus.githubactions.builders.dsl.Push;
import com.octopus.githubactions.builders.dsl.Step;
import com.octopus.githubactions.builders.dsl.UsesStep;
import com.octopus.githubactions.builders.dsl.Workflow;
import com.octopus.githubactions.builders.dsl.WorkflowDispatch;
import com.octopus.jenkins.builders.PipelineBuilder;
import com.octopus.repoclients.RepoClient;
import lombok.NonNull;
import org.jboss.logging.Logger;

/**
 * Builds a GitHub Actions Workflow for Maven projects.
 */
public class JavaMavenBuilder implements PipelineBuilder {

  private static final Logger LOG = Logger.getLogger(JavaMavenBuilder.class.toString());
  private boolean usesWrapper = false;

  @Override
  public Boolean canBuild(@NonNull final RepoClient accessor) {
    if (accessor.testFile("pom.xml")) {
      usesWrapper = usesWrapper(accessor);
      return true;
    }

    return false;
  }

  @Override
  public String generate(@NonNull final RepoClient accessor) {
    return SnakeYamlFactory.getConfiguredYaml().dump(
        Workflow.builder()
            .name("Java Maven Build")
            .on(On.builder()
                .push(new Push())
                .workflowDispatch(new WorkflowDispatch())
                .build()
            )
            .jobs(Jobs.builder()
                .build(Build.builder()
                    .runsOn("ubuntu-latest")
                    .steps(new ImmutableList.Builder<Step>()
                        .add(UsesStep.builder().uses("actions/checkout@v1").build())
                        .build())
                    .build())
                .build())
            .build());
  }

  private boolean usesWrapper(@NonNull final RepoClient accessor) {
    return accessor.testFile("mvnw");
  }

}
