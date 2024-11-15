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
