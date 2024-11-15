package wam.automationtool.application.dto.parameter;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class PreferenceParameterTypesResponseDto {

  private List<PreferenceParameterTypeDto>  preferenceParameterTypeDtoList;
}
