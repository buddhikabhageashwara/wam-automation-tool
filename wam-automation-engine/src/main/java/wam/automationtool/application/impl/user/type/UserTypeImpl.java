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

package wam.automationtool.application.impl.user.type;

import static wam.automationtool.application.config.AppConstant.AuthConstants.USER_TYPE_ALREADY_EXIST_CODE;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import wam.automationtool.application.dto.user.type.UserTypeAddRequestDto;
import wam.automationtool.application.dto.user.type.UserTypeDto;
import wam.automationtool.application.dto.user.type.UserTypesResponseDto;
import wam.automationtool.application.exception.UserTypeExistException;
import wam.automationtool.application.transform.UserTypeTransformer;
import wam.automationtool.domain.entity.user.type.UserType;
import wam.automationtool.domain.service.UserTypeDomainService;
import wam.automationtool.infrastructure.provider.AuthDetailsProvider;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserTypeImpl extends AuthDetailsProvider implements UserTypeService {

  private final UserTypeDomainService userTypeDomainService;
  private final UserTypeTransformer userTypeTransformer;

  /**
   * Adds a new user type to the system. The method first converts the
   * user type name to uppercase and checks if the user type already
   * exists. If it does, a UserTypeExistException is thrown. Otherwise,
   * the new user type is added after transforming the request DTO to
   * the UserType entity.
   *
   * @param userTypeAddRequestDTO The DTO containing details for the new user type.
   * @throws UserTypeExistException If the user type already exists.
   */
  @Override
  public void addUserType(final UserTypeAddRequestDto userTypeAddRequestDTO) {
    final String upperCaseUserType =
            userTypeAddRequestDTO.getUserTypeName().toUpperCase(Locale.ROOT);
    final Optional<UserType> existingUserType =
            userTypeDomainService.findByUserTypeName(upperCaseUserType);
    if (existingUserType.isPresent()) {
      throw new UserTypeExistException(
              HttpStatus.BAD_REQUEST, USER_TYPE_ALREADY_EXIST_CODE, "error.user.type.already.exist");
    }
    final UserType userType =
            userTypeTransformer.userTypeAddRequestDtoToUserType(userTypeAddRequestDTO);
    userTypeDomainService.add(userType);
  }

  /**
   * Retrieves all user types available in the system. The method interacts
   * with the domain service to fetch the user types, transforms them to
   * DTOs, and returns them wrapped in a UserTypesResponseDto.
   *
   * @return A response DTO containing a list of all user types.
   */
  @Override
  public UserTypesResponseDto getUserTypes() {
    final List<UserType> userTypeList = userTypeDomainService.findAll();
    final List<UserTypeDto> userTypeDtoList =
            userTypeTransformer.userTypeListToUserTypeDtoList(userTypeList);
    return UserTypesResponseDto.builder().userTypeDtoList(userTypeDtoList).build();
  }
}
