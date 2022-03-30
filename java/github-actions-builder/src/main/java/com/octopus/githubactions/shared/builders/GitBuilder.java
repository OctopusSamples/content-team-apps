package com.octopus.githubactions.shared.builders;

import com.google.common.collect.ImmutableMap;
import com.octopus.githubactions.shared.builders.dsl.Step;
import com.octopus.githubactions.shared.builders.dsl.UsesWith;
import com.octopus.repoclients.RepoClient;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

/** Contains a number of common steps shared between builders. */
public class GitBuilder {
  /** Builds the common top level comments for the workflow. */
  public String getInitialComments() {
    return "# The following workflow provides an opinionated template you can customize for your own needs.\n"
        + "#\n"
        + "# If you are not an Octopus user, the \"Push to Octopus\", \"Generate Octopus Deploy build information\",\n"
        + "# and \"Create Octopus Release\" steps can be safely deleted.\n"
        + "#\n"
        + "# To configure Octopus, set the OCTOPUS_API_TOKEN secret to the Octopus API key, and\n"
        + "# set the OCTOPUS_SERVER_URL secret to the Octopus URL.\n"
        + "#\n"
        + "# Double check the \"project\" and \"deploy_to\" properties in the \"Create Octopus Release\" step\n"
        + "# match your Octopus projects and environments.\n"
        + "#\n"
        + "# Get a trial Octopus instance from https://octopus.com/start\n";
  }

  /** Build the checkout step. */
  public Step checkOutStep() {
    return UsesWith.builder()
        .uses("actions/checkout@v1")
        .with(new ImmutableMap.Builder<String, String>().put("fetch-depth", "0").build())
        .build();
  }

  /** Build the GitVersion installation step. */
  public Step gitVersionInstallStep() {
    return UsesWith.builder()
        .name("Install GitVersion")
        .uses("gittools/actions/gitversion/setup@v0.9.7")
        .with(new ImmutableMap.Builder<String, String>().put("versionSpec", "5.x").build())
        .build();
  }

  /** Build the step to calculate the versions from git. */
  public Step getVersionCalculate() {
    return UsesWith.builder()
        .name("Determine Version")
        .id("determine_version")
        .uses("gittools/actions/gitversion/execute@v0.9.7")
        .with(new ImmutableMap.Builder<String, String>().put("additionalArguments", "/overrideconfig mode=Mainline").build())
        .build();
  }

  /** Build the Octopus CLI installation step. */
  public Step installOctopusCli() {
    return UsesWith.builder()
        .name("Install Octopus Deploy CLI")
        .uses("OctopusDeploy/install-octocli@v1.1.1")
        .with(new ImmutableMap.Builder<String, String>().put("version", "latest").build())
        .build();
  }

  /** Build the test processing step. */
  public Step buildJunitReport(@NonNull final String name, @NonNull final String path) {
    return UsesWith.builder()
        .name("Report")
        .uses("dorny/test-reporter@v1")
        .ifProperty("always()")
        .with(
            new ImmutableMap.Builder<String, String>()
                .put("name", name)
                .put("path", path)
                .put("reporter", "java-junit")
                .put("fail-on-error", "false")
                .build())
        .build();
  }

  /**
   * Build the github release creation step.
   * Note that in order to allow reruns, we must incorporate the run_number and run_attempt
   * into the release name. This allows the artifacts from a particular attempt to be captured.
   * In order to use github releases from an Octopus feed, the release tags must be valid semver.
   * So we embed the run_number and run_attempt in the metadata.
   */
  public Step createGitHubRelease() {
    return UsesWith.builder()
        .name("Create Release")
        .id("create_release")
        .uses("actions/create-release@v1")
        .env(
            new ImmutableMap.Builder<String, String>()
                .put("GITHUB_TOKEN", "${{ secrets.GITHUB_TOKEN }}")
                .build())
        .with(
            new ImmutableMap.Builder<String, String>()
                .put("tag_name", "${{ steps.determine_version.outputs.semVer }}+run${{ github.run_number }}-attempt${{ github.run_attempt }}")
                .put("release_name", "Release ${{ steps.determine_version.outputs.semVer }} Run ${{ github.run_number }} Attempt ${{ github.run_attempt }}")
                .put("draft", "false")
                .put("prerelease", "false")
                .build())
        .build();
  }

  /** Tag the repo with the release. */
  public Step tagRepo() {
    return UsesWith.builder()
        .name("Tag Release")
        .uses("mathieudutour/github-tag-action@v6.0")
        .with(
            new ImmutableMap.Builder<String, String>()
                .put("custom_tag", "${{ steps.determine_version.outputs.semVer }}")
                .put("github_token", "${{ secrets.GITHUB_TOKEN }}")
                .build())
        .build();
  }

  /** Build the step to upload file to the github release. */
  public Step uploadToGitHubRelease(@NonNull final String path, @NonNull final String name) {
    return UsesWith.builder()
        .name("Upload Release Asset")
        .uses("actions/upload-release-asset@v1")
        .env(
            new ImmutableMap.Builder<String, String>()
                .put("GITHUB_TOKEN", "${{ secrets.GITHUB_TOKEN }}")
                .build())
        .with(
            new ImmutableMap.Builder<String, String>()
                .put("upload_url", "${{ steps.create_release.outputs.upload_url }}")
                .put("asset_path", path)
                .put("asset_name", name)
                .put("asset_content_type", "application/octet-stream")
                .build())
        .build();
  }

  /** Build the step to push files to Octopus. */
  public Step pushToOctopus(@NonNull final String packages) {
    return UsesWith.builder()
        .name("Push to Octopus")
        .uses("OctopusDeploy/push-package-action@v1.1.1")
        .with(
            new ImmutableMap.Builder<String, String>()
                .put("api_key", "${{ secrets.OCTOPUS_API_TOKEN }}")
                .put("packages", packages)
                .put("server", "${{ secrets.OCTOPUS_SERVER_URL }}")
                .put("overwrite_mode", "OverwriteExisting")
                .build())
        .build();
  }

  /** Build the step to push Octopus build information. */
  public Step uploadOctopusBuildInfo(@NonNull final RepoClient accessor) {
    return UsesWith.builder()
        .name("Generate Octopus Deploy build information")
        .uses("xo-energy/action-octopus-build-information@v1.1.2")
        .with(
            new ImmutableMap.Builder<String, String>()
                .put("octopus_api_key", "${{ secrets.OCTOPUS_API_TOKEN }}")
                .put("octopus_project", accessor.getRepoName().getOrElse("application"))
                .put("octopus_server", "${{ secrets.OCTOPUS_SERVER_URL }}")
                .put("push_version", "${{ steps.determine_version.outputs.semVer }}")
                .put("push_package_ids", accessor.getRepoName().getOrElse("application"))
                .put("push_overwrite_mode", "OverwriteExisting")
                .put("output_path", "octopus")
                .build())
        .build();
  }

  /** Build the step to create an Octopus release. */
  public Step createOctopusRelease(@NonNull final RepoClient accessor, @NonNull final String packages) {
    return UsesWith.builder()
        .name("Create Octopus Release")
        .uses("OctopusDeploy/create-release-action@v1.1.1")
        .with(
            new ImmutableMap.Builder<String, String>()
                .put("api_key", "${{ secrets.OCTOPUS_API_TOKEN }}")
                .put("project", accessor.getRepoName().getOrElse("application"))
                .put("server", "${{ secrets.OCTOPUS_SERVER_URL }}")
                .put("deploy_to", "Development")
                .put("packages", packages)
                .build())
        .build();
  }

  public Step collectDependencies() {
    return collectDependencies(null);
  }

  /** Build the dependency collection step. */
  public Step collectDependencies(final String workingDirectory) {
    final String directory = StringUtils.isBlank(workingDirectory)
        ? "" : workingDirectory + "/";
    return UsesWith.builder()
        .name("Collect Dependencies")
        .uses("actions/upload-artifact@v2")
        .with(
            new ImmutableMap.Builder<String, String>()
                .put("name", "Dependencies")
                .put("path", directory + "dependencies.txt")
                .build())
        .build();
  }

  public Step collectDependencyUpdates() {
    return collectDependencyUpdates(null);
  }

  /** Build the dependency updates collection step. */
  public Step collectDependencyUpdates(final String workingDirectory) {
    final String directory = StringUtils.isBlank(workingDirectory)
        ? "" : workingDirectory + "/";
    return UsesWith.builder()
        .name("Collect Dependency Updates")
        .uses("actions/upload-artifact@v2")
        .with(
            new ImmutableMap.Builder<String, String>()
                .put("name", "Dependencies Updates")
                .put("path", directory + "dependencyUpdates.txt")
                .build())
        .build();
  }

  /** Build the java installation step. */
  public Step installJava() {
    return UsesWith.builder()
        .name("Set up JDK 1.17")
        .uses("actions/setup-java@v2")
        .with(
            new ImmutableMap.Builder<String, String>()
                .put("java-version", "17")
                .put("distribution", "adopt")
                .build())
        .build();
  }
}
