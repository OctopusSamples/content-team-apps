package com.octopus.octopub.domain.entities;

import com.github.jasminb.jsonapi.IntegerIdHandler;
import com.github.jasminb.jsonapi.annotations.Type;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Represents an audit JSONAPI resource and database entity. Audit records are based on the idea
 * of an RDF semantic triple, except instead of a generic predicate (Bob knows John) we assume
 * all auditable events involve actions (Bod created Document 1).
 */
@Entity
@Data
@Table(name = "audit")
@Type("audits")
public class Audit {

  @Id
  @com.github.jasminb.jsonapi.annotations.Id(IntegerIdHandler.class)
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  public Integer id;

  @NotBlank public String dataPartition;

  /**
   * The subject that initiated the action.
   */
  @NotBlank public String subject;

  /**
   * The action that was taken.
   */
  @NotBlank public String action;

  /**
   * The object that the action was taken against.
   */
  @NotBlank public String object;

  /**
   * Indicates (but does not verify) that the subject in this record is encrypted.
   */
  public boolean encryptedSubject;

  /**
   * Indicates (but does not verify) that the object in this record is encrypted.
   */
  public boolean encryptedObject;
}
