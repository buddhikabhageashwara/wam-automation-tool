package wam.automationtool.application.impl.user;

import static wam.automationtool.application.config.AppConstant.AuthConstants.PASSWORD_MISMATCHED_CODE;
import static wam.automationtool.application.config.AppConstant.AuthConstants.WAM_AUTOMATION_USER_ERROR;
import static wam.automationtool.application.config.AppConstant.SUPER_ADMIN;

import jakarta.transaction.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import wam.automationtool.application.dto.JWTTokenDto;
import wam.automationtool.application.dto.WAMAutomationTokenDto;
import wam.automationtool.application.dto.WAMAutomationUserDetailsDto;
import wam.automationtool.application.dto.user.UserCreateRequestDto;
import wam.automationtool.application.dto.user.UserLoginRequestDto;
import wam.automationtool.application.dto.user.UserLoginResponseDto;
import wam.automationtool.application.dto.user.UserResetPasswordRequestDto;
import wam.automationtool.application.exception.InvalidCredentialsException;
import wam.automationtool.application.exception.PasswordMismatchedException;
import wam.automationtool.application.exception.UserException;
import wam.automationtool.application.transform.UserTransformer;
import wam.automationtool.application.util.PasswordHashUtil;
import wam.automationtool.application.util.WAMAutomationJWTTokenUtil;
import wam.automationtool.domain.entity.permission.Permission;
import wam.automationtool.domain.entity.user.WAMUser;
import wam.automationtool.domain.entity.user.type.UserType;
import wam.automationtool.domain.entity.usertype.permission.UserTypePermission;
import wam.automationtool.domain.service.UserTypeDomainService;
import wam.automationtool.domain.service.UserTypePermissionDomainService;
import wam.automationtool.domain.service.WAMUserDomainService;
import wam.automationtool.infrastructure.provider.AuthDetailsProvider;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserImpl extends AuthDetailsProvider implements UserService {

  private final WAMUserDomainService wamUserDomainService;
  private final UserTypeDomainService userTypeDomainService;
  private final UserTypePermissionDomainService userTypePermissionDomainService;
  private final WAMAutomationJWTTokenUtil wamAutomationJWTTokenUtil;
  private final UserTransformer userTransformer;

  @Value("${aes.encrypt.decrypt.secret.key}")
  private String encryptDecryptSecretKey;

  @Value("${aes.salt.value}")
  private String saltValue;

  // insert into flow_chart_game.game_user
  // (first_name,last_name,user_password,user_email,is_deleted) VALUES
  // ("game","api","gameapiadmin@gmail.com","/rw3WVpWyAEmRnSkQxFyNw==", false)

  /**
   * Logs in a user by validating credentials, checking permissions, and generating a JWT token.
   *
   * @param userLoginRequestDTO the DTO containing the user's login request details (email and
   *     password).
   * @return UserLoginResponseDto containing the generated JWT token for the user.
   * @throws InvalidCredentialsException if the email or password is invalid.
   */
  @Override
  public UserLoginResponseDto loginUser(final UserLoginRequestDto userLoginRequestDTO) {
    final WAMUser wamUser = getWAMUser(userLoginRequestDTO.getUserEmail());
    final boolean isPasswordMatched =
        PasswordHashUtil.matches(userLoginRequestDTO.getUserPassword(), wamUser.getUserPassword());
    if (!isPasswordMatched) {
      throw new InvalidCredentialsException(
          HttpStatus.UNAUTHORIZED, WAM_AUTOMATION_USER_ERROR, "error.invalid.credential");
    }
    final boolean isSuperAdmin = isSuperAdmin(wamUser.getId());
    final List<String> permissionTypeList = getPermissionListForUser(wamUser.getUserType());
    final WAMAutomationTokenDto wamAutomationTokenDto =
        userTransformer.wamUserToWAMAutomationTokenDto(wamUser, isSuperAdmin, permissionTypeList);
    final JWTTokenDto jwtTokenDto = wamAutomationJWTTokenUtil.generateToken(wamAutomationTokenDto);
    return UserLoginResponseDto.builder().token(jwtTokenDto.getToken()).build();
  }

  /**
   * Retrieves a user from the database based on their email and password.
   *
   * @param userEmail the user's email address.
   * @return the WAMUser object if found.
   * @throws InvalidCredentialsException if no user with the given credentials is found.
   */
  private WAMUser getWAMUser(final String userEmail) {
    final WAMUser wamUser =
        wamUserDomainService
            .findByUserEmail(userEmail)
            .orElseThrow(
                () ->
                    new InvalidCredentialsException(
                        HttpStatus.UNAUTHORIZED,
                        WAM_AUTOMATION_USER_ERROR,
                        "error.invalid.credential"));
    return wamUser;
  }

  /**
   * Checks whether the given user is an active SUPER_ADMIN user.
   *
   * @param wamUserId the id of the user being checked
   * @return true if the user is SUPER_ADMIN (and not deleted); otherwise false
   */
  private boolean isSuperAdmin(final String wamUserId) {
    boolean isSuperAdmin = false;
    final Optional<UserType> userTypeOptional =
        userTypeDomainService.findByUserTypeName(SUPER_ADMIN);
    if (userTypeOptional.isPresent()) {
      final String superAdminUserTypeId = userTypeOptional.get().getId();
      isSuperAdmin =
          wamUserDomainService.existsActiveUserByIdAndUserTypeId(wamUserId, superAdminUserTypeId);
    }
    return isSuperAdmin;
  }

  /**
   * Retrieves the list of permissions assigned to the given user type.
   *
   * @param userType the type of the user (e.g., admin, user).
   * @return a list of permission types (strings) associated with the user type.
   */
  private List<String> getPermissionListForUser(final UserType userType) {
    final List<UserTypePermission> userTypePermissionList =
        Optional.ofNullable(userTypePermissionDomainService.findByUserType(userType))
            .orElse(Collections.emptyList());
    final List<Permission> permissionListForUserType =
        userTypePermissionList.stream()
            .map(UserTypePermission::getPermission)
            .filter(Objects::nonNull)
            .toList();
    final List<String> permissionTypeList =
        permissionListForUserType.stream()
            .map(Permission::getPermissionType)
            .filter(Objects::nonNull)
            .toList();
    return permissionTypeList;
  }

  /**
   * Resets the user's password by verifying the current password and updating it to the new
   * password.
   *
   * @param userResetPasswordRequestDTO the DTO containing the current and new passwords.
   * @throws PasswordMismatchedException if the new and confirm passwords do not match.
   * @throws InvalidCredentialsException if the current password is incorrect.
   */
  @Override
  public void resetPassword(final UserResetPasswordRequestDto userResetPasswordRequestDTO) {
    final boolean isNewAndConfirmPasswordsNotMatched =
        !userResetPasswordRequestDTO
            .getNewPassword()
            .equals(userResetPasswordRequestDTO.getConfirmPassword());
    if (isNewAndConfirmPasswordsNotMatched) {
      throw new PasswordMismatchedException(
          HttpStatus.BAD_REQUEST, PASSWORD_MISMATCHED_CODE, "error.password.mismatched");
    }
    final WAMAutomationUserDetailsDto wamAutomationUserDetailsDto = getWAMAutomationUserDetails();
    final String encryptedNewPassword =
        PasswordHashUtil.hash(userResetPasswordRequestDTO.getConfirmPassword());
    final WAMUser wamUser = getWAMUser(wamAutomationUserDetailsDto.getUserEmail());
    final boolean isPasswordMatched =
        PasswordHashUtil.matches(
            userResetPasswordRequestDTO.getNewPassword(), wamUser.getUserPassword());
    if (!isPasswordMatched) {
      throw new InvalidCredentialsException(
          HttpStatus.UNAUTHORIZED, WAM_AUTOMATION_USER_ERROR, "error.invalid.credential");
    }
    updateUser(encryptedNewPassword, wamUser);
  }

  /**
   * Updates the user's password in the database.
   *
   * @param encryptedNewPassword the user's encrypted new password.
   * @throws InvalidCredentialsException if the current password is incorrect.
   */
  private void updateUser(final String encryptedNewPassword, final WAMUser wamUser) {
    wamUser.setUserPassword(encryptedNewPassword);
    wamUserDomainService.update(wamUser);
  }

  /**
   * Creates a new user account by validating the input and saving the user in the database.
   *
   * @param userCreateRequestDTO the DTO containing the user's details (email, password, etc.).
   * @throws PasswordMismatchedException if the password and confirm password do not match.
   * @throws UserException if the email already exists or if the user type is invalid.
   */
  @Override
  @Transactional
  public void createUser(final UserCreateRequestDto userCreateRequestDTO) {
    final UserType userType = validateUserCreationAndRetrieveUserType(userCreateRequestDTO);
    final String passwordHash = PasswordHashUtil.hash(userCreateRequestDTO.getUserPassword());
    final WAMUser wamUser =
        userTransformer.userCreateRequestDtoToWAMUser(passwordHash, userCreateRequestDTO, userType);
    wamUserDomainService.add(wamUser);
  }

  /**
   * Validates user creation input and retrieves the associated user type.
   *
   * @param userCreateRequestDTO the DTO containing the user's creation details.
   * @return the UserType associated with the provided user type ID.
   * @throws PasswordMismatchedException if the password and confirm password do not match.
   * @throws UserException if the user already exists or if the user type is invalid.
   */
  private UserType validateUserCreationAndRetrieveUserType(
      final UserCreateRequestDto userCreateRequestDTO) {
    final boolean isPasswordAndConfirmPasswordNotMatched =
        !userCreateRequestDTO.getUserPassword().equals(userCreateRequestDTO.getConfirmPassword());
    if (isPasswordAndConfirmPasswordNotMatched) {
      throw new PasswordMismatchedException(
          HttpStatus.BAD_REQUEST, PASSWORD_MISMATCHED_CODE, "error.password.mismatched");
    }
    final Optional<WAMUser> wamUserOptional =
        wamUserDomainService.findByUserEmail(userCreateRequestDTO.getUserEmail());
    if (wamUserOptional.isPresent()) {
      throw new UserException(
          HttpStatus.BAD_REQUEST, WAM_AUTOMATION_USER_ERROR, "error.user.already.exist");
    }
    final Optional<UserType> userType =
        userTypeDomainService.findById(userCreateRequestDTO.getUserTypeId());
    if (userType.isEmpty()) {
      throw new UserException(
          HttpStatus.BAD_REQUEST, WAM_AUTOMATION_USER_ERROR, "error.invalid.user.type");
    }
    return userType.get();
  }
}
