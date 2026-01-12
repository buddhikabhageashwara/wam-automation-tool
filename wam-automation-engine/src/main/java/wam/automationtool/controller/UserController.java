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

package wam.automationtool.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import wam.automationtool.application.dto.user.UserCreateRequestDto;
import wam.automationtool.application.dto.user.UserLoginRequestDto;
import wam.automationtool.application.dto.user.UserLoginResponseDto;
import wam.automationtool.application.dto.user.UserResetPasswordRequestDto;
import wam.automationtool.application.impl.user.UserService;
import static wam.automationtool.application.config.AppConstant.WAM_AUTOMATION_BASE_PATH;

@RestController
@RequestMapping(WAM_AUTOMATION_BASE_PATH + "users")
@RequiredArgsConstructor
public final class UserController {

  private final UserService userService;

  @PostMapping
  public ResponseEntity<Void> createUser(
          @RequestBody @Valid final UserCreateRequestDto userCreateRequestDTO) {
    userService.createUser(userCreateRequestDTO);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @PostMapping("/login")
  public ResponseEntity<UserLoginResponseDto> loginUser(
      @RequestBody @Valid final UserLoginRequestDto userLoginRequestDTO) {
    return ResponseEntity.ok(userService.loginUser(userLoginRequestDTO));
  }

  @PutMapping("/reset-password")
  public ResponseEntity<Void> resetPassword(
      @RequestBody @Valid final UserResetPasswordRequestDto userResetPasswordRequestDTO) {
    userService.resetPassword(userResetPasswordRequestDTO);
    return ResponseEntity.ok().build();
  }
}
