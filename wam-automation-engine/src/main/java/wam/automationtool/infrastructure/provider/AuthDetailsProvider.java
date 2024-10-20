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
