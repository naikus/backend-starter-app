package org.restapp.api;

import java.util.List;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import org.restapp.model.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * A resource to manage controllers
 *
 * GET /users get all users GET /users/{id} get a user with id POST /users create a new user POST
 * /users/{id} update a user PUT /users/{id} update a user DELETE /users/{id} delete a user
 */
@Singleton
@Named
@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
public class UserResource extends BaseResource {
  private static final Logger LOG = LoggerFactory.getLogger(UserResource.class.getSimpleName());

  @GET
  public List<User> getUsers() {
    User user = currentUser(); //retrieve the curently requesting accont
    return userService.getAllUsers();
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public Response addUser(User u) {
    User user = currentUser();
    User newUser = userService.addUser(u, u.getRole().getName());
    return Response.created(UriBuilder.fromPath("/users/{id}").build(newUser.getId()))
        .entity(newUser).build();
  }

  @GET
  @Path("/{id}")
  public Response getUser(@PathParam("id") long id) {
    User current = currentUser();
    User found = userService.getUser(id);
    if(found == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    return Response.ok(found).build();
  }
}
