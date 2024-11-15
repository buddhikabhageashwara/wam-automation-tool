package wam.automationtool.application.transform;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import wam.automationtool.domain.entity.testcasestep.alias.Alias;
import wam.automationtool.domain.entity.testcasestep.alias.parameter.AliasParameter;
import wam.automationtool.domain.entity.testcasestep.alias.parameter.AliasParameterType;

@Service
public class AliasParameterTransformer {

  public List<AliasParameter> aliasParameterMapToAliasParameterList(
          final LinkedHashMap<String, String> aliasParameters, final Alias alias,
          final List<AliasParameterType> aliasParameterTypeList) {
    if (Objects.isNull(aliasParameters) || Objects.isNull(alias) || Objects.isNull(aliasParameterTypeList)) {
      return List.of();
    }
    return aliasParameters.entrySet().stream()
            .filter(entry -> Objects.nonNull(entry.getKey()) && Objects.nonNull(entry.getValue()))
            .map(entry -> createAliasParameter(entry.getKey(), entry.getValue(), alias, aliasParameterTypeList))
            .collect(Collectors.toList());
  }

  private AliasParameter createAliasParameter(
          final String aliasParameterTypeId,
          final String parameterValue,
          final Alias alias,
          final List<AliasParameterType> aliasParameterTypeList) {
    return AliasParameter.builder()
            .parameterValue(parameterValue)
            .alias(alias)
            .aliasParameterType(findAliasParameterTypeById(aliasParameterTypeId, aliasParameterTypeList))
            .build();
  }

  private AliasParameterType findAliasParameterTypeById(
          final String aliasParameterTypeId,
          final List<AliasParameterType> aliasParameterTypeList) {
    return aliasParameterTypeList.stream()
            .filter(type -> Objects.equals(type.getId(), Long.parseLong(aliasParameterTypeId)))
            .findFirst()
            .orElse(null); // Return null if no matching AliasParameterType is found
  }
}
