package wam.automationtool.application.dto.alias;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class AliasParameterDto {

  private long id;
  private String parameterValue;
  private String parameterName;
  private String parameterDisplayName;
  private long aliasParameterTypeId;
}
