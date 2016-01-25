package org.starterapp.auth;

import org.starterapp.auth.PasswordEncoder;
import org.starterapp.usermgmt.UserService;
import java.io.UnsupportedEncodingException;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.starterapp.services.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Authentication service 
 */
@Named
@Singleton
public class AuthenticationService {
  private static final Logger LOG = LoggerFactory.getLogger(AuthenticationService.class.getSimpleName());
  
	@Inject
	private UserService userService;
	
	@Inject
	private PasswordEncoder passwordEncoder;
	
	public boolean authenticate(String username, String password) {
    LOG.info("Username and password {}, {}", username, password);
		if(username == null) {
			return false;
		}
		  org.starterapp.usermgmt.User user = userService.getUserByEmail(username);
		if(user == null) {
			return false;
		}
		
		try {
			return passwordEncoder.matches(password, user.getPassword(), 
					username.getBytes(PasswordEncoder.DEFAULT_ENCODING));
		}catch(UnsupportedEncodingException uee) {
			throw new ServiceException(uee);
		}
	}
}
