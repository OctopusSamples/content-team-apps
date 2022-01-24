package com.octopus.githuboauth.domain.entities;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

/**
 * Represents an OAuth state code used to request a token.
 */
@Data
@Entity
@Table(name = "oauthstate")
public class OauthState {
  @Id
  private String state;
  private Timestamp created;

  /**
   * Constructor that sets state to a random string and created to the current time.
   */
  public OauthState() {
    this.state = UUID.randomUUID().toString();
    this.created = Timestamp.from(Instant.now());
  }

}
