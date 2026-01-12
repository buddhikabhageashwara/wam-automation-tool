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
import wam.automationtool.application.dto.permission.PermissionDto;
import wam.automationtool.domain.entity.permission.Permission;
import wam.automationtool.domain.entity.permission.PermissionType;
import wam.automationtool.domain.entity.user.type.UserType;
import wam.automationtool.domain.entity.usertype.permission.UserTypePermission;

@Service
public class PermissionTransformer {

  /**
   * Transforms a list of Permission entities into a list of PermissionDto.
   *
   * @param permissionList the list of Permission entities to be transformed
   * @return a List of PermissionDto representing the transformed permissions
   */
  public List<PermissionDto> permissionListToPermissionDtoList(
          final List<Permission> permissionList) {
    List<PermissionDto> permissionDtoList =
            permissionList.stream()
                    .map(
                            permission ->
                                    PermissionDto.builder()
                                            .id(permission.getId())
                                            .permissionType(permission.getPermissionType())
                                            .build())
                    .collect(Collectors.toList());
    return permissionDtoList;
  }

  /**
   * Converts a PermissionType enum to a Permission entity.
   *
   * @param permissionType the PermissionType to be converted
   * @return a Permission entity corresponding to the provided PermissionType
   */
  public Permission permissionTypeToPermission(final PermissionType permissionType) {
    return Permission.builder()
            .permissionType(permissionType.getPermissionType())
            .description(permissionType.getDescription())
            .build();
  }

  /**
   * Updates an existing Permission entity with its current values.
   *
   * @param permission the Permission entity to be updated
   * @return the updated Permission entity
   */
  public Permission existingPermissionToPermission(final Permission permission,
                                                   final PermissionType permissionType) {
    permission.setPermissionType(permissionType.getPermissionType());
    permission.setDescription(permissionType.getDescription());
    return permission;
  }

  /**
   * Transforms a UserType and a Permission into a UserTypePermission entity.
   *
   * @param userType the UserType associated with the permission
   * @param permission the Permission to be assigned to the UserType
   * @return a UserTypePermission entity representing the association
   */
  public UserTypePermission toUserTypePermission(final UserType userType,
                                                 final Permission permission) {
    return UserTypePermission.builder()
            .permission(permission)
            .userType(userType)
            .build();
  }
}
