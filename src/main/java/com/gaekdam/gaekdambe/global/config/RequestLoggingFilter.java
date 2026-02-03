package com.gaekdam.gaekdambe.global.config;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {
  private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    // entry log to confirm filter is invoked
    log.debug("RequestLoggingFilter ENTER: {} {}", request.getMethod(), request.getRequestURI());

    // log concrete request class for debugging filter ordering
    log.debug("RequestLoggingFilter: request class = {}", request.getClass().getName());

    // ContentCachingRequestWrapper requires cache size in this Spring version
    ContentCachingRequestWrapper wrapped = new ContentCachingRequestWrapper(request, 1024 * 1024);
    try {
      filterChain.doFilter(wrapped, response);
    } finally {
      byte[] buf = wrapped.getContentAsByteArray();
      if (buf != null && buf.length > 0) {
        String charset = wrapped.getCharacterEncoding() == null ? "UTF-8" : wrapped.getCharacterEncoding();
        String payload = new String(buf, charset);
        log.debug("REQUEST {} {} payload={}", request.getMethod(), request.getRequestURI(), payload);
      } else {
        log.debug("REQUEST {} {} (no payload)", request.getMethod(), request.getRequestURI());
      }
    }
  }
}