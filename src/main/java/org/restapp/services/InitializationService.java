package org.restapp.services;

import org.restapp.model.user.UserRole;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.restapp.model.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * App specific initialization
 * 
 * @author aniket
 */
@Named
@Singleton
public class InitializationService {
    private static final Logger log = LoggerFactory.getLogger(InitializationService.class.getSimpleName());
    
    @Inject
    private UserService userService;
    
    @PostConstruct
    private void init() {
        
        User su = userService.getUserByUsername("superuser");
        if(su == null) {
            log.info("First time init. Adding super user...");

            // create a new roles superuser and appuser
            UserRole suRole = new UserRole("superuser", "A user to rule them all!!");
            suRole.addPermission("users:read")
                    .addPermission("users:create")
                    .addPermission("users:delete");
            suRole = userService.addRole(suRole);
            
            UserRole appUserRole = new UserRole("appuser", "A normal app user");
            appUserRole.addPermission("users:read")
                    .addPermission("users:update");
            appUserRole = userService.addRole(appUserRole);            
            
            log.info("Created Roles {} and {}", suRole.getName(), appUserRole.getName());
            
            // add the user
            su = new User("superuser", "password", suRole);
            su.setFirstName("Super");
            su.setLastName("User");
            su.setEmail("super@example.com");

            su = userService.addUser(su, "superuser");
            log.info("Added superuser {} {}, " + su.getFirstName(), su.getLastName());
        }
    }
}
