package org.restapp.auth.shiro;

import java.util.Arrays;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authz.AuthorizationFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An authz filter that matches only single role in roles array. If any one of the specified roles
 * matches, it succeeds.
 *
 * @author aniket
 */
@Named("RoleAuthzFilter")
@Singleton
public class RoleAuthzFilter extends AuthorizationFilter {
  private static final Logger LOG = LoggerFactory.getLogger(RoleAuthzFilter.class);

  private static final String AUTHENTICATE_HEADER = "WWW-Authenticate";
  private static final String AUTHORIZATION_SCHEME = "Bearer";

  @Override
  protected boolean isAccessAllowed(ServletRequest req, ServletResponse res, Object mappedValue)
      throws Exception {
    Subject subject = getSubject(req, res);
    String[] rolesArray = (String[]) mappedValue;

    LOG.info("Subject is {}", subject.getPrincipal());
    LOG.info("Mapped Roles are {}", Arrays.asList(rolesArray));

    if(rolesArray == null || rolesArray.length == 0) {
      return false;
    }

    for(String role : rolesArray) {
      if(subject.hasRole(role)) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected boolean onAccessDenied(ServletRequest req, ServletResponse res) {
    HttpServletResponse httpRes = WebUtils.toHttp(res);
    httpRes.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    String header = AUTHORIZATION_SCHEME + " realm=\"API\"";
    httpRes.setHeader(AUTHENTICATE_HEADER, header);
    return false; // do not process further
  }
}
