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

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static wam.automationtool.application.config.AppConstant.AuthConstants.PREFERENCE_PARAMETER_TYPE_NOT_FOUND_CODE;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import wam.automationtool.application.exception.PreferenceParameterTypeNotFoundException;
import wam.automationtool.domain.entity.testcasestep.TestCaseStep;
import wam.automationtool.domain.entity.testcasestep.parameter.PreferenceParameter;
import wam.automationtool.domain.entity.testcasestep.parameter.PreferenceParameterType;

@Service
public class PreferenceParameterTransformer {

  public List<PreferenceParameter> preferenceParametersMapToPreferenceParameterList(
      final LinkedHashMap<String, String> preferenceParameters,
      final TestCaseStep testCaseStep,
      final List<PreferenceParameterType> preferenceParameterTypeList) {

    return preferenceParameters.entrySet().stream()
        .map(
            entry -> {
              final Long preferenceParameterTypeId = Long.valueOf(entry.getKey());
              final String parameterValue = entry.getValue();
              final PreferenceParameterType preferenceParameterType =
                  preferenceParameterTypeList.stream()
                      .filter(type -> type.getId().equals(preferenceParameterTypeId))
                      .findFirst()
                      .orElseThrow(
                          () ->
                              new PreferenceParameterTypeNotFoundException(
                                  NOT_FOUND,
                                  PREFERENCE_PARAMETER_TYPE_NOT_FOUND_CODE,
                                  "error.preference.parameter.type.not.found"));
              return PreferenceParameter.builder()
                  .parameterValue(parameterValue)
                  .preferenceParameterType(preferenceParameterType)
                  .testCaseStep(testCaseStep)
                  .build();
            })
        .collect(Collectors.toList());
  }
}
