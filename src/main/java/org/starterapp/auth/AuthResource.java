package org.starterapp.auth;

import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.starterapp.api.ApiResponse;
import org.starterapp.api.BaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A resource for obtaining authentication information
 */
@Named
@Singleton
@Path("/authentication")
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource extends BaseResource {
	private static final Logger LOG = LoggerFactory.getLogger(AuthResource.class.getSimpleName());
	
	AuthenticationService authService;

  @Inject
  public AuthResource(AuthenticationService authService) {
    this.authService = authService;
  }
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createToken(Auth auth) {
    if(auth == null) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
		String username = auth.username, password = auth.password;
		if(authService.authenticate(username, password)) {
			JsonWebToken token = new JsonWebToken.Builder()
					.issuer(JwtSigningInfo.getIssuer())
					.subject(username)
					.jwtId(UUID.randomUUID().toString())
					.audience(JwtSigningInfo.getAudience())
					.sign(JwtSigningInfo.getSecret());
			return Response.ok(new Auth(username, null, token.getRawToken())).build();
		}else {
			return Response.status(Response.Status.UNAUTHORIZED)
					.entity(new ApiResponse("Invalid username or password", 0, true)).build();
		}
	}
  
  /*
  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response createToken(@FormParam("username") String userName, 
			@FormParam("password") String password) {
		return createToken(new Auth(userName, password, null));
	}
  */
	
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.TEXT_PLAIN)
	public Response createTextToken(@FormParam("username") String userName, 
			@FormParam("password") String password) {
		Response tokenRes = createToken(new Auth(userName, password, null));
    Object entity = tokenRes.getEntity();
    if(entity instanceof ApiResponse) {
      return Response.status(Response.Status.UNAUTHORIZED)
          .entity(((ApiResponse) entity).message).build();
    }else {
      return Response.ok(((Auth) entity).token).build();
    }
	}
}
