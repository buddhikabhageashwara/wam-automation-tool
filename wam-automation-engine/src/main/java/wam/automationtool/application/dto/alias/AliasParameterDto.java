package wam.automationtool.application.dto.alias;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class AliasParameterDto {

  private long id;
  private String parameterValue;
  private String parameterName;
  private String parameterDisplayName;
  private long aliasParameterTypeId;
}
