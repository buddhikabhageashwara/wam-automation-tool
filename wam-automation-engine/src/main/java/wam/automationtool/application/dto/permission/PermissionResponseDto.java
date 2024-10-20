package wam.automationtool.application.dto.permission;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class PermissionResponseDto {

  private List<PermissionDto> permissionDtoList;
}
