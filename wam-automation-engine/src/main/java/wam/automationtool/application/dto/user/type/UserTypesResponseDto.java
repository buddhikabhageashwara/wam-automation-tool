package wam.automationtool.application.dto.user.type;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class UserTypesResponseDto {

  private List<UserTypeDto> userTypeDtoList;
}
