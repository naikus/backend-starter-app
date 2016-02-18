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
import org.starterapp.api.UserAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.starterapp.usermgmt.UserService;

/**
 * A resource for obtaining authentication information
 */
@Named
@Singleton
@Path("/authentication")
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource extends UserAware {
	private static final Logger LOG = LoggerFactory.getLogger(AuthResource.class.getSimpleName());
	
	private final AuthenticationService authService;
  private JwtSigningInfo signingInfo;

  @Inject
  public AuthResource(AuthenticationService authService, UserService uService) {
    super(uService);
    this.authService = authService;
    this.signingInfo = JwtSigningInfo.load(JwtSigningInfo.DEFAULT_RESOURCE);
  }
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response authenticate(Auth auth) {
    if(auth == null) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
		String username = auth.username, password = auth.password;
		if(authService.authenticate(username, password)) {
			JsonWebToken token = new JsonWebToken.Builder()
					.issuer(signingInfo.getIssuer())
					.subject(username)
					.jwtId(UUID.randomUUID().toString())
					.audience(signingInfo.getAudience())
					.sign(signingInfo.getSecret());
			return Response.ok(new Auth(username, null, token.getRawToken())).build();
		}else {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
	}
  
	
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.TEXT_PLAIN)
	public Response createTextToken(@FormParam("username") String userName, 
			@FormParam("password") String password) {
		return authenticate(new Auth(userName, password, null));
	}
}
