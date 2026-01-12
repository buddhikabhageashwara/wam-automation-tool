/*
 * MIT License
 *
 * Copyright (c) 2024 buddhika bhageashwara alwis
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
import static wam.automationtool.application.config.AppConstant.CREATED_MODIFIED_USER_ID;

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
      MDC.remove(CREATED_MODIFIED_USER_ID);
      MDC.remove(CORRELATION_ID_LOG_VAR_NAME);
    }
  }

  private boolean isIgnoredPath(final HttpServletRequest httpRequest) {
    return ignorePaths.stream().anyMatch(ignore -> httpRequest.getServletPath().contains(ignore));
  }

}
