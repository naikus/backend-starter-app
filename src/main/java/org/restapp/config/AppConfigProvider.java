package org.restapp.config;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.inject.Named;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named
@Singleton
public class AppConfigProvider extends ConfigProvider {
  private static final Logger LOG = LoggerFactory.getLogger(AppConfigProvider.class.getSimpleName());
  public static final String CONFIG_FILE = "app.properties";
  public static final String ENV_PREFIX = "restapp";
  
  private Configuration configuration;
  
  public AppConfigProvider() {
    
  }

  @Override
  public Configuration get() {
    if(configuration == null) {
      configuration = load();
    }
    return configuration;
  }
  
  private Configuration load() {
    // load from app.properties
    Configuration fileConfig = loadFromProperties();
    // check prefix in app.properties and then load from env
    String prefix = fileConfig.getString("iotvertical.env.prefix");
    if(prefix == null || prefix.trim().length() == 0) {
      prefix = ENV_PREFIX;
    }
    Configuration envConfig = loadFromEnv(prefix);
    // merge
    fileConfig.merge(envConfig);
    return fileConfig;
  }
  
  private Configuration loadFromProperties() {
    InputStream in = null;
    Configuration conf = null;
    try {
      in = Thread.currentThread().getContextClassLoader().getResourceAsStream(CONFIG_FILE);
      Properties props = new Properties();
      props.load(in);
      conf = new Configuration(props);
    }catch(IOException e) {
      LOG.error("Cannot load configuration from" + CONFIG_FILE, e);
      conf = new Configuration();
    }finally {
      sneakyClose(in);
    }
    return conf;
  }
  
  private Configuration loadFromEnv(String configKeyPrefix) {
    Map<String, String> env = System.getenv();
    Map<String, Object> envProps = new HashMap<>();
    env.entrySet().stream()
        .filter((Map.Entry<String, String> e) -> {
          String key = e.getKey();
          String[] parts = key.split("_");
          return parts.length > 1 && parts[0].equalsIgnoreCase(configKeyPrefix);
        }).forEach((Map.Entry<String, String> e) -> {
            String key = e.getKey().replace("_", ".").toLowerCase();
            envProps.put(key, e.getValue());
        });
    LOG.info("Loaded environment properties {}", envProps);
    return new Configuration(envProps);
  }
  
  private void sneakyClose(Closeable c) {
    try {
      if(c != null) c.close();
    }catch(IOException ioe) {
      // ignore
    }
  }
}
