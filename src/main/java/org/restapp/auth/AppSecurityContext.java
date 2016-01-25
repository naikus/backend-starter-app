package org.restapp.auth;

/**
 * A security context for service layer. This is used to obtain information about the current user
 */
public class AppSecurityContext {
  private static final ThreadLocal<AppSecurityContext> scThreadLocal = new ThreadLocal<AppSecurityContext>();

  private Object subject;

  public AppSecurityContext(Object subject) {
    this.subject = subject;
  }

  public Object getSubject() {
    return this.subject;
  }

  public static AppSecurityContext create(Object subject) {
    AppSecurityContext ctx = new AppSecurityContext(subject);
    scThreadLocal.set(ctx);
    return ctx;
  }

  public static AppSecurityContext get() {
    return scThreadLocal.get();
  }

  public static void remove() {
    scThreadLocal.remove();
  }
}
