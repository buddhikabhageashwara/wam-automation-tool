package wam.automationtool.application.impl.parameter;

import wam.automationtool.application.dto.parameter.PreferenceParameterTypeAddRequestDto;
import wam.automationtool.application.dto.parameter.PreferenceParameterTypeResponseDto;
import wam.automationtool.application.dto.parameter.PreferenceParameterTypeUpdateRequestDto;
import wam.automationtool.application.dto.parameter.PreferenceParameterTypesResponseDto;

public interface PreferenceParameterTypeService {

  void addPreferenceParameterType(
      PreferenceParameterTypeAddRequestDto preferenceParameterTypeAddRequestDto);

  PreferenceParameterTypesResponseDto getPreferenceParameterTypes();
  PreferenceParameterTypeResponseDto getPreferenceParameterType(long preferenceParameterTypeId);

  void updatePreferenceParameterType(
      long preferenceParameterTypeId,
      PreferenceParameterTypeUpdateRequestDto preferenceParameterTypeUpdateRequestDto);

  void deletePreferenceParameterType(long preferenceParameterTypeId);
}
