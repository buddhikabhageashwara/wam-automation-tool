package wam.automationtool.application.dto.user.type;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class UserTypeDto {

  private String id;
  private String userTypeName;
}
