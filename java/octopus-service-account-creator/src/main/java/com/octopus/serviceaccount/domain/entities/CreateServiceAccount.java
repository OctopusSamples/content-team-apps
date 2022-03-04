package com.octopus.serviceaccount.domain.entities;

import com.github.jasminb.jsonapi.annotations.Type;
import javax.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Represents an JSONAPI resource and database entity.
 */
@Data
@Type("createserviceaccount")
public class CreateServiceAccount extends ServiceAccount {

  /**
   * The octopus server to create the account against
   */
  @NotBlank
  public String octopusServer;

  public ServiceAccount getServiceAccount() {
    final ServiceAccount serviceAccount = new ServiceAccount();
    serviceAccount.setService(this.isService());
    serviceAccount.setId(this.getId());
    serviceAccount.setUsername(this.getUsername());
    serviceAccount.setDisplayName(this.getDisplayName());
    return serviceAccount;
  }

}
