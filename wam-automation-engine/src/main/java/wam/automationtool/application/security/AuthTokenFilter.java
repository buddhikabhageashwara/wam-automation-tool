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

import static wam.automationtool.application.config.AppConstant.AuthConstants.AUTH_TOKEN_MISSING_CODE;
import static wam.automationtool.application.config.AppConstant.AuthConstants.AUTH_TOKEN_VALIDATION_CODE;
import static wam.automationtool.application.config.AppConstant.AuthConstants.SERVICE_PERMISSION_CODE;
import static wam.automationtool.application.config.AppConstant.ClaimName.WAM_AUTOMATION_USER_DETAILS;
import static wam.automationtool.application.config.AppConstant.CREATED_MODIFIED_USER_ID;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import wam.automationtool.application.config.AppConstant;
import wam.automationtool.application.dto.WAMAutomationUserDetailsDto;
import wam.automationtool.application.exception.AuthTokenMissingException;
import wam.automationtool.application.exception.AuthTokenValidationException;
import wam.automationtool.application.exception.ServicePermissionException;
import wam.automationtool.application.util.WAMAutomationJWTTokenUtil;

@Component
@Slf4j
@RequiredArgsConstructor
public class AuthTokenFilter {

  private final WAMAutomationJWTTokenUtil wamAutomationJWTTokenUtil;

  /**
   * Filters incoming requests to validate the JWT authentication token and check if the user
   * has the necessary permissions to access the resource.
   *
   * <p>This method checks for a valid authorization token in the request header. If the token is missing or invalid,
   * it throws an {@link AuthTokenMissingException} or {@link AuthTokenValidationException}. It also verifies
   * if the user has permission to access the requested resource based on the provided servlet path and HTTP method.
   *
   * @param request the HttpServletRequest object containing client request information
   * @throws AuthTokenMissingException if the authorization token is missing or does not start with the expected prefix
   * @throws AuthTokenValidationException if the JWT token is invalid or fails to be parsed
   * @throws ServicePermissionException if the user does not have the required permissions to access the resource
   */
  public void doFilterInternal(final HttpServletRequest request) {
    String headerAuthToken = request.getHeader(AppConstant.AUTH_HEADER);
    if (!StringUtils.hasText(headerAuthToken) || !headerAuthToken.startsWith(AppConstant.BEARER_VALUE)) {
      throw new AuthTokenMissingException(
              HttpStatus.UNAUTHORIZED, AUTH_TOKEN_MISSING_CODE, "error.auth.access.token.not.empty");
    }
    try {
      // Strip the "Bearer" prefix from the token and validate it
      headerAuthToken = headerAuthToken.substring(AppConstant.BEARER_LENGTH);
      final WAMAutomationUserDetailsDto wamAutomationUserDetailsDto =
              wamAutomationJWTTokenUtil.validateWAMAutomationToken(headerAuthToken);
      request.setAttribute(WAM_AUTOMATION_USER_DETAILS, wamAutomationUserDetailsDto);
      MDC.put(CREATED_MODIFIED_USER_ID, wamAutomationUserDetailsDto.getUserId());

      // Combine servlet path and HTTP method (e.g., "/v1/wam/automation/home/details/GET")
      final String requestPathWithMethod = request.getServletPath() + "/" + request.getMethod();

      // Check if the user has permission for the given request path and method
      final boolean hasPermission = wamAutomationUserDetailsDto.getPermissionTypeList().stream()
              .anyMatch(permissionType -> matchesPermission(requestPathWithMethod, permissionType));

      // If the user is not a super admin and lacks permission, throw an exception
      if (!wamAutomationUserDetailsDto.isSuperAdmin() && !hasPermission) {
        throw new ServicePermissionException(
                HttpStatus.FORBIDDEN, SERVICE_PERMISSION_CODE, "error.service.access.forbidden");
      }
    } catch (final Exception e) {
      log.error("Token validation issue", e);
      throw new AuthTokenValidationException(
              HttpStatus.UNAUTHORIZED, AUTH_TOKEN_VALIDATION_CODE, "error.auth.access.token.validation.failed");
    }
  }

  /**
   * Matches the request path against the permission paths, allowing for dynamic path parameters in the URL.
   *
   * <p>This method converts permission paths that contain placeholders (e.g., {path-param}) into regular expressions
   * that can match actual values in the request path. It then checks
   * if the request path matches the permission path pattern.
   *
   * @param requestPath the actual request path from the HttpServletRequest
   *                    (e.g., "/v1/wam/automation/home/details/GET")
   * @param permissionPath the permission path pattern from the permission list
   *                       (e.g., "/v1/wam/automation/home/details/GET")
   * @return true if the request path matches the permission pattern, false otherwise
   */
  private boolean matchesPermission(final String requestPath, final String permissionPath) {
    // Convert path params like {path-param} to a regex that matches any non-slash characters
    final String permissionPattern = permissionPath.replaceAll("\\{[^/]+\\}", "[^/]+");
    return requestPath.matches(permissionPattern);
  }
}
