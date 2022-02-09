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
 * Represents an audit JSONAPI resource and database entity.
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

  @NotBlank public String subject;

  @NotBlank public String action;

  @NotBlank public String object;
}
