package wam.automationtool.application.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class UserCreateRequestDto {

  @NotBlank(message = "error.user.email.empty")
  private String userEmail;

  @NotBlank(message = "error.user.password.empty")
  private String userPassword;

  @NotBlank(message = "error.confirm.password.empty")
  private String confirmPassword;

  @NotBlank(message = "error.first.name.empty")
  private String firstName;

  @NotBlank(message = "error.last.name.empty")
  private String lastName;

  @NotBlank(message = "error.user.type.id.empty")
  private String userTypeId;
}
