package org.restapp.web.security.shiro;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authz.AuthorizationFilter;

/**
 * An authz filter that matches only single role in roles array.
 * If any one of the specified roles matches, it succeeds.
 * @author aniket
 */
@Named("RoleAuthzFilter")
@Singleton
public class RoleAuthzFilter extends AuthorizationFilter {
    
    @Override
    protected boolean isAccessAllowed(ServletRequest req, ServletResponse res, Object mappedValue) 
            throws Exception {
        Subject subject = getSubject(req, res);
        String[] rolesArray = (String[]) mappedValue;
        
        if(rolesArray == null || rolesArray.length == 0) {
            return true;
        }
        
        for(String role: rolesArray) {
            if(subject.hasRole(role)) {
                return true;
            }
        }
        return false;
    }
}
