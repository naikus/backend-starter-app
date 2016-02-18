package org.starterapp.auth;

import java.io.IOException;
import java.security.Principal;
import javax.inject.Named;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A JAX-RS Container request and response filter that sets the appropriate application security
 * context for all the parts of the application to access the current subject
 */
@Named
@Provider
public class SecurityContextFilter implements ContainerRequestFilter, ContainerResponseFilter {
	 private static final Logger LOG = LoggerFactory.getLogger(
       SecurityContextFilter.class.getSimpleName());
	 
    @Context SecurityContext secContext;

    @Override
    public void filter(ContainerRequestContext crc) throws IOException {
		  Principal p = secContext.getUserPrincipal();
		  LOG.info("URI info {}", crc.getUriInfo().getPath());
		  LOG.info("Auth scheme {}", secContext.getAuthenticationScheme());
		  if(p != null) {
			  LOG.info("User Principal {}", p.getClass());
				long userId = Long.valueOf(p.getName());
        AppSecurityContext.create(userId);
		  }
    }

    @Override
    public void filter(ContainerRequestContext crc, ContainerResponseContext crc1) throws IOException {
        AppSecurityContext.remove();
    }
}
