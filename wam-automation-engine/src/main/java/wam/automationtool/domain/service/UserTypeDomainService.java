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
import wam.automationtool.domain.repository.UserTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserTypeDomainService {

  private final UserTypeRepository userTypeRepository;

  @Autowired
  public UserTypeDomainService(final UserTypeRepository userTypeRepository) {
    this.userTypeRepository = userTypeRepository;
  }

  /**
   * Add a new UserType
   *
   * @param userType the entity to add
   */
  public void add(final UserType userType) {
    userTypeRepository.save(userType);
  }

  /**
   * Update an existing UserType
   *
   * @param userType the entity to update
   */
  public void update(final UserType userType) {
    userTypeRepository.save(userType);
  }

  /**
   * Delete an existing UserType
   *
   * @param userType the entity to delete
   */
  public void delete(final UserType userType) {
    userTypeRepository.delete(userType);
  }

  /**
   * Find a UserType by its ID
   *
   * @param id the ID of the UserType
   * @return an Optional containing the found UserType or empty if not found
   */
  public Optional<UserType> findById(final String id) {
    return userTypeRepository.findByIdAndIsDeleted(id, false);
  }

  /**
   * Find a UserType by its userType
   *
   * @param userTypeName
   * @return an Optional containing the found UserType or empty if not found
   */
  public Optional<UserType> findByUserTypeName(final String userTypeName) {
    return userTypeRepository.findByUserTypeNameAndIsDeleted(userTypeName, false);
  }

  /**
   * Find all UserTypes
   *
   * @return a list of all UserTypes that are not deleted
   */
  public List<UserType> findAll() {
    return userTypeRepository.findByIsDeleted(false);
  }
}
