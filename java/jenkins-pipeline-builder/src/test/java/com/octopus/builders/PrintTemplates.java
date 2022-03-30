package com.octopus.builders;

import com.octopus.jenkins.shared.builders.dotnet.DotnetCoreBuilder;
import com.octopus.jenkins.shared.builders.java.JavaGradleBuilder;
import com.octopus.jenkins.shared.builders.java.JavaMavenBuilder;
import com.octopus.jenkins.shared.builders.nodejs.NodejsBuilder;
import com.octopus.jenkins.shared.builders.php.PhpComposerBuilder;
import com.octopus.test.repoclients.DotnetTestRepoClient;
import com.octopus.test.repoclients.GradleTestRepoClient;
import com.octopus.test.repoclients.MavenTestRepoClient;
import com.octopus.test.repoclients.NodeTestRepoClient;
import com.octopus.test.repoclients.PhpTestRepoClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * These tests are just a convenient way to print the templates for manual testing in a Jenkins
 * instance.
 */
public class PrintTemplates {

  @Test
  public void printMavenTemplate() {
    final String template =
        new JavaMavenBuilder()
            .generate(new MavenTestRepoClient("https://github.com/OctopusSamples/RandomQuotes-Java", true));
    Assertions.assertNotEquals("", template);
    System.out.println(template);
  }

  @Test
  public void printGradleTemplate() {
    final String template =
        new JavaGradleBuilder()
            .generate(new GradleTestRepoClient("https://github.com/OctopusSamples/RandomQuotes-Java", true));
    Assertions.assertNotEquals("", template);
    System.out.println(template);
  }

  @Test
  public void printDotNetTemplate() {
    final String template =
        new DotnetCoreBuilder()
            .generate(new DotnetTestRepoClient("https://github.com/OctopusSamples/RandomQuotes"));
    Assertions.assertNotEquals("", template);
    System.out.println(template);
  }

  @Test
  public void printNodejsTemplate() {
    final String template =
        new NodejsBuilder()
            .generate(new NodeTestRepoClient("https://github.com/OctopusSamples/RandomQuotes-js"));
    Assertions.assertNotEquals("", template);
    System.out.println(template);
  }

  @Test
  public void printPhpTemplate() {
    final String template =
        new PhpComposerBuilder()
            .generate(new PhpTestRepoClient("https://github.com/OctopusSamples/RandomQuotes-PHP"));
    Assertions.assertNotEquals("", template);
    System.out.println(template);
  }

}
