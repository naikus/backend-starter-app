package org.starterapp.auth.shiro;

import org.apache.shiro.authc.AuthenticationToken;
import org.starterapp.auth.JsonWebToken;

/**
 * A shiro authentication token for jwt
 */
public class JWTAuthToken implements AuthenticationToken {
	private JsonWebToken token;
	
	public JWTAuthToken(JsonWebToken token) {
		this.token = token;
	}

	@Override
	public Object getPrincipal() {
		return token == null ? "" : token.getSubject();
	}

	@Override
	public Object getCredentials() {
		return token == null ? "" : token.getRawToken();
	}
	
	public JsonWebToken getToken() {
		return token;
	} 
	
	@Override
	public String toString() {
		return token != null ? token.getSubject() : "<empty>";
	}
	
}
