package wam.automationtool.application.dto.alias;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class AliasListResponseDto {

  private List<AliasDto> aliasDtoList;
}
