package org.starterapp.api;

import java.security.Principal;
import org.starterapp.usermgmt.UserService;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import org.starterapp.usermgmt.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A base class for all resources.
 *
 * @author aniket
 */
public abstract class UserAware {
  private static Logger LOG = LoggerFactory.getLogger(UserAware.class.getSimpleName());

  @Context
  protected SecurityContext securityContext;

  protected UserService userService;
  
  public UserAware(UserService uService) {
    this.userService = uService;
  }

  protected User currentUser() {
    Principal p = securityContext.getUserPrincipal();
    String id = p.getName();
    if(id == null) {
      throw new RuntimeException("No current user");
      // return null;
    }
    long userId = Long.valueOf(id);
    return userService.getUser(userId);
  }
}
