package wam.automationtool.application.dto.alias;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class AliasParameterTypeDto {

  private long id;
  private String parameterDisplayName;
  private String parameterName;
}
