package org.starterapp.auth;

import org.starterapp.usermgmt.UserService;
import java.io.UnsupportedEncodingException;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.starterapp.services.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.starterapp.usermgmt.User;

@Named
@Singleton
public class AuthenticationService {
  private static final Logger LOG = LoggerFactory.getLogger(
      AuthenticationService.class.getSimpleName());
  
	private final UserService userService;
	private final PasswordEncoder passwordEncoder;
  
  @Inject
  public AuthenticationService(UserService uService, PasswordEncoder encoder) {
    this.userService = uService;
    this.passwordEncoder = encoder;
  }
	
	public boolean authenticate(String username, String password) {
		if(username == null) {
			return false;
		}
    User user = userService.getUserByEmail(username);
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
