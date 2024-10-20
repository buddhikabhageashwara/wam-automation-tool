package wam.automationtool.application.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class UserResetPasswordRequestDto {

  @NotBlank(message = "error.current.password.empty")
  private String currentPassword;

  @NotBlank(message = "error.new.password.empty")
  private String newPassword;

  @NotBlank(message = "error.confirm.password.empty")
  private String confirmPassword;
}
