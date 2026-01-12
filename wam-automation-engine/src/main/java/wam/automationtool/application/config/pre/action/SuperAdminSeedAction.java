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

package wam.automationtool.application.config.pre.action;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import wam.automationtool.application.config.pre.action.seed.StartupPreAction;
import wam.automationtool.application.util.PasswordHashUtil;
import wam.automationtool.domain.entity.user.WAMUser;
import wam.automationtool.domain.entity.user.type.UserType;
import wam.automationtool.domain.service.UserTypeDomainService;
import wam.automationtool.domain.service.WAMUserDomainService;

import static wam.automationtool.application.config.AppConstant.SUPER_ADMIN;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class SuperAdminSeedAction implements StartupPreAction {

  private static final String SUPER_ADMIN_DESC =
      "This user type, SUPER_ADMIN, is capable of accessing all services within the automation tool. "
          + "It is encouraged to assign this role to only one user, "
          + "as there should be only one admin user managing the entire tool.";

  private static final String DEFAULT_FIRST_NAME = "wam";
  private static final String DEFAULT_LAST_NAME = "admin";
  private static final String DEFAULT_EMAIL = "wamadmin@default.com";
  private static final String DEFAULT_PASSWORD = "Welcome@911";

  private final UserTypeDomainService userTypeDomainService;
  private final WAMUserDomainService wamUserDomainService;

  @Override
  @Transactional
  public void execute() {
    try {
      final UserType superAdminType =
          userTypeDomainService
              .findByUserTypeName(SUPER_ADMIN)
              .orElseGet(this::createSuperAdminUserType);
      final long superAdminUserCount =
          wamUserDomainService.countActiveUsersForUserTypeId(superAdminType.getId());
      if (superAdminUserCount > 1) {
        throw new IllegalStateException(
            "Data invalid: more than one active SUPER_ADMIN user exists for userTypeId: "
                + superAdminType.getId());
      }
      if (superAdminUserCount == 1) {
        log.info("SUPER_ADMIN user already exists. userTypeId: {}", superAdminType.getId());
        return;
      }
      final String passwordHash = PasswordHashUtil.hash(DEFAULT_PASSWORD);
      final WAMUser superAdminUser =
          WAMUser.builder()
              .firstName(DEFAULT_FIRST_NAME)
              .lastName(DEFAULT_LAST_NAME)
              .userEmail(DEFAULT_EMAIL)
              .userPassword(passwordHash)
              .isDeleted(false)
              .userType(superAdminType)
              .build();
      wamUserDomainService.add(superAdminUser);
      log.info(
          "SUPER_ADMIN user created. userTypeId: {}, email: {}",
          superAdminType.getId(),
          DEFAULT_EMAIL);
    } catch (final Exception exception) {
      log.error("SUPER_ADMIN seeding failed. Application startup will fail.", exception);
      throw exception;
    }
  }

  private UserType createSuperAdminUserType() {
    final UserType userType =
        UserType.builder()
            .userTypeName(SUPER_ADMIN)
            .description(SUPER_ADMIN_DESC)
            .isDeleted(false)
            .build();
    userTypeDomainService.add(userType);
    log.info("SUPER_ADMIN user type created. id: {}", userType.getId());
    return userType;
  }
}
