package com.octopus.githubactions.domain.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.jasminb.jsonapi.IntegerIdHandler;
import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Type;
import java.sql.Timestamp;
import lombok.Data;

/**
 * Represents an audit resource.
 */
@Type("audits")
@Data
public class Audit {

  @Id(IntegerIdHandler.class)
  private Integer id;
  private String subject;
  private String action;
  private String object;
  @JsonFormat(shape = JsonFormat.Shape.NUMBER, pattern = "s")
  private Timestamp time;
  private boolean encryptedSubject;
  private boolean encryptedObject;

  public Audit() {

  }

  /**
   * Constructs an Audit record.
   *
   * @param subject The subject performing the action on the object.
   * @param action  The action performed by the subject on the object.
   * @param object  The object that had the action performed on it by the subject.
   */
  public Audit(final String subject, final String action, final String object) {
    this(subject, action, object, false, false);
  }

  /**
   * Constructs an Audit record.
   *
   * @param subject          The subject performing the action on the object.
   * @param action           The action performed by the subject on the object.
   * @param object           The object that had the action performed on it by the subject.
   * @param encryptedObject  Whether the object is encrypted.
   * @param encryptedSubject Whether the subject is encrypted.
   */
  public Audit(final String subject, final String action, final String object,
      final boolean encryptedSubject, final boolean encryptedObject) {
    this.subject = subject;
    this.action = action;
    this.object = object;
    this.time = new Timestamp(System.currentTimeMillis());
    this.encryptedObject = encryptedObject;
    this.encryptedSubject = encryptedSubject;
  }
}
