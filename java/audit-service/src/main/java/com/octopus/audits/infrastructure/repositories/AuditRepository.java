package com.octopus.audits.infrastructure.repositories;

import com.github.tennaito.rsql.jpa.JpaPredicateVisitor;
import com.octopus.audits.GlobalConstants;
import com.octopus.audits.domain.entities.Audit;
import com.octopus.audits.domain.exceptions.InvalidInput;
import com.octopus.audits.domain.wrappers.FilteredResultWrapper;
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
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.math.NumberUtils;
import org.h2.util.StringUtils;

/**
 * Repositories are the interface between the application and the data store. They don't contain any
 * business logic, security rules, or manual audit logging.
 */
@ApplicationScoped
public class AuditRepository {

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
  public Audit findOne(final int id) {
    final Audit audit = em.find(Audit.class, id);
    /*
     We don't expect any local code to modify the entity returned here. Any changes will be done by
     returning the entity to a client, the client makes the appropriate updates, and the updated
     entity is sent back with a new request.

     To prevent the entity from being accidentally updated, we detach it from the context.
     */
    if (audit != null) {
      em.detach(audit);
    }
    return audit;
  }

  /**
   * Returns all matching entities.
   *
   * @param partitions The partitions that entities can be found in.
   * @param filter     The RSQL filter used to query the entities.
   * @return The matching entities.
   */
  public FilteredResultWrapper<Audit> findAll(
      @NonNull final List<String> partitions,
      final String filter,
      final String pageOffset,
      final String pageLimit) {

    final CriteriaBuilder builder = em.getCriteriaBuilder();
    final CriteriaQuery<Audit> criteria = builder.createQuery(Audit.class);
    final From<Audit, Audit> root = criteria.from(Audit.class);
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
          new JpaPredicateVisitor<Audit>().defineRoot(root);
      final Node rootNode = new RSQLParser().parse(filter);
      final Predicate filterPredicate = rootNode.accept(visitor, em);

      // combine with the filter rules
      final Predicate combinedPredicate = builder.and(partitionPredicate, filterPredicate);

      criteria.where(combinedPredicate);
    } else {
      criteria.where(partitionPredicate);
    }

    // Deal with paging
    final TypedQuery<Audit> query = em.createQuery(criteria);
    final int pageLimitParsed = NumberUtils.toInt(pageLimit, GlobalConstants.DEFAULT_PAGE_LIMIT);
    final int pageOffsetParsed = NumberUtils.toInt(pageOffset, GlobalConstants.DEFAULT_PAGE_OFFSET);
    query.setFirstResult(pageOffsetParsed);
    query.setMaxResults(pageLimitParsed);
    final List<Audit> results = query.getResultList();

    // Get total results
    final CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
    countQuery.select(builder.count(countQuery.from(Audit.class)));
    countQuery.where(criteria.getRestriction());
    final Long count = em.createQuery(countQuery).getSingleResult();

    // detach all the entities
    em.clear();

    return new FilteredResultWrapper(results, count);
  }

  /**
   * Saves a new audit in the data store.
   *
   * @param audit The audit to save.
   * @return The newly created entity.
   */
  public Audit save(@NonNull final Audit audit) {
    audit.id = null;

    validateEntity(audit);

    em.persist(audit);
    em.flush();
    return audit;
  }

  private void validateEntity(final Audit audit) {
    /*
      A sanity check to ensure that any audit record that indicates it has encrypted values
      has encoded those values as Base64. Note that this doesn't verify that the values are actually
      encrypted, but simply serves as a safeguard to ensure clients creating encrypted entries
      haven't forgotten to process the (supposedly) encrypted values.
    */
    if (audit.encryptedSubject && !Base64.isBase64(audit.subject)) {
      throw new InvalidInput("Encrypted values must be encoded as Base64");
    }

    if (audit.encryptedObject && !Base64.isBase64(audit.object)) {
      throw new InvalidInput("Encrypted values must be encoded as Base64");
    }

    final Set<ConstraintViolation<Audit>> violations = validator.validate(audit);
    if (violations.isEmpty()) {
      return;
    }

    throw new InvalidInput(
        violations.stream().map(cv -> cv.getMessage()).collect(Collectors.joining(", ")));
  }
}
