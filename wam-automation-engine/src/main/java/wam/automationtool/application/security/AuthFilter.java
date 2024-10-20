package wam.automationtool.application.security;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.slf4j.MDC;
import static wam.automationtool.application.config.AppConstant.CORRELATION_ID_LOG_VAR_NAME;
import static wam.automationtool.application.config.AppConstant.CustomHeaders.CORRELATION_ID_HEADER;

@Slf4j
public class AuthFilter implements Filter {
  private final Set<String> ignorePaths =
      new HashSet<>(
          Arrays.asList(
              "/v1/wam/automation/users/login",
              "swagger",
              "api-docs",
              "webjars",
              "actuator",
              "health",
              "/v1/wam/automation/health"));

  private final AuthTokenFilter authTokenFilter;
  private final HandlerExceptionResolver resolver;

  public AuthFilter(
      final AuthTokenFilter authTokenFilter, final HandlerExceptionResolver resolver) {
    this.authTokenFilter = authTokenFilter;
    this.resolver = resolver;
  }

  /**
   * Filter all the API request with auth
   *
   * @param request Any API request
   * @param response Any API response
   * @param chain filter chain from spring security
   */
  @Override
  public void doFilter(
          final ServletRequest request, final ServletResponse response, final FilterChain chain) {
    final HttpServletRequest httpRequest = (HttpServletRequest) request;
    final HttpServletResponse httpResponse = (HttpServletResponse) response;
    try {
      String correlationId = httpRequest.getHeader(CORRELATION_ID_HEADER);
      if (Objects.isNull(correlationId) || correlationId.isEmpty()) {
        correlationId = UUID.randomUUID().toString();
      }
      MDC.put(CORRELATION_ID_LOG_VAR_NAME, correlationId);
      log.info("Request received [{}] {}", httpRequest.getMethod(), httpRequest.getServletPath());
      boolean skipAuthCheck = "OPTIONS".equals(httpRequest.getMethod()) || isIgnoredPath(httpRequest);
      if (!skipAuthCheck) {
        authTokenFilter.doFilterInternal(httpRequest);
      }
      chain.doFilter(request, response);
    } catch (final Exception e) {
      log.error("Spring Security Filter Chain Exception: {}", e.getMessage());
      resolver.resolveException(httpRequest, httpResponse, null, e);
    } finally {
      MDC.remove(CORRELATION_ID_LOG_VAR_NAME);
    }
  }

  private boolean isIgnoredPath(final HttpServletRequest httpRequest) {
    return ignorePaths.stream().anyMatch(ignore -> httpRequest.getServletPath().contains(ignore));
  }

}
