package org.starterapp.config;

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
  public static final String ENV_PREFIXES = "starterapp";
  
  private Configuration configuration;
  
  @Override
  public Configuration get() {
    if(configuration == null) {
      configuration = load();
    }
    return configuration;
  }
  
  private Configuration load() {
    // load from app.properties
    Configuration fileConfig = loadFromConfigFile();
    // check prefix in app.properties and then load from env
    String prefixes = fileConfig.getString("config.env.prefixes");
    if(prefixes == null || prefixes.trim().length() == 0) {
      prefixes = ENV_PREFIXES;
    }
    
    Configuration envConfig = new Configuration(),
        sysPropsConfig = new Configuration();
    for(String prefix : prefixes.split("\\s")) {
      // merge
      envConfig.merge(loadFromEnv(prefix));
      sysPropsConfig.merge(loadFromProperties(prefix));
    }
    LOG.info("Loaded environment properties \n{}", envConfig);
    LOG.info("Loaded system properties \n{}", sysPropsConfig);
    fileConfig.merge(envConfig);
    fileConfig.merge(sysPropsConfig);
    return fileConfig;
  }
  
  private Configuration loadFromConfigFile() {
    InputStream in = null;
    Configuration conf = null;
    try {
      in = Thread.currentThread().getContextClassLoader().getResourceAsStream(CONFIG_FILE);
      Properties props = new Properties();
      if(in != null) {
        props.load(in);
      }else {
        LOG.warn("Configuration file {} not found. Using defaults", CONFIG_FILE);
      }
      conf = new Configuration(props);
    }catch(IOException e) {
      LOG.error("Cannot load configuration from" + CONFIG_FILE, e);
      conf = new Configuration();
    }finally {
      sneakyClose(in);
    }
    return conf;
  }
  
  public Configuration loadFromProperties(String configKeyPrefix) {
    Properties env = System.getProperties();
    LOG.debug("Loading system properties with prefix {}", configKeyPrefix);
    
    Map<String, Object> envMap = new HashMap<>();
    env.forEach((Object key, Object value) -> {
      String k = key.toString();
      if(k.startsWith(configKeyPrefix + ".")) {
        envMap.put(k, value);
      }
    });
    return new Configuration(envMap);
  }
  
  private Configuration loadFromEnv(String configKeyPrefix) {
    Map<String, String> env = System.getenv();
    Map<String, Object> envProps = new HashMap<>();
    LOG.debug("Loading environment properties with prefix {}", configKeyPrefix);
    env.entrySet().stream()
        .filter((Map.Entry<String, String> e) -> {
          String key = e.getKey();
          String[] parts = key.split("_");
          return parts.length > 1 && parts[0].equalsIgnoreCase(configKeyPrefix);
        }).forEach((Map.Entry<String, String> e) -> {
            String key = e.getKey().replace("_", ".").toLowerCase();
            envProps.put(key, e.getValue());
        });
    return new Configuration(envProps);
  }
  
  private void sneakyClose(Closeable c) {
    try {
      if(c != null) c.close();
    }catch(IOException ioe) {
      // ignore
    }
  }
  
  @SuppressWarnings("unchecked")
  public static Configuration from(Map config) {
    Map<String, Object> map = new HashMap<>();
    config.forEach((k, v) -> {
      map.put(k == null ? null : k.toString(), v);
    });
    return new Configuration(map);
  }
  
  public static Configuration from(Properties config) {
    return new Configuration(config);
  }
}
