package org.restapp.services;

import org.restapp.da.EMP;
import org.restapp.model.Persistable;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.jdo.JDOException;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A generic persistence manager backed by JPA entity manager
 * @author aniket
 */
@Named
@Singleton
public class PersistenceService {
    private static final Logger log = LoggerFactory.getLogger(PersistenceService.class.getSimpleName());

    
    private PersistenceManagerFactory pmf;
    
    /**
     * Creates a new Persistence service with specified entity manager factory provider.
     * @param emp  The EntityManagerFactory provider implementation
     */
    @Inject
    public PersistenceService(EMP emp) {
        pmf = emp.get();
    }
    
    /**
     * Finds the entity of the specified type by its id
     * @param <T> The type of the entity
     * @param clazz Entity class
     * @param id The id of the entity in data store
     * @return  The found entity or null if the entity was not found
     */
    public <T> T findById(Class<T> clazz, Object id) {
        log.debug("Finding entity {} with id {}", clazz, id);
        PersistenceManager pm = pmf.getPersistenceManager();
        try {
            return pm.getObjectById(clazz, id);
        }catch(JDOObjectNotFoundException e) {
            return null;
        }finally {
            pm.close();
        }
    }
    
    /**
     * Finds the managed entity whose id is the same as specified entity's id.
     * @param <T> 
     * @param entity
     * @return the managed entity if found
     */
    public <T extends Persistable> T find(T entity) {
        return (T)findById(entity.getClass(), entity.getId());
    }
    
    /**
     * Find all entities for the specified type.
     * @param <T> The entity type
     * @param entityClass The entity class
     * @return a list of entities of the specified type or an empty list if not found
     */
    public <T> List<T> findAll(Class<T> entityClass) {
        log.debug("Finding all entities {}", entityClass);
        PersistenceManager pm = pmf.getPersistenceManager();
        try {
            Query q = pm.newQuery(entityClass);
            return (List<T>) q.execute();
        }catch(JDOException e) {
            log.error("Error executing named query", e);
            throw new ServiceException("A data access error occured");
        }finally {
            pm.close();
        }
    }
    
    /**
     * Creates or merges an entity. If the entity does not exist, creates a new one else merges
     * this into existing entity.
     * @param <T> The generic entity type
     * @param entity The entity to persist
     * @return the newly created entity
     */
    public <T> T save(T entity) {
        log.debug("Creating a new entity {}",  entity);
        PersistenceManager pm = pmf.getPersistenceManager();
        Transaction tx = pm.currentTransaction();
        try {
            tx.begin();
            pm.makePersistent(entity);
            tx.commit();
            return entity;
        }catch(JDOException e) {
            log.error("Error executing named query", e);
            throw new ServiceException("A data access error occured");
        }finally {
            if(tx.isActive()) {
                tx.rollback();
            }
            pm.close();
        }
    }
    
    /**
     * Removes the specified entity from the data store.
     * @param entity 
     */
    public void remove(Persistable entity) {
        log.debug("Removing entity {}", entity);
        PersistenceManager pm = pmf.getPersistenceManager();
        Transaction tx = pm.currentTransaction();
        try {
            tx.begin();
            pm.deletePersistent(entity);
            tx.commit();
        }catch(JDOException e) {
            log.error("Error executing named query", e);
            throw new ServiceException("A data access error occured");
        }finally {
            if(tx.isActive()) {
                tx.rollback();
            }
            pm.close();
        }
    }
    
    /**
     * Executes a named query (with implicit parameters) that is expecting a given type.
     * 
     * @param <T> The expected type
     * @param entityClass The entity class of the returned type
     * @param queryName The name of the query as defined by @NamedQuery annotation on entity
     * @param params Additional query parameters.
     * @throws  IllegalArgumentException if the parameters are not in pairs.
     * @return a single result of the expected type or null if the query did not have any result.
     */
    public <T> T getByNamedQuery(Class<T> entityClass, String queryName, Object... params) {
        PersistenceManager pm = pmf.getPersistenceManager();
        Transaction tx = pm.currentTransaction();        
        try {
            tx.begin();
            Query q = pm.newNamedQuery(entityClass, queryName);
            q.setUnique(true);
            q.setResultClass(entityClass);
            
            log.debug("Executing named query '{}' {} with params {}", queryName, q.toString(), params);
            T result = (T) q.executeWithArray(params);
            q.closeAll();
            tx.commit();
            return result;
        }catch(JDOException e) {
            log.error("Error executing named query", e);
            throw new ServiceException("A data access error occured");
        }finally {
            if(tx.isActive()) {
                tx.rollback();
            }
            pm.close();
        }
    }
    
    
    /**
     * Executes a named query(with implicit parameters) that is expecting a given type.
     * 
     * @param <T> The expected type
     * @param entityClass The entity class of the returned type
     * @param queryName The name of the query as defined by @NamedQuery annotation on entity
     * @param params Additional query parameters. 
     * @throws  IllegalArgumentException if the parameters are not in pairs.
     * @return a single result of the expected type or null if the query did not have any result.
     */
    public <T> List<T> getAllByNamedQuery(Class<T> entityClass, String queryName, Object... params) {
        PersistenceManager pm = pmf.getPersistenceManager();
        Transaction tx = pm.currentTransaction();
        
        try {
            tx.begin();
            Query q = pm.newNamedQuery(entityClass, queryName);
            q.setResultClass(entityClass);
            List<T> result = (List<T>) q.executeWithArray(params);
            q.closeAll();
            tx.commit();
            return result;
        }catch(JDOException e) {
            log.error("Error executing named query", e);
            throw new ServiceException("A data access error occured");
        }finally {
            if(tx.isActive()) {
                tx.rollback();
            }
            pm.close();
        }
    }
    
    
    
    
    // ----------------------------------- Private Persistence API ---------------------------------
    
}
