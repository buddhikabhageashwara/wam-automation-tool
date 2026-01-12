/*
 * MIT License
 *
 * Copyright (c) 2024 buddhika bhageashwara alwis
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
