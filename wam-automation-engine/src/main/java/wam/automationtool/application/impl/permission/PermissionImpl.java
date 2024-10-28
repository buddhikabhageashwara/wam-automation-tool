package wam.automationtool.application.impl.permission;

import static wam.automationtool.application.config.AppConstant.AuthConstants.USER_TYPE_OR_PERMISSION_NOT_EXIST_CODE;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import wam.automationtool.application.dto.permission.PermissionDto;
import wam.automationtool.application.dto.permission.PermissionResponseDto;
import wam.automationtool.application.exception.UserTypeOrPermissionNotExistException;
import wam.automationtool.application.transform.PermissionTransformer;
import wam.automationtool.domain.entity.permission.Permission;
import wam.automationtool.domain.entity.permission.PermissionType;
import wam.automationtool.domain.entity.user.type.UserType;
import wam.automationtool.domain.entity.usertype.permission.UserTypePermission;
import wam.automationtool.domain.service.PermissionDomainService;
import wam.automationtool.domain.service.UserTypeDomainService;
import wam.automationtool.domain.service.UserTypePermissionDomainService;
import wam.automationtool.infrastructure.provider.AuthDetailsProvider;

@Service
@Slf4j
@RequiredArgsConstructor
public class PermissionImpl extends AuthDetailsProvider implements PermissionService {

  private final UserTypeDomainService userTypeDomainService;
  private final PermissionDomainService permissionDomainService;
  private final UserTypePermissionDomainService userTypePermissionDomainService;
  private final PermissionTransformer permissionTransformer;

  /**
   * Adds permissions based on the defined PermissionType. If a permission of a specific type
   * already exists, it updates the existing permission; otherwise, it creates a new permission.
   */
  @Override
  public void addPermission() {
    Arrays.stream(PermissionType.values())
        .forEach(
            permissionType -> {
              final Optional<Permission> optionalPermission =
                  permissionDomainService.findByPermissionType(permissionType.getPermissionType());
              if (optionalPermission.isPresent()) {
                final Permission existingPermission =
                    permissionTransformer.existingPermissionToPermission(
                        optionalPermission.get(), permissionType);
                permissionDomainService.update(existingPermission);
              } else {
                final Permission permission =
                    permissionTransformer.permissionTypeToPermission(permissionType);
                permissionDomainService.add(permission);
              }
            });
  }

  /**
   * Assigns a specific permission to a user type. Validates that both the permission and user type
   * exist before proceeding. If either does not exist, it throws a
   * UserTypeOrPermissionNotExistException.
   *
   * @param permissionId the ID of the permission to assign
   * @param userTypeId the ID of the user type to assign the permission to
   * @throws UserTypeOrPermissionNotExistException if the permission or user type does not exist
   */
  @Override
  public void assignPermission(final String permissionId, final String userTypeId) {
    final Optional<Permission> permissionOptional = permissionDomainService.findById(permissionId);
    final Optional<UserType> userTypeOptional = userTypeDomainService.findById(userTypeId);
    if (permissionOptional.isEmpty() || userTypeOptional.isEmpty()) {
      throw new UserTypeOrPermissionNotExistException(
          HttpStatus.BAD_REQUEST,
          USER_TYPE_OR_PERMISSION_NOT_EXIST_CODE,
          "error.user.type.or.permission.not.exist");
    }
    final Permission permission = permissionOptional.get();
    final UserType userType = userTypeOptional.get();
    final Optional<UserTypePermission> optionalUserTypePermission =
        userTypePermissionDomainService.findByPermissionIdAndUserTypeId(
            permission.getId(), userType.getId());
    final UserTypePermission userTypePermission =
        permissionTransformer.toUserTypePermission(userType, permission);
    if (optionalUserTypePermission.isEmpty()) {
      userTypePermissionDomainService.add(userTypePermission);
    }
  }

  /**
   * Retrieves all permissions and transforms them into a list of PermissionDto.
   *
   * @return a PermissionResponseDto containing a list of PermissionDto
   */
  @Override
  public PermissionResponseDto getPermission() {
    final List<Permission> permission = permissionDomainService.findAll();
    final List<PermissionDto> permissionDtoList =
        permissionTransformer.permissionListToPermissionDtoList(permission);
    return PermissionResponseDto.builder().permissionDtoList(permissionDtoList).build();
  }
}
