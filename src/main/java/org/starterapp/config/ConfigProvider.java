package org.starterapp.config;

import javax.inject.Provider;

/**
 * Provider for application configuration
 * @author aniket
 */
public abstract class ConfigProvider implements Provider<Configuration> {
  @Override
  public abstract Configuration get();
}
