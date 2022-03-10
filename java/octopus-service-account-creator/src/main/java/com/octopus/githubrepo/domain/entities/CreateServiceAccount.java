package com.octopus.githubrepo.domain.entities;

import com.github.jasminb.jsonapi.annotations.Type;
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
@Type("createserviceaccount")
public class CreateServiceAccount extends ServiceAccount {

  /**
   * The octopus server to create the account against
   */
  @NotBlank
  private String octopusServer;

  public ServiceAccount convertToServiceAccount() {
    return ServiceAccount.builder()
        .isService(this.isService())
        .id(this.getId())
        .username(this.getUsername())
        .displayName(this.getDisplayName())
        .build();
  }

}
