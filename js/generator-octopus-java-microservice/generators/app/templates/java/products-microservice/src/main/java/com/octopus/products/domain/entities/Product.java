package com.octopus.products.domain.entities;

import com.github.jasminb.jsonapi.IntegerIdHandler;
import com.github.jasminb.jsonapi.annotations.Type;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import org.hibernate.envers.Audited;

/**
 * Represents an JSONAPI resource and database entity.
 */
@Entity
@Data
@Jacksonized
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "product")
@Type("products")
public class Product {

  @Id
  @com.github.jasminb.jsonapi.annotations.Id(IntegerIdHandler.class)
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Integer id;

  @Audited
  @NotBlank
  public String dataPartition;

  @Audited
  @NotBlank
  public String name;

  @Audited
  public String pdf;

  @Audited
  public String epub;

  @Audited
  public String web;

  @Audited
  public String image;

  @Audited
  public String description;
}
