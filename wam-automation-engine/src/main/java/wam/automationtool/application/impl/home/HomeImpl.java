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

package wam.automationtool.application.impl.home;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import wam.automationtool.application.dto.WAMAutomationUserDetailsDto;
import wam.automationtool.application.dto.home.HomeDetailsResponseDto;
import wam.automationtool.domain.entity.permission.PermissionType;
import wam.automationtool.infrastructure.provider.AuthDetailsProvider;

@Service
@Slf4j
@RequiredArgsConstructor
public class HomeImpl extends AuthDetailsProvider implements HomeService {

  /**
   * Retrieves home details based on the user's permissions.
   *
   * <p>This method checks the user's permissions and whether they are a super admin, then generates
   * a list of home details that the user is allowed to view.
   *
   * @return HomeDetailsResponseDto containing a list of home details.
   */
  @Override
  public HomeDetailsResponseDto getHomeDetails() {
    final WAMAutomationUserDetailsDto userDetails = getWAMAutomationUserDetails();
    final List<String> userPermissions = userDetails.getPermissionTypeList();
    final List<Map<String, String>> homeDetails =
        Arrays.stream(PermissionType.values())
            .filter(permission -> isDisplayable(permission, userDetails, userPermissions))
            .map(permission -> createHomeDetailMap(permission))
            .collect(Collectors.toList());
    return HomeDetailsResponseDto.builder().homeDetails(homeDetails).build();
  }

  /**
   * Determines if a given permission should be displayed to the user.
   *
   * <p>This method checks if the user is a super admin or if they have the specific permission
   * required to display the given permission type.
   *
   * @param permission the permission type to check
   * @param userDetails the details of the user requesting home details
   * @param userPermissions the list of permissions the user has
   * @return true if the permission is displayable; false otherwise
   */
  private boolean isDisplayable(
      final PermissionType permission,
      final WAMAutomationUserDetailsDto userDetails,
      final List<String> userPermissions) {
    return (userDetails.isSuperAdmin() && permission.isDisplay())
        || (userPermissions.contains(permission.getPermissionType()) && permission.isDisplay());
  }

  /**
   * Creates a map representation of a permission for home details.
   *
   * <p>This method constructs a map with the name and page of the given permission.
   *
   * @param permission the permission type to convert to a map
   * @return a map containing the permission name and page
   */
  private Map<String, String> createHomeDetailMap(final PermissionType permission) {
    return Map.of(
        "name", permission.getName(),
        "page", permission.getPage());
  }
}
