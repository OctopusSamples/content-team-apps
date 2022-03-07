package com.octopus.githubrepo.domain.entities;

import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Type;
import java.util.Collection;
import javax.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Represents an JSONAPI resource requesting the creation of a new Octopus service account.
 * Note this resource encapsulates the details of the cloud instance to create the account in
 * as well as the details of the new account.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@Type("creategithubrepo")
public class CreateGithubRepo  {

  @Id
  private String id;

  /**
   * The octopus server to create the account against
   */
  @NotBlank
  private String octopusServer;

  @NotBlank
  private String githubOwner;

  @NotBlank
  private String githubRepository;

  private Collection<Secret> secrets;

}
