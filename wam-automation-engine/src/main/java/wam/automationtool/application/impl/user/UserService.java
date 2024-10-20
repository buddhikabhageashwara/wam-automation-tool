package wam.automationtool.application.impl.user;

import wam.automationtool.application.dto.user.UserCreateRequestDto;
import wam.automationtool.application.dto.user.UserLoginRequestDto;
import wam.automationtool.application.dto.user.UserResetPasswordRequestDto;
import wam.automationtool.application.dto.user.UserLoginResponseDto;

public interface UserService {

  UserLoginResponseDto loginUser(UserLoginRequestDto userLoginRequestDTO);

  void resetPassword(UserResetPasswordRequestDto userResetPasswordRequestDTO);

  void createUser(UserCreateRequestDto userCreateRequestDTO);
}
