package org.starterapp.services;

/**
 * A general exception used by services
 * 
 * @author aniket
 */
public class ServiceException extends RuntimeException {
  public ServiceException() {
    super();
  }
    
  public ServiceException(String message) {
    super(message);
  }

  public ServiceException(Throwable cause) {
    super(cause);
  }

  public ServiceException(String message, Throwable cause) {
    super(message, cause);
  }
}