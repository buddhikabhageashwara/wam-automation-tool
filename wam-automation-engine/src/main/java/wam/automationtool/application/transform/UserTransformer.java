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
