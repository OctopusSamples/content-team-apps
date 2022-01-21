package com.octopus.builders;

import com.octopus.repoclients.RepoClient;

/**
 * This interface defines a pipeline builder. Each builder is responsible for detecting files in a
 * repo that indicate that it can build a suitable pipeline.
 */
public interface PipelineBuilder {

  /**
   * Determine if this builder can build a pipeline for the given repo.
   *
   * @return true if this builder can build a pipeline, and false otherwise
   */
  Boolean canBuild(RepoClient accessor);

  /**
   * Builds the pipeline from a given repo.
   *
   * @return The Jenkins pipeline generated from the repo
   */
  String generate(RepoClient accessor);

  /**
   * The builder priority. Higher number means this builder will be tested first.
   *
   * @return The builder priority
   */
  default Integer getPriority() {
    return 0;
  }
}
