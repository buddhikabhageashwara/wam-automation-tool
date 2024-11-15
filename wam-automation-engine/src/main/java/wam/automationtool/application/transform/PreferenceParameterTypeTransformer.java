package wam.automationtool.application.transform;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import wam.automationtool.application.dto.parameter.PreferenceParameterTypeAddRequestDto;
import wam.automationtool.application.dto.parameter.PreferenceParameterTypeDto;
import wam.automationtool.application.dto.parameter.PreferenceParameterTypeUpdateRequestDto;
import wam.automationtool.domain.entity.testcasestep.parameter.PreferenceParameterType;

@Service
public class PreferenceParameterTypeTransformer {

  public PreferenceParameterType preferenceParameterTypeAddRequestDtoToPreferenceParameterType(
      final PreferenceParameterTypeAddRequestDto preferenceParameterTypeAddRequestDto) {
    return PreferenceParameterType.builder()
        .parameterDisplayName(preferenceParameterTypeAddRequestDto.getParameterDisplayName())
        .parameterName(preferenceParameterTypeAddRequestDto.getParameterName())
        .build();
  }

  public PreferenceParameterTypeDto preferenceParameterTypeToDto(
      final PreferenceParameterType preferenceParameterType) {
    return PreferenceParameterTypeDto.builder()
        .id(preferenceParameterType.getId())
        .parameterDisplayName(preferenceParameterType.getParameterDisplayName())
        .parameterName(preferenceParameterType.getParameterName())
        .build();
  }

  public List<PreferenceParameterTypeDto> preferenceParameterTypeListToDtoList(
      final List<PreferenceParameterType> preferenceParameterTypes) {
    return preferenceParameterTypes.stream()
        .map(this::preferenceParameterTypeToDto)
        .collect(Collectors.toList());
  }

  public PreferenceParameterType preferenceParameterTypeUpdateRequestDtoToPreferenceParameterType(
      final PreferenceParameterTypeUpdateRequestDto preferenceParameterTypeUpdateRequestDto) {
    return PreferenceParameterType.builder()
        .parameterDisplayName(preferenceParameterTypeUpdateRequestDto.getParameterDisplayName())
        .build();
  }
}
