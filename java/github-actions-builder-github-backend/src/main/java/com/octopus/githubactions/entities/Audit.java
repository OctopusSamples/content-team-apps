package com.octopus.githubactions.entities;

import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Type;
import java.sql.Timestamp;
import lombok.Data;

/** Represents an audit resource. */
@Type("audits")
@Data
public class Audit {

  @Id private String id;
  private String subject;
  private String action;
  private String object;
  private Timestamp time;

  /**
   * Constructs an Audit record.
   *
   * @param subject The subject performing the action on the object.
   * @param action The action performed by the subject on the object.
   * @param object The object that had the action performed on it by the subject.
   */
  public Audit(final String subject, final String action, final String object) {
    this.subject = subject;
    this.action = action;
    this.object = object;
    this.time = new Timestamp(System.currentTimeMillis());
  }
}
