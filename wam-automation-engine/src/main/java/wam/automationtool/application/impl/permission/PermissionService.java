package wam.automationtool.application.impl.permission;

import wam.automationtool.application.dto.permission.PermissionResponseDto;

public interface PermissionService {

  void addPermission();

  void assignPermission(String permissionId, String userTypeId);

  PermissionResponseDto getPermission();
}
