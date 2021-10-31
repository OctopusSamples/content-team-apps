package com.octopus.githubactions.builders.java;

import com.octopus.jenkins.builders.PipelineBuilder;
import com.octopus.repoclients.RepoClient;
import lombok.NonNull;
import org.jboss.logging.Logger;

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
    return null;
  }

  private boolean usesWrapper(@NonNull final RepoClient accessor) {
    return accessor.testFile("mvnw");
  }

}
