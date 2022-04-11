package com.octopus.githubproxy.domain.entities;

import io.vavr.control.Try;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

/**
 * The ID of a GitHub repo is "owner/repo". This class splits the two out.
 */
@Data
@Builder
public class RepoId {

  private String owner;
  private String repo;

  /**
   * Builds a RepoId from a combined id.
   *
   * @param id The combined id.
   * @return An optional containing the owner and repo, or empty if the string could not be parsed.
   */
  public static Optional<RepoId> fromId(@NonNull final String id) {
    // The ID must be a valid URL like https://api.github.com/repos/owner/repo
    return Optional.ofNullable(Try.of(() -> new URL(URLDecoder.decode(id, StandardCharsets.UTF_8)))
        // The repo and owner are in the path
        .map(URL::getPath)
        // We expect to find a repos prefix
        .filter(p -> p.startsWith("/repos/"))
        // remove the repos prefix
        .map(p -> p.replaceFirst("/repos/", ""))
        // the remaining path is the owner and repo
        .map(p -> p.split("/"))
        // we expect to find two components: the owner and repo
        .filter(p -> p.length == 2)
        // convert the two components to a RepoId
        .map(p -> RepoId.builder()
            .owner(p[0])
            .repo(p[1])
            .build())
        // all other results (exceptions or filtered results) means failure
        .getOrElse(() -> null));
  }
}
