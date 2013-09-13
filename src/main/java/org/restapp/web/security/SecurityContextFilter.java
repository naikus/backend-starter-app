package org.restapp.web.security;


import java.io.IOException;
import javax.inject.Named;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import org.restapp.security.AppSecurityContext;

/**
 * A JAX-RS Container request and response filter that sets the appropriate application security
 * context for all the parts of the application to access the current subject
 */
@Named
@Provider
public class SecurityContextFilter implements ContainerRequestFilter, ContainerResponseFilter {
    @Context SecurityContext secContext;

    @Override
    public void filter(ContainerRequestContext crc) throws IOException {
        long userId = Long.valueOf(secContext.getUserPrincipal().getName());
        AppSecurityContext.create(userId);
    }

    @Override
    public void filter(ContainerRequestContext crc, ContainerResponseContext crc1) throws IOException {
        AppSecurityContext.remove();
    }
}
