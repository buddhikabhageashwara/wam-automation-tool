package wam.automationtool.application.transform;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import wam.automationtool.application.dto.alias.AliasParameterTypeAddRequestDto;
import wam.automationtool.application.dto.alias.AliasParameterTypeDto;
import wam.automationtool.application.dto.alias.AliasParameterTypeUpdateRequestDto;
import wam.automationtool.domain.entity.testcasestep.alias.parameter.AliasParameterType;

@Service
public class AliasParameterTypeTransformer {

  public AliasParameterType aliasParameterTypeAddRequestDtoToAliasParameterType(
      final AliasParameterTypeAddRequestDto aliasParameterTypeAddRequestDto) {
    return AliasParameterType.builder()
        .parameterDisplayName(aliasParameterTypeAddRequestDto.getParameterDisplayName())
        .parameterName(aliasParameterTypeAddRequestDto.getParameterName())
        .build();
  }

  public AliasParameterTypeDto aliasParameterTypeToDto(
      final AliasParameterType aliasParameterType) {
    return AliasParameterTypeDto.builder()
        .id(aliasParameterType.getId())
        .parameterDisplayName(aliasParameterType.getParameterDisplayName())
        .parameterName(aliasParameterType.getParameterName())
        .build();
  }

  public List<AliasParameterTypeDto> aliasParameterTypeListToDtoList(
      final List<AliasParameterType> aliasParameterTypes) {
    return aliasParameterTypes.stream()
        .map(this::aliasParameterTypeToDto)
        .collect(Collectors.toList());
  }

  public AliasParameterType aliasParameterTypeUpdateRequestDtoToAliasParameterType(
      final AliasParameterTypeUpdateRequestDto aliasParameterTypeUpdateRequestDto) {
    return AliasParameterType.builder()
        .parameterDisplayName(aliasParameterTypeUpdateRequestDto.getParameterDisplayName())
        .build();
  }
}
