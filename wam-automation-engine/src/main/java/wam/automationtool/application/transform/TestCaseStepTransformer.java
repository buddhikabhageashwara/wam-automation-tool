package wam.automationtool.application.transform;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import wam.automationtool.application.dto.alias.AliasDto;
import wam.automationtool.application.dto.execute.TestCaseStepExecuteRequestDto;
import wam.automationtool.application.dto.execute.TestCaseStepExecuteResponseDto;
import wam.automationtool.application.dto.parameter.AssertParameterDto;
import wam.automationtool.application.dto.parameter.PreferenceParameterDto;
import wam.automationtool.application.dto.report.TestCaseStepExecutionDto;
import wam.automationtool.application.dto.testcasestep.TestCaseStepAddRequestDto;
import wam.automationtool.application.dto.testcasestep.TestCaseStepDto;
import wam.automationtool.domain.entity.testcase.TestCase;
import wam.automationtool.domain.entity.testcasestep.TestCaseStep;
import wam.automationtool.domain.entity.testcasestep.parameter.AssertParameter;
import wam.automationtool.domain.entity.testcasestep.parameter.PreferenceParameter;
import wam.automationtool.domain.entity.testcasestep.parameter.PreferenceParameterType;

@Service
public class TestCaseStepTransformer {

  public TestCaseStep TestCaseStepAddRequestDtoToTestCaseStep(
      final TestCaseStepAddRequestDto testCaseStepAddRequestDto, final TestCase testCase) {
    return TestCaseStep.builder()
        .testCaseStepType(testCaseStepAddRequestDto.getTestCaseStepType())
        .executionOrder(testCaseStepAddRequestDto.getExecutionOrder())
        .testCaseStepName(testCaseStepAddRequestDto.getTestCaseStepName())
        .testCase(testCase)
        .build();
  }

  public List<TestCaseStepDto> testCaseStepListToTestCaseStepDtoList(
      final List<TestCaseStep> testCaseStepList) {
    return Objects.isNull(testCaseStepList)
        ? Collections.emptyList()
        : testCaseStepList.stream()
            .map(this::transformTestCaseStepToDto)
            .collect(Collectors.toList());
  }

  public TestCaseStepDto transformTestCaseStepToDto(final TestCaseStep testCaseStep) {
    if (Objects.isNull(testCaseStep)) {
      return null;
    }
    return TestCaseStepDto.builder()
        .id(testCaseStep.getId())
        .testCaseStepType(testCaseStep.getTestCaseStepType())
        .executionOrder(testCaseStep.getExecutionOrder())
        .testCaseStepName(testCaseStep.getTestCaseStepName())
        .testCaseId(
            Objects.nonNull(testCaseStep.getTestCase()) ? testCaseStep.getTestCase().getId() : null)
        .preferenceParameterDtoList(
            transformPreferenceParametersToDtoList(
                testCaseStep.getPreferenceParameters().stream().toList()))
        .assertParameterDtoList(
            transformAssertParametersToDtoList(
                testCaseStep.getAssertParameters().stream().toList()))
        .build();
  }

  private List<PreferenceParameterDto> transformPreferenceParametersToDtoList(
      final List<PreferenceParameter> preferenceParameters) {
    return Objects.isNull(preferenceParameters)
        ? Collections.emptyList()
        : preferenceParameters.stream()
            .map(this::transformPreferenceParameterToDto)
            .collect(Collectors.toList());
  }

  private PreferenceParameterDto transformPreferenceParameterToDto(
      final PreferenceParameter preferenceParameter) {
    if (Objects.isNull(preferenceParameter)) {
      return null;
    }
    PreferenceParameterType parameterType = preferenceParameter.getPreferenceParameterType();
    return PreferenceParameterDto.builder()
        .id(preferenceParameter.getId())
        .parameterValue(preferenceParameter.getParameterValue())
        .parameterName(Objects.nonNull(parameterType) ? parameterType.getParameterName() : null)
        .parameterDisplayName(
            Objects.nonNull(parameterType) ? parameterType.getParameterDisplayName() : null)
        .preferenceParameterTypeId(Objects.nonNull(parameterType) ? parameterType.getId() : null)
        .testCaseStepId(
            Objects.nonNull(preferenceParameter.getTestCaseStep())
                ? preferenceParameter.getTestCaseStep().getId()
                : null)
        .build();
  }

  private List<AssertParameterDto> transformAssertParametersToDtoList(
      final List<AssertParameter> assertParameters) {
    return Objects.isNull(assertParameters)
        ? Collections.emptyList()
        : assertParameters.stream()
            .map(this::transformAssertParameterToDto)
            .collect(Collectors.toList());
  }

  private AssertParameterDto transformAssertParameterToDto(final AssertParameter assertParameter) {
    if (Objects.isNull(assertParameter)) {
      return null;
    }
    return AssertParameterDto.builder()
        .id(assertParameter.getId())
        .parameterName(assertParameter.getParameterName())
        .parameterValue(assertParameter.getParameterValue())
        .testCaseStepId(
            Objects.nonNull(assertParameter.getTestCaseStep())
                ? assertParameter.getTestCaseStep().getId()
                : null)
        .build();
  }

  public TestCaseStepExecutionDto testCaseStepExecuteResponseDtoToTestCaseStepExecutionDto(
      final TestCaseStepExecuteResponseDto testCaseStepExecuteResponseDto,
      final TestCaseStep testCaseStep) {
    return TestCaseStepExecutionDto.builder()
        .startTime(testCaseStepExecuteResponseDto.getStartTime())
        .endTime(testCaseStepExecuteResponseDto.getEndTime())
        .id(testCaseStep.getId())
        .executionOrder(testCaseStep.getExecutionOrder())
        .testCaseStepType(testCaseStep.getTestCaseStepType())
        .testCaseStepName(testCaseStep.getTestCaseStepName())
        .expectedResult(testCaseStepExecuteResponseDto.getExpectedResult())
        .actualResult(testCaseStepExecuteResponseDto.getActualResult())
        .status(testCaseStepExecuteResponseDto.getStatus())
        .build();
  }

  public TestCaseStepExecuteRequestDto toTestCaseStepExecuteRequestDto(
      final List<AliasDto> aliasDtoList,
      final TestCaseStepDto testCaseStepDto,
      final String executionId,
      final String token) {
    return TestCaseStepExecuteRequestDto.builder()
        .token(token)
        .executionId(executionId)
        .aliasDtoList(aliasDtoList)
        .testCaseStepDto(testCaseStepDto)
        .testCaseStepType(testCaseStepDto.getTestCaseStepType())
        .build();
  }
}
