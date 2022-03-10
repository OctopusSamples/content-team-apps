package com.octopus.githubrepo.domain.entities;

import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Builder;
import lombok.Data;

/**
 * Represents an API key.
 */
@Data
@Builder
@Type("apikey")
public class ApiKey {
  @Id
  private String id;
  private String purpose;
  private String userId;
  private String apiKey;
  private String created;
  private String expires;
}
