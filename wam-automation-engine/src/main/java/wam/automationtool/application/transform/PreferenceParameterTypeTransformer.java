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
