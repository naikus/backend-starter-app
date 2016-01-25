package org.starterapp.usermgmt;

import org.starterapp.persistence.Persistable;
import javax.jdo.annotations.Index;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.Query;
import javax.jdo.annotations.Unique;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@PersistenceCapable(detachable = "true", cacheable = "true")
@Queries({
  @Query(name = "User.findByEmail",
      value = "select from org.starter.usermgmt.User where email == :email")
})
public class User extends Persistable {
  @Persistent(nullValue = NullValue.EXCEPTION)
  @NotNull(message = "user.password.notnull")
  private String password;

  private String firstName;
  private String lastName;

  @NotNull(message = "user.email.notnull")
  @Unique
  @Index(name = "email")
  private String email;

  @Persistent(defaultFetchGroup = "true")
  @Join
  private UserRole role;

  public User() {
  }

  public User(String email, String password, UserRole role) {
    this.email = email;
    this.password = password;
    this.role = role;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public UserRole getRole() {
    return role;
  }

  public void setRole(UserRole role) {
    this.role = role;
  }

  public String getFullName() {
    String fName = getFirstName(), lName = getLastName();
    if(fName == null && lName == null) {
      return getEmail();
    }
    return fName != null ? fName + (lName != null ? ", " + lName : "") : lName;
  }

  @Override
  public String toString() {
    return new StringBuilder("User(")
        .append("fullName=").append(this.getFullName())
        .append(", email=").append(this.getEmail()).append(")").toString();
  }
}
