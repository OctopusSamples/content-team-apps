package com.octopus.githubactions.builders;

import com.octopus.repoclients.RepoClient;
import com.octopus.test.repoclients.DotnetTestRepoClient;
import com.octopus.test.repoclients.MavenTestRepoClient;
import com.octopus.test.repoclients.NodeTestRepoClient;
import com.octopus.test.repoclients.PhpTestRepoClient;
import org.junit.jupiter.api.Test;

public class PrintWorkflow {

  @Test
  public void printMavenWorkflow() {
    final JavaMavenBuilder builder = new JavaMavenBuilder();
    final RepoClient client =
        new MavenTestRepoClient("https://github.com/OctopusSamples/RandomQuotes-Java", true);
    builder.canBuild(client);
    System.out.println(builder.generate(client));
  }

  @Test
  public void printNodeJsWorkflow() {
    final NodeJsBuilder builder = new NodeJsBuilder();
    final RepoClient client =
        new NodeTestRepoClient("https://github.com/OctopusSamples/RandomQuotes-Js");
    builder.canBuild(client);
    System.out.println(builder.generate(client));
  }

  @Test
  public void printPhpWorkflow() {
    final PhpComposerBuilder builder = new PhpComposerBuilder();
    final RepoClient client =
        new PhpTestRepoClient("https://github.com/OctopusSamples/RandomQuotes-PHP");
    builder.canBuild(client);
    System.out.println(builder.generate(client));
  }

  @Test
  public void printDotNetWorkflow() {
    final DotNetCoreBuilder builder = new DotNetCoreBuilder();
    final RepoClient client =
        new DotnetTestRepoClient("https://github.com/OctopusSamples/RandomQuotes");
    builder.canBuild(client);
    System.out.println(builder.generate(client));
  }
}
