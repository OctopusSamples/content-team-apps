package com.octopus.githubproxy.domain.entities;

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
    final String[] split = id.trim().split("/");
    if (split.length == 2) {
      return Optional.of(RepoId.builder()
          .owner(split[0])
          .repo(split[1])
          .build());
    }

    return Optional.empty();
  }
}
