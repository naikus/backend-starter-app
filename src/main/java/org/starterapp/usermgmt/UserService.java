package org.starterapp.usermgmt;

import org.starterapp.persistence.PMFProvider;
import java.io.UnsupportedEncodingException;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.starterapp.auth.PasswordEncoder;
import org.starterapp.persistence.PersistenceService;
import org.starterapp.services.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author aniket
 */
@Named
@Singleton
public class UserService {
  private static final Logger LOG = LoggerFactory.getLogger(UserService.class.getSimpleName());

  private PersistenceService persistence;

  @Inject
  private PasswordEncoder passwordEncoder;

  @Inject
  @Named("AppPMF")
  public UserService(PMFProvider provider) {
    persistence = new PersistenceService(provider);
  }

  public List<User> getAllUsers() {
    return persistence.findAll(User.class);
  }

  public User getUser(long userId) {
    return persistence.findById(User.class, userId);
  }

  public User addUser(User user, String role) {
    if(user == null) {
      throw new IllegalArgumentException("User cannot be null");
    }

    User existing = getUserByEmail(user.getEmail());
    if(existing != null) {
      throw new ServiceException("User with username '" + user.getEmail() + "' already exists");
    }
    // encode user's password
    encodePassword(user);

    // check if role exists
    UserRole existingRole = persistence.getByNamedQuery(UserRole.class, "UserRole.findByName", role);
    if(existingRole == null) {
      throw new ServiceException(String.format("Role %s does not exist", role));
    }

    user.setRole(existingRole);
    return persistence.save(user);
  }

  public User updateUser(User user) {
    User existing = persistence.find(user);
    if(user == null) {
      throw new ServiceException("Could not find specified user");
    }

    // encode user's password
    encodePassword(user);

    // check if the role has changed
    UserRole role = user.getRole();
    if(role != null) {
      role = persistence.getByNamedQuery(UserRole.class, "UserRole.findByName", role.getName());
      if(role == null) {
        throw new ServiceException(String.format("Role %s does not exist", role));
      }
    }
    existing.setRole(role);
    return persistence.save(existing);
  }

  public User getUserByEmail(String email) {
    return persistence.getByNamedQuery(User.class, "User.findByEmail", email);
  }

  public UserRole addRole(UserRole role) {
    return persistence.save(role);
  }

  public UserRole getRoleByName(String roleName) {
    return persistence.getByNamedQuery(UserRole.class, "UserRole.findByName", roleName);
  }

  private String encodePassword(String password, String salt) {
    try {
      return passwordEncoder.encode(password, salt.getBytes("UTF-8"));
    }catch(UnsupportedEncodingException ue) {
      throw new ServiceException("Could not encode password");
    }
  }

  private void encodePassword(User user) {
    String password = user.getPassword(), username = user.getEmail();
    if(password == null || username == null) {
      throw new IllegalArgumentException("User username or password cannot be null");
    }
    user.setPassword(encodePassword(password, username));
  }
}
