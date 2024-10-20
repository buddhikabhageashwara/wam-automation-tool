package wam.automationtool.application.dto.permission;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class PermissionDto {

  private String id;
  private String permissionType;
}
