package org.starterapp.auth;

import java.io.InputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class JwtSigningInfo {
  private static final Logger LOG = LoggerFactory.getLogger(JwtSigningInfo.class.getSimpleName());
  public static final String DEFAULT_RESOURCE = "jwt.properties";
  private final Properties jwtProps;
  
  public JwtSigningInfo(Properties props) {
    this.jwtProps = props;
  }
  
  public String getSecret() {
    return jwtProps.getProperty("app.jwt.secret");
  }
  
  public String getIssuer() {
    return jwtProps.getProperty("app.jwt.issuer");
  }
  
  public String getAudience() {
    return jwtProps.getProperty("app.jwt.audience");
  }
  
  public static JwtSigningInfo load(String classpathResource) {
    Properties props = new Properties();
    try(InputStream in = Thread.currentThread().getContextClassLoader()
        .getResourceAsStream(classpathResource)) {
      props.load(in);
    }catch(Exception ioe) {
      LOG.error("Error loading jwt signing properties", ioe);
    }
    return new JwtSigningInfo(props);
  }
}
