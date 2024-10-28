package wam.automationtool.application.dto.user.type;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class UserTypeAddRequestDto {

  @NotBlank(message = "error.user.type.empty")
  private String userTypeName;

  private String description;
}
