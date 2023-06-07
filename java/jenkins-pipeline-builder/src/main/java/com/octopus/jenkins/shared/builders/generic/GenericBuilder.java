package com.octopus.jenkins.shared.builders.generic;

import com.google.common.collect.ImmutableList;
import com.octopus.builders.PipelineBuilder;
import com.octopus.jenkins.shared.builders.java.JavaGitBuilder;
import com.octopus.jenkins.shared.dsl.Element;
import com.octopus.jenkins.shared.dsl.Function1Arg;
import com.octopus.jenkins.shared.dsl.Function1ArgTrailingLambda;
import com.octopus.jenkins.shared.dsl.FunctionTrailingLambda;
import com.octopus.jenkins.shared.dsl.StringContent;
import com.octopus.repoclients.RepoClient;
import lombok.NonNull;
import org.jboss.logging.Logger;

/**
 * Generic builder, useful for static apps.
 */
public class GenericBuilder implements PipelineBuilder {

  private static final Logger LOG = Logger.getLogger(GenericBuilder.class.toString());
  private static final JavaGitBuilder GIT_BUILDER = new JavaGitBuilder();

  @Override
  public String getName() {
    return "Generic";
  }

  @Override
  public Integer getPriority() {
    return -100;
  }

  @Override
  public Boolean canBuild(@NonNull final RepoClient accessor) {
    return true;
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
                    .add(GIT_BUILDER.createWorkspaceCleanupStep())
                    .add(GIT_BUILDER.createCheckoutStep(accessor))
                    .add(createPackageStep(accessor))
                    .add(GIT_BUILDER.createDeployStage(accessor))
                    .build())
                .build())
            .build()
        )
        .build()
        .toString();
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
