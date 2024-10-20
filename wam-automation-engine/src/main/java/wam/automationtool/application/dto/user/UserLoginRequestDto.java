package wam.automationtool.application.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class UserLoginRequestDto {

  @NotBlank(message = "error.user.email.empty")
  private String userEmail;

  @NotBlank(message = "error.user.password.empty")
  private String userPassword;
}
