package com.octopus.serviceaccount.domain.entities;

import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Type("apikey")
public class ApiKey {
  /**
   * The service account id
   */
  @Id
  private String id;
  private String purpose;
  private String userId;
  private String apiKey;
  private String created;
  private String expires;
}
