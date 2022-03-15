package com.octopus.githubrepo.domain.entities;

import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

/**
 * Represents an JSONAPI resource requesting the creation of a new Octopus service account.
 * Note this resource encapsulates the details of the cloud instance to create the account in
 * as well as the details of the new account.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@Type("creategithubrepo")
@Jacksonized
public class CreateGithubRepo  {

  @Id
  private String id;

  @NotBlank
  private String githubOwner;

  @NotBlank
  private String githubRepository;

  private String generator;

  private Map<String, String> options;

  private Collection<Secret> secrets;

}
