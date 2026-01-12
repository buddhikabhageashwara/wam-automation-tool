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

package wam.automationtool.application.util;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;
import wam.automationtool.application.dto.alias.AliasDto;
import wam.automationtool.application.dto.alias.AliasParameterDto;
import wam.automationtool.application.dto.execute.TestCaseStepExecuteRequestDto;
import wam.automationtool.application.dto.parameter.PreferenceParameterDto;

@Slf4j
public class AliasManager {

    public static AliasDto extractAlias(final String value, final List<AliasDto> aliasDtoList) {
    return aliasDtoList.stream()
        .filter(aliasDto -> value.equals(aliasDto.getAliasName()))
        .findFirst()
        .orElse(AliasDto.builder().aliasParameterDtoList(new ArrayList<>()).build());
    }

    public static List<AliasParameterDto> getAliasParametersForAlias(
            final TestCaseStepExecuteRequestDto testCaseStepExecuteRequestDto,
            final String testCaseStepParameterType) {
        final List<PreferenceParameterDto> preferenceParameterDtoList =
                testCaseStepExecuteRequestDto.getTestCaseStepDto().getPreferenceParameterDtoList();
        final List<AliasDto> aliasDtoList = testCaseStepExecuteRequestDto.getAliasDtoList();
        final String tcsPreferenceParameterValue =
                getParameterValueByName(
                        preferenceParameterDtoList, testCaseStepParameterType);
        if (Objects.isNull(tcsPreferenceParameterValue)) {
            log.debug(
                    testCaseStepParameterType.concat(
                            " preference parameter is null. test case step id: {}"),
                    testCaseStepExecuteRequestDto.getTestCaseStepDto().getId());
            return new ArrayList<>();
        }
        final AliasDto extractedAlias =
                extractAlias(tcsPreferenceParameterValue, aliasDtoList);
        return extractedAlias.getAliasParameterDtoList();
    }

    private static String getParameterValueByName(
            final List<PreferenceParameterDto> preferenceParameterDtoList, final String parameterName) {
        return preferenceParameterDtoList.stream()
                .filter(preference -> parameterName.equals(preference.getParameterName()))
                .map(PreferenceParameterDto::getParameterValue)
                .findFirst()
                .orElse(null);
    }
}
