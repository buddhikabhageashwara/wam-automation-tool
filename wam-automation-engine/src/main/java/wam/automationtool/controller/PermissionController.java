package wam.automationtool.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import wam.automationtool.application.dto.permission.PermissionResponseDto;
import wam.automationtool.application.impl.permission.PermissionService;

@RestController
@RequestMapping("/v1/wam/automation/permissions")
@RequiredArgsConstructor
public final class PermissionController {

  private final PermissionService permissionService;

  @PostMapping
  public ResponseEntity<Void> addPermission() {
    permissionService.addPermission();
    return ResponseEntity.ok().build();
  }

  @GetMapping
  public ResponseEntity<PermissionResponseDto> getPermission() {
    return ResponseEntity.ok(permissionService.getPermission());
  }

  @PostMapping("/{permission-id}/usertypes/{user-type-id}")
  public ResponseEntity<Void> assignPermission(
      @PathVariable("permission-id") String permissionId,
      @PathVariable("user-type-id") String userTypeId) {
    permissionService.assignPermission(permissionId, userTypeId);
    return ResponseEntity.ok().build();
  }
}
