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

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wam.automationtool.domain.entity.user.WAMUser;
import wam.automationtool.domain.repository.WAMUserRepository;

@Service
public class WAMUserDomainService {

  private final WAMUserRepository wamUserRepository;

  @Autowired
  public WAMUserDomainService(final WAMUserRepository wamUserRepository) {
    this.wamUserRepository = wamUserRepository;
  }

  /**
   * Save a new WAMUser entity to the database.
   *
   * @param WAMUser The WAMUser entity to add.
   */
  public void add(final WAMUser WAMUser) {
    wamUserRepository.save(WAMUser);
  }

  /**
   * Finds a WAMUser by userEmail.
   *
   * @param userEmail The email of the user.
   * @return An Optional containing the found WAMUser, or empty if not found.
   */
  public Optional<WAMUser> findByUserEmail(final String userEmail) {
    return wamUserRepository.findByUserEmailAndIsDeleted(userEmail, false);
  }

  /**
   * Updates an existing WAMUser entity in the database.
   *
   * @param WAMUser The WAMUser entity to update.
   */
  public void update(final WAMUser WAMUser) {
    wamUserRepository.save(WAMUser);
  }

  public long countActiveUsersForUserTypeId(final String userTypeId) {
    return wamUserRepository.countByUserType_IdAndIsDeleted(userTypeId, false);
  }

  public boolean existsActiveUserByIdAndUserTypeId(
      final String wamUserId, final String userTypeId) {
    return wamUserRepository.existsByIdAndUserType_IdAndIsDeleted(wamUserId, userTypeId, false);
  }
}
