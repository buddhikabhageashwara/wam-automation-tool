/*
 * MIT License
 *
 * Copyright (c) 2024 buddhika bhageashwara alwis
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
   * relevant fields for the DTO, like the ID and user type name, and excludes
   * those with the name "SUPER_ADMIN".
   *
   * @param userTypeList A list of UserType entities to be transformed.
   * @return List<UserTypeDto> A list of DTOs representing the user types.
   */
  public List<UserTypeDto> userTypeListToUserTypeDtoList(final List<UserType> userTypeList) {
    List<UserTypeDto> userTypeDtoList =
            userTypeList.stream()
                    .filter(userType -> !"SUPER_ADMIN".equals(userType.getUserTypeName()))
                    .map(userType ->
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
