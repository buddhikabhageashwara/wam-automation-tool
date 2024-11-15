package wam.automationtool.application.transform;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import wam.automationtool.domain.entity.testcasestep.TestCaseStep;
import wam.automationtool.domain.entity.testcasestep.parameter.AssertParameter;

@Service
public class AssertParameterTransformer {

  public List<AssertParameter> preferenceParametersMapToPreferenceParameterList(
      final LinkedHashMap<String, String> assertParameters, final TestCaseStep testCaseStep) {

    return assertParameters.entrySet().stream()
        .map(
            entry -> {
              final String parameterName = entry.getKey();
              final String parameterValue = entry.getValue();
              return AssertParameter.builder()
                  .parameterName(parameterName)
                  .parameterValue(parameterValue)
                  .testCaseStep(testCaseStep)
                  .build();
            })
        .collect(Collectors.toList());
  }
}
