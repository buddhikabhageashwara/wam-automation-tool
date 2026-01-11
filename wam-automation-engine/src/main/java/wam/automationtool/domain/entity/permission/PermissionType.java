package wam.automationtool.domain.entity.permission;

import java.util.Arrays;
import lombok.Getter;

@Getter
public enum PermissionType {
  HOME(
      "HOME",
      "Home",
      "home.html",
      "/v1/wam/automation/homedetails/GET",
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
      "Fetches a list of user types"),
  ADD_ALIAS(
      "ADD_ALIAS",
      "Alias",
      "alias.html",
      "/v1/wam/automation/alias/POST",
      false,
      "Adds a new alias"),
  RETRIEVE_ALIASES(
      "RETRIEVE_ALIASES", "", "", "/v1/wam/automation/alias/GET", false, "Fetches alias list"),
  RETRIEVE_ALIAS(
      "RETRIEVE_ALIAS",
      "",
      "",
      "/v1/wam/automation/alias/{path-param}/GET",
      false,
      "Fetches an alias by id"),
  DELETE_ALIAS(
      "DELETE_ALIAS",
      "",
      "",
      "/v1/wam/automation/alias/{path-param}/DELETE",
      false,
      "Deletes an alias by id"),
  ADD_ALIAS_PARAMETER_TYPE(
      "ADD_ALIAS_PARAMETER_TYPE",
      "Alias Parameter Type",
      "alias-parameter-type.html",
      "/v1/wam/automation/aliasparametertypes/POST",
      false,
      "Adds a new alias parameter type"),
  RETRIEVE_ALIAS_PARAMETER_TYPES(
      "RETRIEVE_ALIAS_PARAMETER_TYPES",
      "",
      "",
      "/v1/wam/automation/aliasparametertypes/GET",
      false,
      "Fetches alias parameter types list"),
  RETRIEVE_ALIAS_PARAMETER_TYPE(
      "RETRIEVE_ALIAS_PARAMETER_TYPE",
      "",
      "",
      "/v1/wam/automation/aliasparametertypes/{path-param}/GET",
      false,
      "Fetches an alias parameter type by id"),
  UPDATE_ALIAS_PARAMETER_TYPE(
      "UPDATE_ALIAS_PARAMETER_TYPE",
      "",
      "",
      "/v1/wam/automation/aliasparametertypes/{path-param}/PUT",
      false,
      "Updates an alias parameter type by id"),
  DELETE_ALIAS_PARAMETER_TYPE(
      "DELETE_ALIAS_PARAMETER_TYPE",
      "",
      "",
      "/v1/wam/automation/aliasparametertypes/{path-param}/DELETE",
      false,
      "Deletes an alias parameter type by id"),
  ADD_PREFERENCE_PARAMETER_TYPE(
      "ADD_PREFERENCE_PARAMETER_TYPE",
      "Preference Parameter Type",
      "preference-parameter-type.html",
      "/v1/wam/automation/preferenceparametertypes/POST",
      false,
      "Adds a new preference parameter type"),
  RETRIEVE_PREFERENCE_PARAMETER_TYPES(
      "RETRIEVE_PREFERENCE_PARAMETER_TYPES",
      "",
      "",
      "/v1/wam/automation/preferenceparametertypes/GET",
      false,
      "Fetches preference parameter types list"),
  RETRIEVE_PREFERENCE_PARAMETER_TYPE(
      "RETRIEVE_PREFERENCE_PARAMETER_TYPE",
      "",
      "",
      "/v1/wam/automation/preferenceparametertypes/{path-param}/GET",
      false,
      "Fetches a preference parameter type by id"),
  UPDATE_PREFERENCE_PARAMETER_TYPE(
      "UPDATE_PREFERENCE_PARAMETER_TYPE",
      "",
      "",
      "/v1/wam/automation/preferenceparametertypes/{path-param}/PUT",
      false,
      "Updates a preference parameter type by id"),
  DELETE_PREFERENCE_PARAMETER_TYPE(
      "DELETE_PREFERENCE_PARAMETER_TYPE",
      "",
      "",
      "/v1/wam/automation/preferenceparametertypes/{path-param}/DELETE",
      false,
      "Deletes a preference parameter type by id"),
  ADD_TEST_CASE(
      "ADD_TEST_CASE",
      "Test Case",
      "test-case.html",
      "/v1/wam/automation/testcases/POST",
      false,
      "Adds a new test case"),
  RETRIEVE_TEST_CASES_BY_TEST_PLAN(
      "RETRIEVE_TEST_CASES_BY_TEST_PLAN",
      "",
      "",
      "/v1/wam/automation/testcases/testplans/{path-param}/GET",
      false,
      "Fetches test cases by test plan id"),
  RETRIEVE_TEST_CASE(
      "RETRIEVE_TEST_CASE",
      "",
      "",
      "/v1/wam/automation/testcases/{path-param}/GET",
      false,
      "Fetches a test case by id"),
  UPDATE_TEST_CASE(
      "UPDATE_TEST_CASE",
      "",
      "",
      "/v1/wam/automation/testcases/{path-param}/PUT",
      false,
      "Updates a test case by id"),
  DELETE_TEST_CASE(
      "DELETE_TEST_CASE",
      "",
      "",
      "/v1/wam/automation/testcases/{path-param}/DELETE",
      false,
      "Deletes a test case by id"),
  ADD_TEST_CASE_STEP(
      "ADD_TEST_CASE_STEP",
      "Test Case Step",
      "test-case-step.html",
      "/v1/wam/automation/testcasesteps/POST",
      false,
      "Adds a new test case step"),
  UPDATE_TEST_CASE_STEP(
      "UPDATE_TEST_CASE_STEP",
      "Update Test Case Step",
      "update-test-case-step.html",
      "/v1/wam/automation/testcasesteps/PATCH",
      false,
      "Update test case step"),
  RETRIEVE_TEST_CASE_STEPS_BY_TEST_CASE(
      "RETRIEVE_TEST_CASE_STEPS_BY_TEST_CASE",
      "",
      "",
      "/v1/wam/automation/testcasesteps/testcases/{path-param}/GET",
      false,
      "Fetches test case steps by test case id"),
  RETRIEVE_TEST_CASE_STEP(
      "RETRIEVE_TEST_CASE_STEP",
      "",
      "",
      "/v1/wam/automation/testcasesteps/{path-param}/GET",
      false,
      "Fetches a test case step by id"),
  DELETE_TEST_CASE_STEP(
      "DELETE_TEST_CASE_STEP",
      "",
      "",
      "/v1/wam/automation/testcasesteps/{path-param}/DELETE",
      false,
      "Deletes a test case step by id"),
  ADD_TEST_PLAN(
      "ADD_TEST_PLAN",
      "Test Plan",
      "test-plan.html",
      "/v1/wam/automation/testplans/POST",
      false,
      "Adds a new test plan"),
  RETRIEVE_TEST_PLANS(
      "RETRIEVE_TEST_PLANS",
      "",
      "",
      "/v1/wam/automation/testplans/GET",
      false,
      "Fetches test plans list"),
  RETRIEVE_TEST_PLAN(
      "RETRIEVE_TEST_PLAN",
      "",
      "",
      "/v1/wam/automation/testplans/{path-param}/GET",
      false,
      "Fetches a test plan by id"),
  UPDATE_TEST_PLAN(
      "UPDATE_TEST_PLAN",
      "",
      "",
      "/v1/wam/automation/testplans/{path-param}/PUT",
      false,
      "Updates a test plan by id"),
  DELETE_TEST_PLAN(
      "DELETE_TEST_PLAN",
      "",
      "",
      "/v1/wam/automation/testplans/{path-param}/DELETE",
      false,
      "Deletes a test plan by id"),
  EXECUTE_BY_TEST_PLAN(
      "EXECUTE_BY_TEST_PLAN",
      "Execution",
      "execution.html",
      "/v1/wam/automation/executions/testplans/{path-param}/POST",
      false,
      "Executes automation by test plan id"),
  EXECUTE_BY_TEST_CASE(
      "EXECUTE_BY_TEST_CASE",
      "",
      "",
      "/v1/wam/automation/executions/testcases/{path-param}/POST",
      false,
      "Executes automation by test case id"),
  EXECUTE_BY_TEST_CASE_STEP(
      "EXECUTE_BY_TEST_CASE_STEP",
      "",
      "",
      "/v1/wam/automation/executions/testcasesteps/{path-param}/POST",
      false,
      "Executes automation by test case step id");

  private final String id;
  private final String name;
  private final String page;
  private final String permissionType;
  private final boolean isDisplay;
  private final String description;

  PermissionType(
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
  public static PermissionType getByValue(final String name) {
    return Arrays.stream(PermissionType.values())
        .filter(enumRole -> enumRole.name.equals(name))
        .findFirst()
        .orElse(HOME);
  }
}
