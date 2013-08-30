package org.restapp.da;

import javax.jdo.PersistenceManagerFactory;

/**
 * A class that provides an EntityManagerFactory
 * @author aniket
 */
public abstract class EMP {
    public abstract PersistenceManagerFactory get();    
}
