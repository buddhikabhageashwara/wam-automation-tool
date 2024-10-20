package wam.automationtool.application.impl.user.type;

import wam.automationtool.application.dto.user.type.UserTypeAddRequestDto;
import wam.automationtool.application.dto.user.type.UserTypesResponseDto;

public interface UserTypeService {

  void addUserType(UserTypeAddRequestDto userTypeAddRequestDTO);
  UserTypesResponseDto getUserTypes();
}
