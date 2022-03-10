package com.octopus.customers.domain.entities;

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
 * Represents an JSONAPI resource and database entity.
 */
@Entity
@Data
@Table(name = "customer")
@Type("customers")
public class Customer {

  @Id
  @com.github.jasminb.jsonapi.annotations.Id(IntegerIdHandler.class)
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  public Integer id;

  @NotBlank
  public String dataPartition;

  /**
   * The customers first name.
   */
  @NotBlank
  public String firstName;

  /**
   * The customers last name.
   */
  @NotBlank
  public String lastName;

  /**
   * The customers address.
   */
  @NotBlank
  public String addressLine1;

  /**
   * The customers address.
   */
  @NotBlank
  public String addressLine2;

  /**
   * The customers city.
   */
  @NotBlank
  public String city;

}
