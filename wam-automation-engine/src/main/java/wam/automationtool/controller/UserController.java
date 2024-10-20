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

@RestController
@RequestMapping("/v1/wam/automation/users")
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
