package com.octopus.customers.infrastructure.repositories;

import com.github.tennaito.rsql.jpa.JpaPredicateVisitor;
import com.octopus.Constants;
import com.octopus.customers.domain.entities.Customer;
import com.octopus.exceptions.InvalidInput;
import com.octopus.wrappers.FilteredResultWrapper;
import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.ast.Node;
import cz.jirutka.rsql.parser.ast.RSQLVisitor;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import lombok.NonNull;
import org.apache.commons.lang3.math.NumberUtils;
import org.h2.util.StringUtils;

/**
 * Repositories are the interface between the application and the data store. They don't contain any
 * business logic, security rules, or manual audit logging.
 */
@ApplicationScoped
public class CustomersRepository {

  @Inject
  EntityManager em;

  @Inject
  Validator validator;

  /**
   * Get a single entity.
   *
   * @param id The ID of the entity to update.
   * @return The entity.
   */
  public Customer findOne(final int id) {
    final Customer customer = em.find(Customer.class, id);
    /*
     We don't expect any local code to modify the entity returned here. Any changes will be done by
     returning the entity to a client, the client makes the appropriate updates, and the updated
     entity is sent back with a new request.

     To prevent the entity from being accidentally updated, we detach it from the context.
     */
    if (customer != null) {
      em.detach(customer);
    }
    return customer;
  }

  /**
   * Returns all matching entities.
   *
   * @param partitions The partitions that entities can be found in.
   * @param filter     The RSQL filter used to query the entities.
   * @return The matching entities.
   */
  public FilteredResultWrapper<Customer> findAll(
      @NonNull final List<String> partitions,
      final String filter,
      final String pageOffset,
      final String pageLimit) {

    final CriteriaBuilder builder = em.getCriteriaBuilder();
    final CriteriaQuery<Customer> criteria = builder.createQuery(Customer.class);
    final From<Customer, Customer> root = criteria.from(Customer.class);
    criteria.orderBy(builder.desc(root.get("time")));

    // add the partition search rules
    final Predicate partitionPredicate =
        builder.or(
            partitions.stream()
                .filter(org.apache.commons.lang3.StringUtils::isNotBlank)
                .map(p -> builder.equal(root.get("dataPartition"), p))
                .collect(Collectors.toList())
                .toArray(new Predicate[0]));

    if (!StringUtils.isNullOrEmpty(filter)) {
      /*
       Makes use of RSQL queries to filter any responses:
       https://github.com/jirutka/rsql-parser
      */
      final RSQLVisitor<Predicate, EntityManager> visitor =
          new JpaPredicateVisitor<Customer>().defineRoot(root);
      final Node rootNode = new RSQLParser().parse(filter);
      final Predicate filterPredicate = rootNode.accept(visitor, em);

      // combine with the filter rules
      final Predicate combinedPredicate = builder.and(partitionPredicate, filterPredicate);

      criteria.where(combinedPredicate);
    } else {
      criteria.where(partitionPredicate);
    }

    // Deal with paging
    final TypedQuery<Customer> query = em.createQuery(criteria);
    final int pageLimitParsed = NumberUtils.toInt(pageLimit, Constants.DEFAULT_PAGE_LIMIT);
    final int pageOffsetParsed = NumberUtils.toInt(pageOffset, Constants.DEFAULT_PAGE_OFFSET);
    query.setFirstResult(pageOffsetParsed);
    query.setMaxResults(pageLimitParsed);
    final List<Customer> results = query.getResultList();

    // Get total results
    final CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
    countQuery.select(builder.count(countQuery.from(Customer.class)));
    countQuery.where(criteria.getRestriction());
    final Long count = em.createQuery(countQuery).getSingleResult();

    // detach all the entities
    em.clear();

    return new FilteredResultWrapper(results, count);
  }

  /**
   * Saves a new audit in the data store.
   *
   * @param customer The audit to save.
   * @return The newly created entity.
   */
  public Customer save(@NonNull final Customer customer) {
    customer.id = null;

    validateEntity(customer);

    em.persist(customer);
    em.flush();
    return customer;
  }

  private void validateEntity(final Customer customer) {
    final Set<ConstraintViolation<Customer>> violations = validator.validate(customer);
    if (violations.isEmpty()) {
      return;
    }

    throw new InvalidInput(
        violations.stream().map(cv -> cv.getMessage()).collect(Collectors.joining(", ")));
  }
}
