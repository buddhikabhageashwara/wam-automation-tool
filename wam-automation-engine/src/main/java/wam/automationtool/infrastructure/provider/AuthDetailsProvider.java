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

package wam.automationtool.infrastructure.provider;

import static wam.automationtool.application.config.AppConstant.AuthConstants.FAILED_CODE;
import static wam.automationtool.application.config.AppConstant.ClaimName.WAM_AUTOMATION_USER_DETAILS;
import static wam.automationtool.application.config.AppConstant.INTERNAL_ERROR_MSG_KEY;

import wam.automationtool.application.dto.WAMAutomationUserDetailsDto;
import wam.automationtool.application.exception.CommonErrorsException;
import wam.automationtool.application.exception.CustomError;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

@Component
public class AuthDetailsProvider {

  /**
   * Getting UserDetailsDto
   *
   * @return UserDetailsDto
   */
  public final WAMAutomationUserDetailsDto getWAMAutomationUserDetails() {
    final RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
    if (Objects.nonNull(requestAttributes)) {
      final WAMAutomationUserDetailsDto userDetailsDto =
          (WAMAutomationUserDetailsDto)
              requestAttributes.getAttribute(WAM_AUTOMATION_USER_DETAILS, 0);
      return userDetailsDto;
    }
    throw new CommonErrorsException(FAILED_CODE, List.of(new CustomError(INTERNAL_ERROR_MSG_KEY)));
  }
}
