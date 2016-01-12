package org.restapp.web.security.shiro;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.util.WebUtils;
import org.restapp.web.security.JsonWebToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
@Named("JWTTokenAuthcFilter")
@Singleton
public class JWTTokenAuthcFilter extends AuthenticatingFilter {
	private static final String AUTHORIZATION_HEADER = "Authorization";
	private static final String AUTHENTICATE_HEADER = "WWW-Authenticate";
	private static final String AUTHORIZATION_SCHEME = "Bearer";
	
	private static final Logger LOG = LoggerFactory.getLogger(JWTTokenAuthcFilter.class.getSimpleName());

	@Override
	protected AuthenticationToken createToken(ServletRequest req, ServletResponse res) throws Exception {
		HttpServletRequest httpReq = WebUtils.toHttp(req);
		String authorization = httpReq.getHeader(AUTHORIZATION_HEADER);
		LOG.info("Authorization header {}", authorization);
		if(authorization == null || authorization.length() == 0) {
			return createEmptyToken();
		}
		return createToken(authorization);
	}
	
	@Override
	protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, 
			Object mappedValue) {
		try {
			AuthenticationToken token = createToken(request, response);
			if(token.getPrincipal() != null) {
				Subject sub = getSubject(request, response);
				sub.login(token);
				return true;
			}
			return false;
		} catch (Exception ex) {
			LOG.error(ex.getMessage(), ex);
			return false;
		}
	}

	@Override
	protected boolean onAccessDenied(ServletRequest req, ServletResponse res) throws Exception {
		sendChallenge(req, res);
		return false; // do not process further
	}
	
	private void sendChallenge(ServletRequest req, ServletResponse res) throws Exception {
		HttpServletResponse httpRes = WebUtils.toHttp(res);
		httpRes.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		String header = AUTHORIZATION_SCHEME + " realm=\"API\"";
		httpRes.setHeader(AUTHENTICATE_HEADER, header);
		LOG.debug("Sending challenge {}", header);
	}
	
	private AuthenticationToken createToken(String authorization) {
		String[] schemeAndToken = authorization.split(" ");
		String token = null;
		if(schemeAndToken.length == 2) {
			token = schemeAndToken[1];
		}else {
			token = schemeAndToken[0];
		}
    JsonWebToken jwt = new JsonWebToken(token);
		LOG.debug("Created jwt from auth header {}", jwt.getRawToken());
		return new JWTAuthToken(jwt);
	}
	
	private AuthenticationToken createEmptyToken() {
		LOG.debug("Created empty token");
		return new JWTAuthToken(null);
	}
	
}
