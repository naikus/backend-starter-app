package org.restapp.web.security.shiro;

import org.restapp.model.user.User;
import org.restapp.model.user.UserRole;
import org.restapp.services.PasswordEncoder;
import org.restapp.services.UserService;
import java.io.UnsupportedEncodingException;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A shiro security realm (security DB) that uses JPA and other services to provide authorization
 * and authentication data.
 * 
 * @author aniket
 */
@Named("DatastoreBackedRealm")
@Singleton
public class DatastoreBackedRealm extends AuthorizingRealm {
    private static final Logger log = LoggerFactory.getLogger(DatastoreBackedRealm.class.getSimpleName());
    
    @Inject
    private UserService userService;
    
    @Inject 
    private PasswordEncoder passwordEncoder;
    
    public DatastoreBackedRealm() {
        this.setCredentialsMatcher(new CredentialsMatcher() {
            @Override
            public boolean doCredentialsMatch(AuthenticationToken at, AuthenticationInfo ai) {
                UsernamePasswordToken token = (UsernamePasswordToken) at;
                SimpleAuthenticationInfo info = (SimpleAuthenticationInfo) ai;
                
                String rawPassword = new String(token.getPassword()), 
                        encodedPass = ai.getCredentials().toString();
                
                // ??
                log.debug("Matching passwod: {}, with {}", rawPassword, encodedPass);
                return passwordEncoder.matches(rawPassword, encodedPass, info.getCredentialsSalt().getBytes());
            }
        });
        this.setName("DatastoreBackedRealm");
    }
    
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authToken) 
            throws AuthenticationException {
        UsernamePasswordToken token = (UsernamePasswordToken) authToken;
        
        String userName = token.getUsername();
        User user = userService.getUserByUsername(userName);
        
        if(user != null) {
            try {
                // User id is the pricipal in our case. This means in our application code we
                // will get user id in SecurityContext.getUserPrincipal().getName()
                return new SimpleAuthenticationInfo(user.getId(), 
                        user.getPassword(), 
                        ByteSource.Util.bytes(userName.getBytes("UTF-8")), 
                        getName());
            }catch(UnsupportedEncodingException ue) {
                throw new AuthenticationException("Unsupported encoding", ue);
            }
        }else {
            return null;
        }
    }
        
    
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection pc) {
        Long userId =  (Long) pc.fromRealm(getName()).iterator().next();
        User user = userService.getUser(userId);
        if(user != null) {
            SimpleAuthorizationInfo authInfo = new SimpleAuthorizationInfo();
            UserRole userRole = user.getRole();
            if(userRole != null) {
                // log.debug("Permissions =========== {}", userRole.getPermissions());
                log.debug("Auth Info for {} with role {}", user.getUsername(), userRole.getName());
                
                for(String permission : userRole.getPermissions()) {
                    authInfo.addStringPermission(permission);
                }
                authInfo.addRole(userRole.getName());
            }else {
                log.info("User {} has no role associated", user.getUsername());
            }
            return authInfo;
        }else {
            return null;
        }
    }
    
}
