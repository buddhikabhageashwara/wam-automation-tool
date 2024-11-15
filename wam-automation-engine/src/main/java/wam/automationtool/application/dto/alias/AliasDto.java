package wam.automationtool.application.dto.alias;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Builder
@Getter
@Setter
public class AliasDto {

  private long id;
  private String aliasType;
  private String aliasName;
  private List<AliasParameterDto> aliasParameterDtoList;
}
