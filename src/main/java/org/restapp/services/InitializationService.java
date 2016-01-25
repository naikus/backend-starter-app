package org.restapp.services;

import org.restapp.usermgmt.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.restapp.config.ConfigProvider;
import org.restapp.config.Configuration;
import org.restapp.usermgmt.User;
import org.restapp.usermgmt.UserRole;

/**
 * App specific initialization
 * 
 * @author aniket
 */
@Named
@Singleton
public class InitializationService {
    private static final Logger LOG = LoggerFactory.getLogger(
			 InitializationService.class.getSimpleName());
    
    @Inject
    private UserService userService;
    
    private Configuration appConfig;
    
    @Inject
    public InitializationService(ConfigProvider provider) {
      appConfig = provider.get();
      LOG.info("Loaded configuration\n{}", appConfig);
    }
	 
    @PostConstruct
    private void init() {
        User su = userService.getUserByEmail("superuser@example.com");
        if(su == null) {
            LOG.info("First time init. Adding super user...");

            // create a new roles superuser and appuser
            UserRole suRole = new UserRole("superuser", "A user to rule them all!!");
            suRole.addPermission("users:read")
                .addPermission("users:create")
                .addPermission("users:update")
                .addPermission("users:delete");
            
            suRole = userService.addRole(suRole);
            
            UserRole appUserRole = new UserRole("appuser", "A normal app user");
            appUserRole.addPermission("users:read")
                    .addPermission("users:update");
            appUserRole = userService.addRole(appUserRole);            
            
            LOG.info("Created Roles {} and {}", suRole.getName(), appUserRole.getName());
            
            // add the user
            su = new User("superuser@example.com", "password", suRole);
            su.setFirstName("Super");
            su.setLastName("User");

            su = userService.addUser(su, "superuser");
            LOG.info("Added superuser {} {}, ", su.getFirstName(), su.getLastName());
        }
    }
}