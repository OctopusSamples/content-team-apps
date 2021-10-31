package com.octopus.builders;

import com.google.common.io.Resources;
import com.octopus.builders.dotnet.DotnetCoreBuilder;
import com.octopus.builders.go.GoBuilder;
import com.octopus.builders.java.JavaGradleBuilder;
import com.octopus.builders.java.JavaMavenBuilder;
import com.octopus.builders.nodejs.NodejsBuilder;
import com.octopus.builders.php.PhpComposerBuilder;
import com.octopus.builders.python.PythonBuilder;
import com.octopus.builders.ruby.RubyGemBuilder;
import com.octopus.jenkinsclient.JenkinsClient;
import com.octopus.jenkinsclient.JenkinsDetails;
import com.octopus.octopusclient.OctopusClient;
import com.octopus.repoclients.DotnetTestRepoClient;
import com.octopus.repoclients.GoTestRepoClient;
import com.octopus.repoclients.GradleTestRepoClient;
import com.octopus.repoclients.MavenTestRepoClient;
import com.octopus.repoclients.NodeTestRepoClient;
import com.octopus.repoclients.PhpTestRepoClient;
import com.octopus.repoclients.PythonTestRepoClient;
import com.octopus.repoclients.RepoClient;
import com.octopus.repoclients.RubyTestRepoClient;
import io.vavr.control.Try;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.stream.Stream;
import lombok.NonNull;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.junit.Before;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

@Testcontainers
public class JavaMavenBuilderTest {

  private static final JavaMavenBuilder JAVA_MAVEN_BUILDER = new JavaMavenBuilder();
  private static final JavaGradleBuilder JAVA_GRADLE_BUILDER = new JavaGradleBuilder();
  private static final PythonBuilder PYTHON_BUILDER = new PythonBuilder();
  private static final PhpComposerBuilder PHP_COMPOSER_BUILDER = new PhpComposerBuilder();
  private static final NodejsBuilder NODEJS_NPM_BUILDER = new NodejsBuilder();
  private static final RubyGemBuilder RUBY_GEM_BUILDER = new RubyGemBuilder();
  private static final GoBuilder GO_BUILDER = new GoBuilder();
  private static final DotnetCoreBuilder DOTNET_CORE_BUILDER = new DotnetCoreBuilder();
  private static final PipelineBuilder[] PIPELINE_BUILDERS = {
      JAVA_MAVEN_BUILDER,
      JAVA_GRADLE_BUILDER,
      PYTHON_BUILDER,
      PHP_COMPOSER_BUILDER,
      NODEJS_NPM_BUILDER,
      RUBY_GEM_BUILDER,
      GO_BUILDER,
      DOTNET_CORE_BUILDER
  };
  private static final JenkinsClient JENKINS_CLIENT = new JenkinsClient();
  private static final OctopusClient OCTOPUS_CLIENT = new OctopusClient();
  private static final String RANDOM_DB_PASSWORD = RandomStringUtils.random(16, true, true);
  private static final String RANDOM_OCTO_PASSWORD = RandomStringUtils.random(16, true, true);
  private static final String RANDOM_OCTO_API = RandomStringUtils.random(32, true, true)
      .toUpperCase();
  private static final Network NETWORK = Network.newNetwork();


  @Container
  public GenericContainer mssql = new GenericContainer(
      DockerImageName.parse("mcr.microsoft.com/mssql/server"))
      .withNetwork(NETWORK)
      .withNetworkAliases("db")
      .withEnv("SA_PASSWORD", RANDOM_DB_PASSWORD)
      .withEnv("ACCEPT_EULA", "Y")
      .withStartupTimeout(Duration.ofMinutes(5));

  @Container
  public GenericContainer octopus = new GenericContainer(
      DockerImageName.parse("octopusdeploy/octopusdeploy"))
      .withNetwork(NETWORK)
      .withNetworkAliases("octopus")
      .withExposedPorts(8080)
      .withEnv("DB_CONNECTION_STRING",
          "Server=db,1433;Database=OctopusDeploy;User=sa;Password=" + RANDOM_DB_PASSWORD)
      .withEnv("CONNSTRING",
          "Server=db,1433;Database=OctopusDeploy;User=sa;Password=" + RANDOM_DB_PASSWORD)
      .withEnv("ADMIN_USERNAME", "admin")
      .withEnv("ADMIN_PASSWORD", RANDOM_OCTO_PASSWORD)
      .withEnv("ADMIN_EMAIL", "admin@example.org")
      .withEnv("ACCEPT_EULA", "Y")
      .withEnv("ADMIN_API_KEY", "API-" + RANDOM_OCTO_API)
      .withStartupTimeout(Duration.ofMinutes(15))
      .dependsOn(mssql);

  /**
   * A Jenkins container that has the appropriate plugins installed, an admin user setup, the
   * initial wizard disabled, and other customizations.
   */
  @Container
  public GenericContainer jenkins = new GenericContainer(new ImageFromDockerfile()
      .withDockerfileFromBuilder(builder ->
          builder
              .from("jenkins/jenkins:lts")
              .user("root")
              // install plugins
              .run("jenkins-plugin-cli --plugins "
                  + "pipeline-utility-steps:2.10.0 "
                  + "gradle:1.37.1 "
                  + "maven-plugin:3.13 "
                  + "jdk-tool:1.5 "
                  + "workflow-aggregator:2.6 "
                  + "git:4.8.2 "
                  + "msbuild:1.30 "
                  + "mstest:1.0.0 "
                  + "octopusdeploy:3.1.6")
              .run("apt-get update")
              // Install php, ruby, python
              .run(
                  "apt-get install dnsutils sed vim maven wget curl sudo python3 python3-pip ruby-full ruby-dev php7.4 php-cli php-zip php-dom php-mbstring unzip -y")
              // install bundler
              .run("gem install bundler")
              // let the jenkins user run sudo
              .run("echo \"jenkins ALL=(ALL) NOPASSWD: ALL\" >> /etc/sudoers")
              // install gradle
              .run("wget https://services.gradle.org/distributions/gradle-7.2-bin.zip")
              .run("unzip gradle-7.2-bin.zip")
              .run("mv gradle-7.2 /opt")
              .run("chmod +x /opt/gradle-7.2/bin/gradle")
              .run("ln -s /opt/gradle-7.2/bin/gradle /usr/bin/gradle")
              // install jdk 17
              .run("wget https://cdn.azul.com/zulu/bin/zulu17.28.13-ca-jdk17.0.0-linux_x64.tar.gz")
              .run("tar -xzf zulu17.28.13-ca-jdk17.0.0-linux_x64.tar.gz")
              .run("mv zulu17.28.13-ca-jdk17.0.0-linux_x64 /opt")
              // install dotnet
              .run(
                  "wget https://packages.microsoft.com/config/debian/11/packages-microsoft-prod.deb -O packages-microsoft-prod.deb")
              .run("dpkg -i packages-microsoft-prod.deb")
              .run(
                  "apt-get update; apt-get install -y apt-transport-https && apt-get update && apt-get install -y dotnet-sdk-5.0 dotnet-sdk-3.1")
              // install octocli
              .run(
                  "apt update && sudo apt install -y --no-install-recommends gnupg curl ca-certificates apt-transport-https && "
                      + "curl -sSfL https://apt.octopus.com/public.key | apt-key add - && "
                      + "sh -c \"echo deb https://apt.octopus.com/ stable main > /etc/apt/sources.list.d/octopus.com.list\" && "
                      + "apt update && sudo apt install -y octopuscli")
              // install nodejs
              .run("curl -fsSL https://deb.nodesource.com/setup_16.x | bash -")
              .run("apt-get install -y nodejs")
              // install yarn
              .run("curl -sS https://dl.yarnpkg.com/debian/pubkey.gpg | sudo apt-key add -")
              .run(
                  "echo \"deb https://dl.yarnpkg.com/debian/ stable main\" | sudo tee /etc/apt/sources.list.d/yarn.list")
              .run("apt update; apt install yarn")
              // install composer
              .run("wget -O composer-setup.php https://getcomposer.org/installer")
              .run("php composer-setup.php --install-dir=/usr/local/bin --filename=composer")
              // install golang
              .run("wget https://golang.org/dl/go1.17.1.linux-amd64.tar.gz")
              .run("rm -rf /usr/local/go && tar -C /usr/local -xzf go1.17.1.linux-amd64.tar.gz")
              .env("PATH", "/usr/local/go/bin:/root/go/bin:${PATH}")
              // install gitversion
              .run(
                  "wget https://github.com/GitTools/GitVersion/releases/download/5.7.0/gitversion-linux-x64-5.7.0.tar.gz")
              .run("mkdir /opt/gitversion")
              .run("tar -C /opt/gitversion -xzf gitversion-linux-x64-5.7.0.tar.gz")
              .run("chmod -R 755 /opt/gitversion")
              .env("PATH", "/opt/gitversion:${PATH}")
              .build()))
      .withCopyFileToContainer(MountableFile.forClasspathResource("jenkins/maven_tool.groovy"),
          "/usr/share/jenkins/ref/init.groovy.d/maven_tool.groovy")
      .withCopyFileToContainer(MountableFile.forClasspathResource("jenkins/gradle_tool.groovy"),
          "/usr/share/jenkins/ref/init.groovy.d/gradle_tool.groovy")
      .withCopyFileToContainer(MountableFile.forClasspathResource("jenkins/java_tool.groovy"),
          "/usr/share/jenkins/ref/init.groovy.d/java_tool.groovy")
      .withCopyFileToContainer(MountableFile.forClasspathResource("jenkins/octopus_tool.groovy"),
          "/usr/share/jenkins/ref/init.groovy.d/octopus_tool.groovy")
      .withCopyFileToContainer(MountableFile.forClasspathResource("jenkins/octopus_server.groovy"),
          "/usr/share/jenkins/ref/init.groovy.d/octopus_server.groovy")
      .withNetwork(NETWORK)
      .dependsOn(octopus)
      .withExposedPorts(8081)
      .withNetworkAliases("jenkins")
      .withEnv("OCTOPUS_API_KEY", "API-" + RANDOM_OCTO_API)
      .withEnv("JENKINS_OPTS", "--httpPort=8081")
      .withEnv("JAVA_OPTS",
          "-Djenkins.install.runSetupWizard=false "
              + "-Dhudson.security.csrf.GlobalCrumbIssuerConfiguration.DISABLE_CSRF_PROTECTION=true");

  private static Stream<Arguments> provideTestRepos() {
    return Stream.of(
        Arguments.of(
            "dotnetcore",
            new DotnetTestRepoClient(
                "https://github.com/OctopusSamples/RandomQuotes")),
        Arguments.of(
            "ruby",
            new RubyTestRepoClient(
                "https://github.com/OctopusSamples/RandomQuotes-Ruby",
                "master")),
        Arguments.of(
            "go",
            new GoTestRepoClient(
                "https://github.com/OctopusSamples/RandomQuotes-Go",
                "main")),
        Arguments.of(
            "php",
            new NodeTestRepoClient(
                "https://github.com/OctopusSamples/RandomQuotes-JS")),
        Arguments.of(
            "php",
            new PhpTestRepoClient(
                "https://github.com/OctopusSamples/RandomQuotes-PHP")),
        Arguments.of(
            "python",
            new PythonTestRepoClient(
                "https://github.com/OctopusSamples/RandomQuotes-Python",
                "main")),
        Arguments.of(
            "gradle",
            new GradleTestRepoClient(
                "https://github.com/mcasperson/SampleGradleProject-SpringBoot",
                false)),
        Arguments.of(
            "maven",
            new MavenTestRepoClient(
                "https://github.com/mcasperson/SampleMavenProject-SpringBoot",
                false)),
        Arguments.of(
            "mavenWrapper",
            new MavenTestRepoClient(
                "https://github.com/mcasperson/SampleMavenProject-SpringBoot",
                true)),
        Arguments.of(
            "mavenWrapperQuarkus",
            new MavenTestRepoClient(
                "https://github.com/mcasperson/SampleMavenProject-Quarkus",
                true)),
        Arguments.of(
            "gradleWrapper",
            new GradleTestRepoClient(
                "https://github.com/mcasperson/SampleGradleProject-SpringBoot",
                true))
    );
  }


  @ParameterizedTest
  @MethodSource("provideTestRepos")
  public void verifyTemplate(@NonNull final String name, @NonNull final RepoClient accessor)
      throws IOException {

    initOctopus(accessor);

    System.out.println(jenkins.getLogs());

    System.out.println("Testing " + accessor.getClass().getName());

    final String template = Arrays.stream(PIPELINE_BUILDERS)
        .filter(p -> p.canBuild(accessor))
        .map(p -> p.generate(accessor))
        .findFirst().get();

    System.out.println(template);

    // Add the job to the docker image
    addJobToJenkins(getScriptJob(template), name);

    final JenkinsDetails jenkinsDetails = new JenkinsDetails(
        jenkins.getHost(),
        jenkins.getFirstMappedPort());

    // print the Jenkins URL
    System.out.println("Jenkins URL: " + jenkinsDetails);

    // Now restart jenkins, initiate a build, and check the build result
    final Try<Boolean> success =
        // wait for the server to start
        JENKINS_CLIENT.waitServerStarted(jenkinsDetails)
            // restart the server to pick up the new jobs
            .flatMap(r -> JENKINS_CLIENT.restartJenkins(jenkinsDetails))
            // wait for the server to start again
            .flatMap(r -> JENKINS_CLIENT.waitServerStarted(jenkinsDetails))
            // start building the job
            .flatMap(r -> JENKINS_CLIENT.startJob(jenkinsDetails, name))
            // wait for the job to finish
            .flatMap(r -> JENKINS_CLIENT.waitJobBuilding(jenkinsDetails, name))
            // see if the job was a success
            .map(JENKINS_CLIENT::isSuccess);

    // dump the job logs
    JENKINS_CLIENT.getJobLogs(jenkins.getHost(), jenkins.getFirstMappedPort(), name)
        .onSuccess(System.out::println);

    Assertions.assertTrue(success.isSuccess());
    Assertions.assertTrue(success.get());
  }

  private void initOctopus(@NonNull final RepoClient accessor) {
    OCTOPUS_CLIENT.setUrl("http://" + octopus.getHost() + ":" + octopus.getFirstMappedPort());
    OCTOPUS_CLIENT.setApiKey("API-" + RANDOM_OCTO_API);
    OCTOPUS_CLIENT.createEnvironment("Dev");
    OCTOPUS_CLIENT.createProject(accessor.getRepoName().get(), OCTOPUS_CLIENT.getDefaultProjectGroupId(),
        OCTOPUS_CLIENT.getDefaultLifecycleId());
    OCTOPUS_CLIENT.addStepToProject(accessor.getRepoName().get());
  }

  private void addJobToJenkins(@NonNull final String jobXml, @NonNull final String jobName) {
    jenkins.copyFileToContainer(
        Transferable.of(jobXml.getBytes(), 0744),
        "/var/jenkins_home/jobs/" + jobName + "/config.xml");

    jenkins.copyFileToContainer(
        Transferable.of(("lastCompletedBuild -1\n"
            + "lastFailedBuild -1\n"
            + "lastStableBuild -1\n"
            + "lastSuccessfulBuild -1\n"
            + "lastUnstableBuild -1\n"
            + "lastUnsuccessfulBuild -1").getBytes(), 0744),
        "/var/jenkins_home/jobs/" + jobName + "/builds/permalinks");

    jenkins.copyFileToContainer(
        Transferable.of("".getBytes(), 0744),
        "/var/jenkins_home/jobs/" + jobName + "/builds/legacyIds");


  }

  private String getScriptJob(@NonNull final String script) throws IOException {
    final String template = Resources.toString(
        Resources.getResource("jenkins/job_template.xml"),
        StandardCharsets.UTF_8);
    return template.replace("#{Script}", StringEscapeUtils.escapeXml11(script));
  }
}
