package org.starterapp.api;

import org.starterapp.usermgmt.UserService;
import javax.inject.Inject;
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
public abstract class BaseResource {
  private static Logger LOG = LoggerFactory.getLogger(BaseResource.class.getSimpleName());

  @Context
  protected SecurityContext securityContext;

  @Inject
  protected UserService userService;

  protected User currentUser() {
    long userId = Long.valueOf(securityContext.getUserPrincipal().getName());
    return userService.getUser(userId);
  }
}
