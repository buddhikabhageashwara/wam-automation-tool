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

import static org.springframework.http.HttpHeaders.ORIGIN;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CorsFilter implements Filter {

  @Value("#{'${allowed.origins}'.split(',')}")
  private List<String> allowedOrigins;

  /**
   * Add response cors
   *
   * @param request API request
   * @param response API response
   * @param chain to continue filtering2
   * @throws IOException Using IO exception might be occurred
   * @throws ServletException Using Servlet exception might be occurred
   */
  @Override
  public void doFilter(
      final ServletRequest request, final ServletResponse response, final FilterChain chain)
      throws IOException, ServletException {
    HttpServletResponse httpServletResponse = (HttpServletResponse) response;
    HttpServletRequest httpServletRequest = (HttpServletRequest) request;
    final String origin = httpServletRequest.getHeader(ORIGIN);
    final List<String> configuredMatchedAllowOriginList =
            allowedOrigins.stream().filter(s -> origin.contains(s)).collect(Collectors.toList());
    if (!configuredMatchedAllowOriginList.isEmpty()) {
      httpServletResponse.setHeader("Access-Control-Allow-Origin", origin);
      request.setAttribute(ORIGIN, origin);
    }
    httpServletResponse.setHeader("Access-Control-Allow-Credentials", "true");
    httpServletResponse.setHeader(
        "Access-Control-Allow-Methods", "GET,HEAD,OPTIONS,POST,PUT,DELETE");
    httpServletResponse.setHeader(
        "Access-Control-Allow-Headers",
        "Access-Control-Allow-Headers,"
            + " Origin,Accept,"
            + " X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers,"
            + " custom-id, Authorization");
    chain.doFilter(request, httpServletResponse);
  }
}
