package org.starterapp.persistence;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;

/**
 * An implementation of EMP that provides entity manager factory for this app
 *
 * @author aniket
 */
@Singleton
@Named("AppPMF")
public class AppPMFProvider extends PMFProvider {

  private static final PersistenceManagerFactory PMF
      = JDOHelper.getPersistenceManagerFactory("starterapp-model");

  @Override
  public PersistenceManagerFactory get() {
    return PMF;
  }

}
