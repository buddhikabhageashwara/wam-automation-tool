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
