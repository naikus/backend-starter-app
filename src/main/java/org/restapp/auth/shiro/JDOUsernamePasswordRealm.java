package org.restapp.auth.shiro;

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
import org.restapp.usermgmt.User;
import org.restapp.usermgmt.UserRole;
import org.restapp.auth.PasswordEncoder;
import org.restapp.usermgmt.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A shiro security realm (security DB) that uses JPA and other services to provide authorization
 * and authentication data.
 *
 * @author aniket
 */
@Named("JDOUsernamePasswordRealm")
@Singleton
public class JDOUsernamePasswordRealm extends AuthorizingRealm {
	private static final Logger LOG = LoggerFactory.getLogger
		(JDOUsernamePasswordRealm.class.getSimpleName());

	@Inject
	private UserService userService;

	@Inject
	private PasswordEncoder passwordEncoder;

	public JDOUsernamePasswordRealm() {
		this.setCredentialsMatcher(new CredentialsMatcher() {
			@Override
			public boolean doCredentialsMatch(AuthenticationToken at, AuthenticationInfo ai) {
				// LOG.info("Authentication Info {}", ai);
				if(ai == null) {
					return false;
				}
				UsernamePasswordToken token = (UsernamePasswordToken) at;
				SimpleAuthenticationInfo info = (SimpleAuthenticationInfo) ai;
				
				String rawPassword = new String(token.getPassword()),
						encodedPass = ai.getCredentials().toString();
				
				// LOG.info("\n{}\n{}", rawPassword, encodedPass);
				return passwordEncoder.matches(rawPassword, encodedPass,
						info.getCredentialsSalt().getBytes());
			}
		});
		this.setName(JDOUsernamePasswordRealm.class.getSimpleName());
	}

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authToken)
			throws AuthenticationException {
		UsernamePasswordToken token = (UsernamePasswordToken) authToken;
		token.setRememberMe(false);

		String userName = token.getUsername();
		// LOG.info("Token authentication info: {}-{}", userName, token.getPassword());
    User user = userService.getUserByEmail(userName);
		LOG.info("Authentication User {}", user);

		if (user != null) {
			user.getRole().getPermissions();
			try {
            // User id is the pricipal in our case. This means in our application code we
				// will get user id in SecurityContext.getUserPrincipal().getName()
				byte[] salt = userName.getBytes(PasswordEncoder.DEFAULT_ENCODING);
				ByteSource saltSource = ByteSource.Util.bytes(salt);
				return new SimpleAuthenticationInfo(user.getId(),
						user.getPassword(),
						saltSource,
						getName());
			} catch (UnsupportedEncodingException ue) {
				throw new AuthenticationException("Unsupported encoding", ue);
			}
		} else {
			return null;
		}
	}

	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection pc) {
		Long userId = (Long) pc.fromRealm(getName()).iterator().next();
		User user = userService.getUser(userId);
		// User user = (User) pc.fromRealm(getName()).iterator().next();
		LOG.info("User Authz {}", user);

		if (user != null) {
			SimpleAuthorizationInfo authzInfo = new SimpleAuthorizationInfo();
      UserRole userRole = user.getRole();
			LOG.info("User Authz Role {}", userRole);
			if (userRole != null) {
				LOG.debug("Permissions for role {}: {}", userRole, userRole.getPermissions());
				for (String permission : userRole.getPermissions()) {
					authzInfo.addStringPermission(permission);
				}
				authzInfo.addRole(userRole.getName());
			} else {
				LOG.info("User {} has no role associated", user.getEmail());
			}
			return authzInfo;
		} else {
			return null;
		}
	}

}
