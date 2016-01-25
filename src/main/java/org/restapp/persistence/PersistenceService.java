package org.restapp.persistence;

import java.util.List;
import javax.jdo.FetchGroup;
import javax.jdo.JDOException;
import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;
import org.restapp.persistence.Persistable;
import java.util.concurrent.Callable;
import javax.jdo.FetchPlan;
import org.restapp.services.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A generic persistence manager backed by JPA entity manager
 *
 * @author aniket
 */
public class PersistenceService {
  private static final Logger LOG = LoggerFactory.getLogger(PersistenceService.class.getSimpleName());

  private final PersistenceManagerFactory pmf;

  /**
   * Creates a new Persistence service with specified entity manager factory provider.
   *
   * @param emp The EntityManagerFactory provider implementation
   */
  public PersistenceService(PMFProvider emp) {
    pmf = emp.get();
  }
  
  /**
   * Finds the entity of the specified type by its id
   *
   * @param <T> The type of the entity
   * @param clazz Entity class
   * @param id The id of the entity in data store
   * @return The found entity or null if the entity was not found
   */
  public <T extends Persistable> T findById(Class<T> clazz, Object id) {
    LOG.debug("Finding entity {} with id {}", clazz, id);
    PersistenceManager pm = pm();
    try {
      T entity = pm.getObjectById(clazz, id);
      if(JDOHelper.isDeleted(entity)) {
        return null;
      }
      return entity;
    }catch(JDOObjectNotFoundException e) {
      return null;
    }finally {
      done();
    }
  }
  
  public <T extends Persistable> T findById(Class<T> clazz, Object id, boolean detach) {
    if(!detach) {
      return findById(clazz, id);
    }
    LOG.debug("Finding entity {} with id {}", clazz, id);
    PersistenceManager pm = pm();
    try {
      T entity = pm.getObjectById(clazz, id);
      if(JDOHelper.isDeleted(entity)) {
        return null;
      }
      return pm.detachCopy(entity);
    }catch(JDOObjectNotFoundException e) {
      return null;
    }finally {
      done();
    }
  }

  public <T extends Persistable> T findById(Class<T> clazz, Object id, String... fields) {
    LOG.debug("Finding entity {} with id {}, including fields {}", clazz, id, fields);
    PersistenceManager pm = pm();
    FetchGroup grp = pm.getFetchGroup(clazz, "Subset");
    for(String field : fields) {
      grp.addMember(field);
    }
    pm.getFetchPlan().setDetachmentOptions(FetchPlan.DETACH_LOAD_FIELDS).addGroup("Subset");
    try {
      T t = pm.getObjectById(clazz, id);
      if(JDOHelper.isDeleted(t)) {
        return null;
      }
      return t;
    }catch(JDOObjectNotFoundException e) {
      return null;
    }finally {
      done();
    }
  }

  /**
   * Finds the managed entity whose id is the same as specified entity's id.
   *
   * @param <T>
   * @param entity
   * @return the managed entity if found
   */
  @SuppressWarnings("unchecked")
  public <T extends Persistable> T find(T entity) {
    return (T) findById(entity.getClass(), entity.getId());
  }
  
  @SuppressWarnings("unchecked")
  public <T extends Persistable> T find(T entity, boolean detach) {
    return (T) findById(entity.getClass(), entity.getId(), detach);
  }

  /**
   * Find all entities for the specified type.
   *
   * @param <T> The entity type
   * @param entityClass The entity class
   * @param offset
   * @param limit
   * @return a list of entities of the specified type or an empty list if not found
   */
  @SuppressWarnings("unchecked")
  public <T extends Persistable> List<T> findAll(Class<T> entityClass, long offset, long limit) {
    LOG.debug("Finding all entities {}", entityClass);
    PersistenceManager pm = pm();
    try {
      Query q = pm.newQuery(entityClass);
      q.setRange(offset, (offset + limit));
      List<T> ts = (List<T>) q.execute();
      // return (List<T>) pm.detachCopyAll(ts);
      return ts;
    }catch(JDOException e) {
      LOG.error("Error executing findAll query", e);
      throw new ServiceException("A data access error occured");
    }finally {
      done();
    }
  }

  public <T extends Persistable> List<T> findAll(Class<T> entityClass) {
    return findAll(entityClass, 0, 100);
  }
  
  @SuppressWarnings("unchecked")
  public <T extends Persistable> List<T> findAll(Class<T> entityClass, String... fields) {
    LOG.debug("Getting all instances of entity {}", entityClass);
    PersistenceManager pm = pm();
    pm.getFetchGroup(entityClass, "ExtraFields").addMembers(fields);
    pm.getFetchPlan().addGroup("ExtraFields");
    try {
      List<T> ret = (List<T>) pm.newQuery(entityClass).execute();
      // return (List<T>) pm.detachCopyAll(ret);
      return ret;
    }catch(JDOException e) {
      throw new ServiceException("A data access error occured", e);
    }finally {
        done();
    }
  }

  /**
   * Creates or merges an entity. If the entity does not exist, creates a new one else merges this
   * into existing entity.
   *
   * @param <T> The generic entity type
   * @param entity The entity to persist
   * @return the newly created entity
   */
  public <T extends Persistable> T save(T entity) {
    LOG.debug("Creating a new entity {}", entity);
    PersistenceManager pm = pm();
    try {
      begin();
      pm.makePersistent(entity);
      commit();
      return entity;
    }catch(JDOException e) {
      LOG.error("Error saving entity", e);
      throw new ServiceException("A data access error occured");
    }finally {
      done();
    }
  }

  /**
   * Removes the specified entity from the data store.
   *
   * @param entity
   */
  public void remove(Persistable entity) {
    LOG.debug("Removing entity {}", entity);
    PersistenceManager pm = pm();
    try {
      begin();
      pm.deletePersistent(entity);
      commit();
    }catch(JDOException e) {
      LOG.error("Error executing named query", e);
      throw new ServiceException("A data access error occured");
    }finally {
      done();
    }
  }

  /**
   * Executes a named query (with implicit parameters) that is expecting a given type.
   *
   * @param <T> The expected type
   * @param entityClass The entity class of the returned type
   * @param queryName The name of the query as defined by @NamedQuery annotation on entity
   * @param params Additional query parameters.
   * @throws IllegalArgumentException if the parameters are not in pairs.
   * @return a single result of the expected type or null if the query did not have any result.
   */
  @SuppressWarnings("unchecked")
  public <T extends Persistable> T getByNamedQuery(Class<T> entityClass, String queryName, Object... params) {
    PersistenceManager pm = pm();
    try {
      begin();
      Query q = pm.newNamedQuery(entityClass, queryName);
      q.setUnique(true);
      q.setResultClass(entityClass);

      LOG.debug("Executing named query '{}' {} with params {}", queryName, q.toString(), params);
      T result = (T) q.executeWithArray(params);
      // pm.detachCopy(result);
      q.closeAll();
      commit();
      return result;
    }catch(JDOException e) {
      LOG.error("Error executing named query", e);
      throw new ServiceException("A data access error occured");
    }finally {
      done();
    }
  }
  
  @SuppressWarnings("unchecked")
  public <T extends Persistable> T getByNamedQuery(Class entityClass, Class<T> resultClass, 
      String queryName, Object... params) {
    PersistenceManager pm = pm();
    pm.getFetchPlan().setDetachmentOptions(FetchPlan.DETACH_LOAD_FIELDS);
    try {
      begin();
      Query q = pm.newNamedQuery(entityClass, queryName);
      q.setUnique(true);
      q.setResultClass(resultClass);

      LOG.debug("Executing named query '{}' {} with params {}", queryName, q.toString(), params);
      T result = (T) q.executeWithArray(params);
      // q.closeAll();
      commit();
      return result;
    }catch(JDOException e) {
      LOG.error("Error executing named query", e);
      throw new ServiceException("A data access error occured");
    }finally {
      done();
    }
  }

  /**
   * Executes a named query(with implicit parameters) that is expecting a given type.
   *
   * @param <T> The expected type
   * @param entityClass The entity class of the returned type
   * @param queryName The name of the query as defined by @NamedQuery annotation on entity
   * @param params Additional query parameters.
   * @throws IllegalArgumentException if the parameters are not in pairs.
   * @return a single result of the expected type or null if the query did not have any result.
   */
  @SuppressWarnings("unchecked")
  public <T extends Persistable> List<T> getAllByNamedQuery(Class<T> entityClass, String queryName, 
      Object... params) {
    PersistenceManager pm = pm();
    try {
      begin();
      Query q = pm.newNamedQuery(entityClass, queryName);
      q.setResultClass(entityClass);
      List<T> result = (List<T>) q.executeWithArray(params);
      // q.closeAll();
      commit();
      return result;
    }catch(JDOException e) {
      LOG.error("Error executing named query", e);
      throw new ServiceException("A data access error occured");
    }finally {
      done();
    }
  }
  
  @SuppressWarnings("unchecked")
  public <T> List<T> getAllByNamedQuery(Class entityClass, Class<T> resultClass, String queryName, 
        Object... params) {
    PersistenceManager pm = pm();
    try {
      begin();
      Query q = pm.newNamedQuery(entityClass, queryName);
      q.setResultClass(resultClass);
      List<T> result = (List<T>) q.executeWithArray(params);
      // q.closeAll();
      commit();
      return result;
    }catch(JDOException e) {
      LOG.error("Error executing named query", e);
      throw new ServiceException("A data access error occured");
    }finally {
       done();
    }
  }


  private static final ThreadLocal<Boolean> extTransaction = new ThreadLocal<>();
  private static final ThreadLocal<PersistenceManager> ctx = new ThreadLocal<>();

  public <T> T inTransaction(Callable<T> call) {
    PersistenceManager pm = pm();
    try {
      begin();
      extTransaction.set(true);
      LOG.info("TRANSACTION ID {}: {}", pm.currentTransaction(), pm.currentTransaction().isActive());
      T ret = call.call();
      extTransaction.set(false);
      commit();
      return ret;
    }catch(Exception e) {
      extTransaction.set(false);
      throw new ServiceException(e);
    }finally {
      extTransaction.set(false);
      done();
    }
  }

	// ----------------------------------- Private Persistence API ---------------------------------
  private PersistenceManager pm() {
    PersistenceManager pm = ctx.get();
    if(pm == null) {
      LOG.debug("Getting a fresh persistence manager");
      pm = pmf.getPersistenceManager();
      ctx.set(pm);
    }
    return pm;
  }

  private void begin() {
    if(Boolean.TRUE.equals(extTransaction.get())) {
      return;
    }
    PersistenceManager pm = pm();
    Transaction tx = pm.currentTransaction();
    LOG.debug("Begining transaction for pm {}", pm);
    tx.begin();
  }

  private void commit() {
    if(Boolean.TRUE.equals(extTransaction.get())) {
      return;
    }
    PersistenceManager pm = ctx.get();
    if(pm != null) {
      LOG.debug("Commiting transaction for pm {}", pm);
      pm.currentTransaction().commit();
    }
  }

  private void done() {
    if(Boolean.TRUE.equals(extTransaction.get())) {
      return;
    }
    PersistenceManager pm = ctx.get();
    if(pm != null) {
      LOG.debug("Cleaning up transaction for pm {}", pm);
      ctx.remove();
      Transaction tx = pm.currentTransaction();
      if(tx.isActive()) {
        tx.rollback();
      }
      pm.close();
    }
  }

}