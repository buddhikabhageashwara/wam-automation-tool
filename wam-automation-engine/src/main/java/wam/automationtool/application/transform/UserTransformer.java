package wam.automationtool.application.transform;

import java.util.List;
import org.springframework.stereotype.Service;
import wam.automationtool.application.dto.WAMAutomationTokenDto;
import wam.automationtool.application.dto.user.UserCreateRequestDto;import wam.automationtool.domain.entity.user.type.UserType;import wam.automationtool.domain.entity.user.WAMUser;

@Service
public class UserTransformer {

  /**
   * transform userTypeListToUserTypeDtoList
   *
   * @return List<UserTypeDto>
   */
  public WAMAutomationTokenDto wamUserToWAMAutomationTokenDto(
      final WAMUser wamUser, final boolean isSuperAdmin, final List<String> permissionTypeList) {
    return WAMAutomationTokenDto.builder()
        .userId(String.valueOf(wamUser.getId()))
        .firstName(wamUser.getFirstName())
        .lastName(wamUser.getLastName())
        .userEmail(wamUser.getUserEmail())
        .permissionTypeList(permissionTypeList)
        .isSuperAdmin(isSuperAdmin)
        .build();
  }

  public WAMUser userCreateRequestDtoToWAMUser(
          final String encryptedUserPassword,
          final UserCreateRequestDto userCreateRequestDto,
          final UserType userType) {
    return WAMUser.builder()
            .userPassword(encryptedUserPassword)
            .userEmail(userCreateRequestDto.getUserEmail())
            .firstName(userCreateRequestDto.getFirstName())
            .lastName(userCreateRequestDto.getLastName())
            .userType(userType)
            .isDeleted(false)
            .build();
  }
}
