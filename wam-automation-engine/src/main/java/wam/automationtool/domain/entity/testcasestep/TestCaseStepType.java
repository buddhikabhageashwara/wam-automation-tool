package wam.automationtool.domain.entity.testcasestep;

import java.util.Arrays;
import lombok.Getter;

@Getter
public enum TestCaseStepType {
  HOME(
          "HOME",
          "Home",
          "home.html",
          "/v1/wam/automation/home/details/GET",
          true,
          "Retrieves the home page details"),
  ADD_PERMISSION(
          "ADD_PERMISSION",
          "Permission",
          "permission.html",
          "/v1/wam/automation/permissions/POST",
          true,
          "Allows adding new permissions"),
  RETRIEVE_PERMISSIONS(
          "RETRIEVE_PERMISSIONS",
          "",
          "",
          "/v1/wam/automation/permissions/GET",
          false,
          "Fetches a list of existing permissions"),
  ASSIGN_PERMISSION(
          "ASSIGN_PERMISSION",
          "Assign Permission",
          "assign-permission.html",
          "/v1/wam/automation/permissions/{path-param}/usertypes/{path-param}/POST",
          true,
          "Assigns a specific permission to a user type"),
  ADD_USER(
          "ADD_USER",
          "User",
          "user.html",
          "/v1/wam/automation/users/POST",
          true,
          "Allow adding a new user"),
  LOGIN_USER(
          "LOGIN_USER",
          "",
          "",
          "/v1/wam/automation/users/login/POST",
          false,
          "Authenticates a user login request"),
  RESET_PASSWORD(
          "RESET_PASSWORD",
          "My Profile",
          "my-profile.html",
          "/v1/wam/automation/users/reset-password/PUT",
          true,
          "Allows the user to reset their password"),
  ADD_USER_TYPE(
          "ADD_USER_TYPE",
          "User Type",
          "user-type.html",
          "/v1/wam/automation/usertypes/POST",
          true,
          "Enables adding a new user type"),
  RETRIEVE_USER_TYPES(
          "RETRIEVE_USER_TYPES",
          "",
          "",
          "/v1/wam/automation/usertypes/GET",
          false,
          "Fetches a list of user types");

  private final String id;
  private final String name;
  private final String page;
  private final String permissionType;
  private final boolean isDisplay;
  private final String description;

  TestCaseStepType(
          final String id,
          final String name,
          final String page,
          final String permissionType,
          final boolean isDisplay,
          final String description) {
    this.id = id;
    this.name = name;
    this.page = page;
    this.permissionType = permissionType;
    this.isDisplay = isDisplay;
    this.description = description;
  }

  /**
   * Retrieves a PermissionType based on its name.
   *
   * @param name the name of the permission type
   * @return the corresponding PermissionType or HOME if not found
   */
  public static TestCaseStepType getByValue(final String name) {
    return Arrays.stream(TestCaseStepType.values())
            .filter(enumRole -> enumRole.name.equals(name))
            .findFirst()
            .orElse(HOME);
  }

}
