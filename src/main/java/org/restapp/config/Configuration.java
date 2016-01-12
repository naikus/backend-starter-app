package org.restapp.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application configuration
 */
public class Configuration /* extends Observable */ {
  private static final Logger LOG = LoggerFactory.getLogger(Configuration.class.getSimpleName());
  private final Map<String, ? super Object> props = new HashMap<>();
  
  Configuration(Map<String, Object> configProps) {
    this.props.putAll(configProps);
  }
  
  Configuration(Properties properties) {
    properties.entrySet().forEach(e -> this.props.put(e.getKey().toString(), e.getValue()));
  }
  
  Configuration() {}
  
  public boolean hasKey(String key) {
    return props.containsKey(key);
  }
  
  public Object set(String key, Object value) {
    return props.put(key, value);
  }
  
  public void merge(Configuration other) {
    this.props.putAll(other.props);
  }

  public Object get(String key) {
    return props.get(key);
  }

  public String getString(String key) {
    Object val = props.get(key);
    return val == null ? null : val.toString();
  }
  
  public String getString(String key, String defaultValue) {
    Object val = props.get(key);
    return val == null ? defaultValue : val.toString();
  }

  public int getInt(String key, int defaultValue) {
    Object objVal = props.get(key);
    if(objVal == null) {return defaultValue;}
    
    Integer val = getAs(objVal, Integer.class);
    if(val != null) {return val.intValue();}

    return Integer.parseInt(objVal.toString());
  }
  
  public int getInt(String key) {
    return getInt(key, 0);
  }
  
  public long getLong(String key, long defaultValue) {
    Object objVal = props.get(key);
    if(objVal == null) {return defaultValue;}
    
    Long val = getAs(objVal, Long.class);
    if(val != null) {return val.longValue();}
    
    return Long.parseLong(objVal.toString());
  }

  public long getLong(String key) {
    return getLong(key, 0);
  }

  public boolean getBoolean(String key) {
    Object objVal = props.get(key);
    if(objVal == null) return false;
    return Boolean.parseBoolean(objVal.toString());
  }

  public double getDouble(String key, double defaultValue) {
    Object objVal = props.get(key);
    if(objVal == null) {return defaultValue;}
    
    Double val = getAs(objVal, Double.class);
    if(val != null) {return val.doubleValue();}
    
    return Double.parseDouble(objVal.toString());
  }
  
  public double getDouble(String key) {
    return getDouble(key, 0.0);
  }
  
  @SuppressWarnings("unchecked")
  public <T> T get(String key, Class<T> type) {
    Object val = props.get(key);
    if(val == null) {
      return null;
    }
    if(type.isAssignableFrom(val.getClass())) {
      return (T) val;
    }
    return null;
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    props.entrySet().forEach((e) -> {
      sb.append(e.getKey()).append("=").append(e.getValue()).append("\n");
    });
    return sb.toString();
  }
  
  @SuppressWarnings("unchecked")
  private <T> T getAs(Object val, Class<T> type) {
    if(val == null) {
      return null;
    }
    if(type.isAssignableFrom(val.getClass())) {
      return (T) val;
    }
    return null;
  }
}
