package com.octopus.githubrepo.domain.entities;

import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Type;
import java.util.Collection;
import java.util.Map;
import javax.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

/**
 * Represents an JSONAPI resource requesting the creation of a new GitHub commit.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@Type("githubcommit")
@Jacksonized
public class CreateGithubCommit {

  @Id
  private String id;

  @NotBlank
  private String githubRepository;

  private String githubOwner;

  private String githubBranch;

  private String generator;

  private Map<String, String> options;

  private Collection<Secret> secrets;

  private boolean createNewRepo;
}