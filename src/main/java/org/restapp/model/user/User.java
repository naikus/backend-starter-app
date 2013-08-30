package org.restapp.model.user;


import org.restapp.model.Persistable;
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
    @Query(name = "User.findByUsername", 
            value = "select from org.restapp.model.user.User where username == :username")
})
@Index(members = {"username"})
public class User extends Persistable {

    @Persistent(nullValue = NullValue.EXCEPTION)
    @Unique
    @NotNull(message = "user.username")
    private String username;
    
    @Persistent(nullValue = NullValue.EXCEPTION)
    @NotNull(message = "user.password")
    private String password;
    
    private String firstName;
    private String lastName;
    
    @NotNull(message = "user.email")
    private String email;
    
    @Persistent(defaultFetchGroup = "true")
    @Join
    private UserRole role;

    
    public User() {}
    
    public User(String username, String password, UserRole role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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
    
    @Override
    public String toString() {
        return getId() + ":" + getUsername() + ":" + getPassword();
    }
}
