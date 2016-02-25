package org.starterapp.services;

import org.starterapp.usermgmt.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.starterapp.config.ConfigProvider;
import org.starterapp.config.Configuration;
import org.starterapp.usermgmt.User;
import org.starterapp.usermgmt.UserRole;

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
    
    private final UserService userService;    
    private final Configuration appConfig;
    
    @Inject
    public InitializationService(ConfigProvider provider, UserService uService) {
      this.userService = uService;
      appConfig = provider.get();
      LOG.info("Loaded configuration\n{}", appConfig);
    }
	 
    @PostConstruct
    private void init() {
        User su = userService.getUserByEmail("superuser@example.com");
        if(su == null) {
            LOG.info("Clean startup!");
            LOG.info("-------------------------------------------------------------------------");
            
            // create a new roles superuser and appuser
            UserRole suRole = new UserRole("superuser", "A user to rule them all!!");
            suRole.addPermission("users:read")
                .addPermission("users:create")
                .addPermission("users:update")
                .addPermission("users:delete");
            LOG.info("Adding role '{}'...", suRole.getName());
            suRole = userService.addRole(suRole);
            
            UserRole appUserRole = new UserRole("appuser", "A normal app user");
            appUserRole.addPermission("users:read")
                .addPermission("users:update");
            LOG.info("Adding role '{}'...", appUserRole.getName());
            appUserRole = userService.addRole(appUserRole);
            
            LOG.info("Created Roles {} and {}", suRole.getName(), appUserRole.getName());
            
            // add the user
            su = new User("superuser@example.com", "password", suRole);
            su.setFirstName("Super");
            su.setLastName("User");
            LOG.info("Adding user '{}' with role {}", su.getEmail(), suRole.getName());
            userService.addUser(su, suRole.getName());
            
            LOG.info("Initialization complete!");
            LOG.info("-------------------------------------------------------------------------");
        }
    }
}