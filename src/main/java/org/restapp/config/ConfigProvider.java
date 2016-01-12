package org.restapp.config;

import javax.inject.Provider;

/**
 * Provider for application configuration
 * @author aniket
 */
public abstract class ConfigProvider implements Provider<Configuration> {
  public abstract Configuration get();
}
