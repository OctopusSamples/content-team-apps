package com.octopus.githuboauth.infrastructure.repositories;

import com.octopus.githuboauth.domain.entities.OAuthState;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import lombok.NonNull;

/**
 * Repositories are the interface between the application and the data store. They don't contain any
 * business logic, security rules, or manual audit logging. Note though that we use Envers to
 * automatically track database changes.
 */
@ApplicationScoped
public class OAuthStateRepository {

  @Inject EntityManager em;

  /**
   * Get a single entity.
   *
   * @param id The ID of the entity to return.
   * @return The entity.
   */
  public OAuthState findOne(final String id) {
    final OAuthState state = em.find(OAuthState.class, id);
    /*
     We don't expect any local code to modify the entity returned here. Any changes will be done by
     returning the entity to a client, the client makes the appropriate updates, and the updated
     entity is sent back with a new request.

     To prevent the entity from being accidentally updated, we detach it from the context.
     */
    if (state != null) {
      em.detach(state);
    }
    return state;
  }

  /**
   * Delete a state entity.
   *
   * @param id The id of the state entity to delete.
   */
  public void delete(final String id) {
    em.createQuery("delete from OAuthState a where a.id=:id").setParameter("id", id).executeUpdate();
  }

  /**
   * Saves a new product in the data store.
   *
   * @param state The state to save.
   * @return The newly created entity.
   */
  public OAuthState save(@NonNull final OAuthState state) {
    em.persist(state);
    em.flush();
    return state;
  }
}
