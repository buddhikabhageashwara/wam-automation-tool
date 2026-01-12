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

package wam.automationtool.domain.service;

import wam.automationtool.domain.entity.permission.Permission;
import wam.automationtool.domain.repository.PermissionRepository;
import java.util.List;
import java.util.Objects;import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PermissionDomainService {

  private final PermissionRepository permissionRepository;

  @Autowired
  public PermissionDomainService(final PermissionRepository permissionRepository) {
    this.permissionRepository = permissionRepository;
  }

  /**
   * Adds a new PermissionList to the repository.
   *
   * @param permission The PermissionList entity to add.
   */
  public void add(final Permission permission) {
    permissionRepository.save(permission);
  }

  /**
   * Updates an existing PermissionList in the repository.
   *
   * @param permission The PermissionList entity to update.
   */
  public void update(final Permission permission) {
    permissionRepository.save(permission);
  }

  /**
   * Deletes a PermissionList from the repository.
   *
   * @param permission The PermissionList entity to delete.
   */
  public void delete(final Permission permission) {
    permissionRepository.delete(permission);
  }

  /**
   * Finds a PermissionList by its ID.
   *
   * @param id The ID of the PermissionList to find.
   * @return An Optional containing the found PermissionList, or empty if not found.
   */
  public Optional<Permission> findById(final String id) {
    return permissionRepository.findByIdAndIsDeleted(id, false);
  }

  /**
   *
   * @param permissions
   * @return A List containing the found PermissionLists.
   */
  public List<Permission> findByPermission(final List<Permission> permissions) {
    final List<String> permissionIdList = permissions.stream()
            .map(Permission::getId)
            .filter(Objects::nonNull)
            .toList();
    return permissionRepository.findByIdInAndIsDeleted(permissionIdList, false);
  }

  /**
   * Finds all PermissionLists that are not marked as deleted.
   *
   * @return A list of all active PermissionLists.
   */
  public List<Permission> findAll() {
    return permissionRepository.findByIsDeleted(false);
  }

  public Optional<Permission> findByPermissionType(final String permissionType) {
    return permissionRepository.findByPermissionTypeAndIsDeleted(permissionType, false);
  }

}
