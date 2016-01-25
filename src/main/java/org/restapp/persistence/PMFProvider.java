package org.restapp.persistence;

import javax.inject.Provider;
import javax.jdo.PersistenceManagerFactory;

/**
 * A class that provides an EntityManagerFactory
 * @author aniket
 */
public abstract class PMFProvider implements Provider<PersistenceManagerFactory> {
  @Override
  public abstract PersistenceManagerFactory get();
}
