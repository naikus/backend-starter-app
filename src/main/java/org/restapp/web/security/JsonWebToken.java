package org.restapp.web.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.ReadOnlyJWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A JWT implementation of authentication token
 */
public class JsonWebToken {
	private static final Logger LOG = LoggerFactory.getLogger(JsonWebToken.class.getSimpleName());
	public static final String DEFAULT_ISSUER = "http://www.restapp.com";
	public static final String DEFAULT_AUDIENCE = "http://www.restapp.com/api";
	
	private String rawToken;
	
	private SignedJWT jwt;
	private JWSHeader header;
	private ReadOnlyJWTClaimsSet claims;
	
	public JsonWebToken(String token) throws RuntimeException {
		this.rawToken = token;
		try {
			jwt = SignedJWT.parse(rawToken);
			header = jwt.getHeader();
			claims = jwt.getJWTClaimsSet();
			
			LOG.info("Header: {}\n Claims: {}", header, claims);
		}catch(ParseException pe) {
			throw new RuntimeException("Unparsable token", pe);
		}
	}
	
	private JsonWebToken(SignedJWT jwt) {
		try {
			this.jwt = jwt;
			this.header = jwt.getHeader();
			this.claims = jwt.getJWTClaimsSet();
		}catch(ParseException pe) {
			throw new RuntimeException("Unparsable token", pe);
		}
	}
	
	public boolean isExpired() {
		long current = new Date().getTime() + 120000; // a 2 minute leeway
		long expDate = claims.getExpirationTime().getTime();
		return expDate < current;
	}
	
	public String getSubject() {
		return claims.getSubject();
	}
	
	public String getType() {
		return claims.getType();
	}
	
	public String getAlgorithm() {
		return header.getAlgorithm().getName();
	}
	
	public String getIssuer() {
		return claims.getIssuer();
	}
	
	public Date getIssuedAt() {
		return claims.getIssueTime();
	}
	
	public Date getExpiresOn() {
		return claims.getExpirationTime();
	}
	
	public boolean verify(String secret) {
		JWSVerifier verifier = new MACVerifier(secret);
		try {
			return jwt.verify(verifier);
		}catch(JOSEException je) {
			throw new RuntimeException("Could not verify token", je);
		}
	}
	
	public String getRawToken() {
		return jwt.serialize();
	}

	@Override
	public String toString() {
		return rawToken;
	}
	
	public final static class Builder {
		public static enum SigningAlgorithm {
			HS256,
			HS384,
			HS512 
		}
		
		private static final long DEFAULT_EXP = 10 * 60 * 1000;
		
		private String jti;
		private String typ;
		private SigningAlgorithm alg = SigningAlgorithm.HS512;
		private String iss;
		private String sub;
		private String aud;
		private Date iat;
		private Date exp;
		private Map<String, Object> claims = new HashMap<>();
		
		public Builder() {
			 type("JWT");
			 issuedAt(new Date());
		}

		public Builder type(String typ) {
			this.typ = (typ == null ? "JWT" : typ);
			return this;
		}
		
		public Builder algorithm(SigningAlgorithm alg) {
			this.alg = (alg == null ? SigningAlgorithm.HS512 : alg);
			return this;
		}
		
		public Builder issuer(String iss) {
			this.iss = iss;
			return this;
		}
		
		public Builder subject(String sub) {
			this.sub = sub;
			return this;
		}
		
		public Builder audience(String aud) {
			this.aud = aud;
			return this;
		}
		
		public Builder issuedAt(Date iat) {
			this.iat = iat;
			return this.expires(new Date(iat.getTime() + DEFAULT_EXP));
		}
		
		public Builder expires(Date exp) {
			this.exp = exp;
			return this;
		}
		
		public Builder jwtId(String id) {
			this.jti = id;
			return this;
		}
		
		public Builder claim(String name, Object value) {
			claims.put(jti, value);
			return this;
		}
		
		private SignedJWT build() {
			JWSHeader header = new JWSHeader.Builder(
					new JWSAlgorithm(this.alg.name())).type(JOSEObjectType.JWS).build();
			JWTClaimsSet claimSet = new JWTClaimsSet();
			claimSet.setIssuer(iss);
			claimSet.setJWTID(jti);
			claimSet.setIssuer(iss);
			claimSet.setSubject(sub);
			claimSet.setAudience(aud);
			claimSet.setIssueTime(iat);
			claimSet.setExpirationTime(exp);
			claimSet.setType(typ);
			claimSet.setAllClaims(claims);
			
			SignedJWT jwt = new SignedJWT(header, claimSet);
			return jwt;
		}
		
		public JsonWebToken get() {
			SignedJWT jwt = build();
			return new JsonWebToken(jwt);
		}
		
		public JsonWebToken sign(String secret) throws RuntimeException {
			SignedJWT jwt = build();
			JWSSigner macSigner = new MACSigner(secret);
			try {
				jwt.sign(macSigner);
				return new JsonWebToken(jwt);
			}catch(JOSEException e) {
				throw new RuntimeException("Error signing JWT", e);
			}
		}
	}
}
