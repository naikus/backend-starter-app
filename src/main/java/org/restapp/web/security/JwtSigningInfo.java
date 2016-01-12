package org.restapp.web.security;

import java.io.InputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class JwtSigningInfo {
  private static final Logger LOG = LoggerFactory.getLogger(JwtSigningInfo.class.getSimpleName());
  private static final Properties props = new Properties();
  
  static {
    loadInfo();
  }
  
  private static void loadInfo() {
    try(InputStream in = Thread.currentThread().getContextClassLoader()
        .getResourceAsStream("jwt.properties")) {
      props.load(in);
    }catch(Exception ioe) {
      LOG.error("Error loading jwt signing properties");
    }
  }
  
  public static String getSecret() {
    return props.getProperty("app.jwt.secret");
  }
  
  public static String getIssuer() {
    return props.getProperty("app.jwt.issuer");
  }
  
  public static String getAudience() {
    return props.getProperty("app.jwt.audience");
  }
}
