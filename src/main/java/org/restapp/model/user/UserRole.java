package org.restapp.model.user;

import org.restapp.model.Persistable;
import java.util.HashSet;
import java.util.Set;
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
  @Query(name = "UserRole.findByName",
      value = "select from org.restapp.model.user.UserRole where name == :name")
})
public class UserRole extends Persistable {
  @Persistent(nullValue = NullValue.EXCEPTION)
  @Index
  @Unique
  @NotNull(message = "userrole.name.notnull")
  private String name;

  @Persistent(nullValue = NullValue.NONE)
  private String description;

  @Persistent(defaultFetchGroup = "true")
  @Join
  private Set<String> permissions = new HashSet<String>();

  public UserRole() {
  }

  public UserRole(String name, String desc) {
    this.name = name;
    this.description = desc;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public Set<String> getPermissions() {
    return permissions;
  }

  public void setPermissions(Set<String> permissions) {
    this.permissions = permissions;
  }

  public UserRole addPermission(String permission) {
    getPermissions().add(permission);
    return this;
  }

  public boolean hasPermission(String permission) {
    return getPermissions().contains(permission);
  }

  @Override
  public String toString() {
    return getName();
  }
}
