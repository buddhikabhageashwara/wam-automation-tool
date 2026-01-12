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
