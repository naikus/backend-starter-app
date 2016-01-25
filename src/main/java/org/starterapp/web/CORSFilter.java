/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.starterapp.web;

/**
 *
 * @author naikus
 */
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A response filter that allows us to enable CORS for the API
 */
public class CORSFilter implements Filter {  
  private static final Logger LOG = LoggerFactory.getLogger(CORSFilter.class);
  
  private String allowOrigin;
  private String allowCredentials;
  private String allowHeaders;
  private String allowMethods;

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    allowOrigin = filterConfig.getInitParameter("Allow-Origin");
    allowCredentials = filterConfig.getInitParameter("Allow-Credentials");
    allowHeaders = filterConfig.getInitParameter("Allow-Headers");
    allowMethods = filterConfig.getInitParameter("Allow-Methods");
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
      throws IOException, ServletException {
    HttpServletResponse httpRes = (HttpServletResponse) response;
    httpRes.addHeader("Access-Control-Allow-Origin", allowOrigin);
    httpRes.addHeader("Access-Control-Allow-Credentials", allowCredentials);
    httpRes.addHeader("Access-Control-Allow-Headers", allowHeaders);
    httpRes.addHeader("Access-Control-Allow-Methods", allowMethods);
    
    HttpServletRequest httpReq = (HttpServletRequest) request;
    if("OPTIONS".equalsIgnoreCase(httpReq.getMethod())) {
      LOG.info("Allowing OPTIONS method for {}", httpReq.getRequestURI());
    }else {
      chain.doFilter(request, response);
    }
  }

  @Override
  public void destroy() {}
  
}

