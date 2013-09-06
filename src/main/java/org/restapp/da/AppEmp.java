package org.restapp.da;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;

/**
 * An implementation of EMP that provides entity manager factory for this app
 * @author aniket
 */
@Singleton
@Named("AppEmp")
public class AppEmp extends EMP {
    private static PersistenceManagerFactory pmf;
    
    public AppEmp() {
        pmf = JDOHelper.getPersistenceManagerFactory("restapp-model");
        pmf.setDetachAllOnCommit(true);
    };
    
    @Override
    public PersistenceManagerFactory get() {
        return pmf;
    }
    
}
