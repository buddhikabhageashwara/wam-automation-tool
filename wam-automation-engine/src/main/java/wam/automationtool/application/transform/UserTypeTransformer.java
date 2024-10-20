package wam.automationtool.application.transform;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import wam.automationtool.application.dto.user.type.UserTypeAddRequestDto;
import wam.automationtool.application.dto.user.type.UserTypeDto;
import wam.automationtool.domain.entity.user.type.UserType;

@Service
public class UserTypeTransformer {

  /**
   * Transforms a list of UserType entities to a list of UserTypeDto objects.
   * This method maps each UserType entity to a UserTypeDto, keeping only
   * relevant fields for the DTO, like the ID and user type name.
   *
   * @param userTypeList A list of UserType entities to be transformed.
   * @return List<UserTypeDto> A list of DTOs representing the user types.
   */
  public List<UserTypeDto> userTypeListToUserTypeDtoList(final List<UserType> userTypeList) {
    List<UserTypeDto> userTypeDtoList =
            userTypeList.stream()
                    .map(
                            userType ->
                                    UserTypeDto.builder()
                                            .id(userType.getId())
                                            .userTypeName(userType.getUserTypeName())
                                            .build())
                    .collect(Collectors.toList());
    return userTypeDtoList;
  }

  /**
   * Converts a UserTypeAddRequestDto object to a UserType entity. This is
   * useful when adding a new user type, as the DTO is the incoming object
   * while the UserType entity is used for persistence.
   *
   * @param userTypeAddRequestDTO The DTO containing details about the new user type.
   * @return UserType The UserType entity created from the DTO.
   */
  public UserType userTypeAddRequestDtoToUserType(final UserTypeAddRequestDto userTypeAddRequestDTO) {
    return UserType.builder()
            .userTypeName(userTypeAddRequestDTO.getUserTypeName())
            .description(userTypeAddRequestDTO.getDescription())
            .isDeleted(false)
            .build();
  }
}
