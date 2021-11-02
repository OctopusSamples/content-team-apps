package com.octopus.githubactions.builders;

import com.octopus.githubactions.builders.java.JavaMavenBuilder;
import com.octopus.repoclients.RepoClient;
import com.octopus.test.repoclients.MavenTestRepoClient;
import org.junit.jupiter.api.Test;

public class PrintWorkflow {

  @Test
  public void printMavenWorkflow() {
    final JavaMavenBuilder builder = new JavaMavenBuilder();
    final RepoClient client = new MavenTestRepoClient("https://github.com/OctopusSamples/RandomQuotes-Java", true);
    builder.canBuild(client);
    System.out.println(builder.generate(client));
  }
}
