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

import wam.automationtool.domain.entity.user.type.UserType;
import wam.automationtool.domain.entity.usertype.permission.UserTypePermission;
import wam.automationtool.domain.repository.UserTypePermissionRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserTypePermissionDomainService {

  private final UserTypePermissionRepository repository;

  @Autowired
  public UserTypePermissionDomainService(final UserTypePermissionRepository repository) {
    this.repository = repository;
  }

  public void add(final UserTypePermission userTypePermission) {
    repository.save(userTypePermission);
  }

  public void update(final UserTypePermission userTypePermission) {
    repository.save(userTypePermission);
  }

  public void delete(final UserTypePermission userTypePermission) {
    repository.delete(userTypePermission);
  }

  public Optional<UserTypePermission> findById(final String id) {
    return repository.findById(id);
  }

  public Optional<UserTypePermission> findByPermissionIdAndUserTypeId(
      final String permissionListId, final String userTypeId) {
    return repository.findByPermissionIdAndUserTypeIdAndIsDeleted(
        permissionListId, userTypeId, false);
  }

  public List<UserTypePermission> findByUserType(final UserType userType) {
    return repository.findByUserTypeAndIsDeleted(userType, false);
  }

  public List<UserTypePermission> findAll() {
    return repository.findAll();
  }
}
