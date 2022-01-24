package com.octopus.githuboauth.domain.entities;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "oauthstate")
public class OAuthState {
  @Id
  private String state;
  private Timestamp created;

  public OAuthState() {
    this.state = UUID.randomUUID().toString();
    this.created = Timestamp.from(Instant.now());
  }

}
