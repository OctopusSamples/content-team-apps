package com.octopus.githubrepo.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Secret {
  private String name;
  private String value;
  private boolean serverSideEncrypted;
  private boolean clientSideEncrypted;
}
