package wam.automationtool.application.transform;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import wam.automationtool.application.dto.alias.AliasAddRequestDto;
import wam.automationtool.application.dto.alias.AliasDto;
import wam.automationtool.application.dto.alias.AliasParameterDto;
import wam.automationtool.domain.entity.testcasestep.alias.Alias;
import wam.automationtool.domain.entity.testcasestep.alias.parameter.AliasParameter;

@Service
public class AliasTransformer {

  public Alias aliasAddRequestDtoToAlias(final AliasAddRequestDto aliasAddRequestDto) {
    return Alias.builder()
            .aliasName(aliasAddRequestDto.getAliasName())
            .aliasType(aliasAddRequestDto.getAliasType())
            .build();
  }

  public List<AliasDto> aliasListToAliasDtoList(final List<Alias> aliasList) {
    if (Objects.isNull(aliasList) || aliasList.isEmpty()) {
      return List.of();
    }
    return aliasList.stream().map(this::aliasToAliasDto).collect(Collectors.toList());
  }

  public AliasDto aliasToAliasDto(final Alias alias) {
    return AliasDto.builder()
            .id(alias.getId())
            .aliasType(alias.getAliasType())
            .aliasName(alias.getAliasName())
            .aliasParameterDtoList(convertAliasParametersToDto(alias.getAliasParameters()))
            .build();
  }

  private List<AliasParameterDto> convertAliasParametersToDto(final List<AliasParameter> aliasParameters) {
    if (Objects.isNull(aliasParameters) || aliasParameters.isEmpty()) {
      return List.of();
    }
    return aliasParameters.stream()
        .map(this::convertAliasParameterToDto)
        .collect(Collectors.toList());
  }

  private AliasParameterDto convertAliasParameterToDto(final AliasParameter aliasParameter) {
    return AliasParameterDto.builder()
        .id(aliasParameter.getId())
        .parameterValue(aliasParameter.getParameterValue())
        .parameterName(aliasParameter.getAliasParameterType().getParameterName())
        .parameterDisplayName(aliasParameter.getAliasParameterType().getParameterDisplayName())
        .aliasParameterTypeId(aliasParameter.getAliasParameterType().getId())
        .build();
  }
}
