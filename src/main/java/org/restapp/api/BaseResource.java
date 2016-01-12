package org.restapp.api;

import org.restapp.services.UserService;
import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import org.restapp.model.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A base class for all rest resources.
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
