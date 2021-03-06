package org.starterapp.auth.shiro;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.starterapp.usermgmt.User;
import org.starterapp.usermgmt.UserRole;
import org.starterapp.usermgmt.UserService;
import org.starterapp.auth.JsonWebToken;
import org.starterapp.auth.JwtSigningInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An authorizing realm that uses JSON Web Token for API authentication an authorization
 */
@Named("JDOJwtRealm")
@Singleton
public class JDOJwtRealm extends AuthorizingRealm {
	private static final Logger LOG = LoggerFactory.getLogger(JDOJwtRealm.class.getSimpleName());
	
	private UserService userService;
	// private String secret = JwtSigningInfo.getSecret();
  private JwtSigningInfo signingInfo;
	
  @Inject
	public JDOJwtRealm(UserService us) {
    this.userService = us;
		this.setName(JDOJwtRealm.class.getSimpleName());
		this.setAuthenticationTokenClass(JWTAuthToken.class);
    this.signingInfo = JwtSigningInfo.load(JwtSigningInfo.DEFAULT_RESOURCE);
		this.setCredentialsMatcher(new CredentialsMatcher() {
      String secret = signingInfo.getSecret();
      
			@Override
			public boolean doCredentialsMatch(AuthenticationToken at, AuthenticationInfo ai) {
				if(ai == null) {
					return false;
				}
        JWTAuthToken token = (JWTAuthToken) at;
        JsonWebToken jwt = token.getToken();
				if(jwt == null) {
					LOG.info("Token not found");
					return false;
				}
				
				if(jwt.isExpired()) {
					LOG.info("Expired token {}", jwt.getIssuedAt());
					return false;
				}
				
				if(!jwt.verify(secret)) {
					LOG.info("Token verification failed");
					return false;
				}
				
				PrincipalCollection pc = ai.getPrincipals();
				// User user = (User) pc.fromRealm(JDOJwtRealm.this.getName()).iterator().next();
				Long userId = (Long) pc.fromRealm(getName()).iterator().next();
        User user = userService.getUser(userId);
				
				if(! user.getEmail().equals(token.getPrincipal())) {
					LOG.info("Principals don't match {}-{}", token.getPrincipal(), user.getEmail());
					return false;
				}
				
				return true;
			}
		});
	}

	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection pc) {
		Long userId = (Long) pc.fromRealm(getName()).iterator().next();
		User user = userService.getUser(userId);
		// User user = (User) pc.fromRealm(getName()).iterator().next();
		
		if(user != null) {
			SimpleAuthorizationInfo authzInfo = new SimpleAuthorizationInfo();
      UserRole userRole = user.getRole();
			LOG.debug("User Authz Role {}", userRole);
			if(userRole != null) {
				LOG.debug("Permissions for role {}: {}", userRole, userRole.getPermissions());
        // DO NOT USE stream() api! I messes up with JDO objects!
        userRole.getPermissions().forEach((permission) -> {
          authzInfo.addStringPermission(permission);
        });
				authzInfo.addRole(userRole.getName());
			}else {
				LOG.info("User {} has no role associated", user.getEmail());
			}
			return authzInfo;
		}else {
			return null;
		}
	}

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken at) 
			throws AuthenticationException {
		JWTAuthToken token = (JWTAuthToken) at;
		String userEmail = (String) token.getPrincipal();
		User user = userService.getUserByEmail(userEmail);
		LOG.info("Getting authentication info for {}", user);
		
		if(user != null) {
			return new SimpleAuthenticationInfo(user.getId(), null, getName());
		}
		return null;
	}
	
}
