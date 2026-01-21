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

package wam.automationtool.application.impl.testcasestep;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static wam.automationtool.application.config.AppConstant.AuthConstants.INVALID_TEST_CASE_STEP_TYPE_CODE;
import static wam.automationtool.application.config.AppConstant.AuthConstants.PREFERENCE_PARAMETER_TYPE_NOT_FOUND_CODE;
import static wam.automationtool.application.config.AppConstant.AuthConstants.TEST_CASE_NOT_FOUND_CODE;
import static wam.automationtool.application.config.AppConstant.AuthConstants.TEST_CASE_STEP_NOT_FOUND_CODE;

import jakarta.transaction.Transactional;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import wam.automationtool.application.dto.testcasestep.*;
import wam.automationtool.application.exception.InvalidTestCaseStepTypeException;
import wam.automationtool.application.exception.PreferenceParameterTypeNotFoundException;
import wam.automationtool.application.exception.TestCaseNotFoundException;
import wam.automationtool.application.exception.TestCaseStepNotFoundException;
import wam.automationtool.application.transform.AssertParameterTransformer;
import wam.automationtool.application.transform.PreferenceParameterTransformer;
import wam.automationtool.application.transform.TestCaseStepTransformer;
import wam.automationtool.domain.entity.testcase.TestCase;
import wam.automationtool.domain.entity.testcasestep.TestCaseStep;
import wam.automationtool.domain.entity.testcasestep.TestCaseStepType;
import wam.automationtool.domain.entity.testcasestep.parameter.AssertParameter;
import wam.automationtool.domain.entity.testcasestep.parameter.PreferenceParameter;
import wam.automationtool.domain.entity.testcasestep.parameter.PreferenceParameterType;
import wam.automationtool.domain.service.AssertParameterDomainService;
import wam.automationtool.domain.service.PreferenceParameterDomainService;
import wam.automationtool.domain.service.PreferenceParameterTypeDomainService;
import wam.automationtool.domain.service.TestCaseDomainService;
import wam.automationtool.domain.service.TestCaseStepDomainService;
import wam.automationtool.infrastructure.provider.AuthDetailsProvider;

@Service
@Slf4j
@RequiredArgsConstructor
public class TestCaseStepImpl extends AuthDetailsProvider implements TestCaseStepService {

  private final TestCaseStepTransformer testCaseStepTransformer;
  private final PreferenceParameterTransformer preferenceParameterTransformer;
  private final AssertParameterTransformer assertParameterTransformer;
  private final TestCaseStepDomainService testCaseStepDomainService;
  private final TestCaseDomainService testCaseDomainService;
  private final PreferenceParameterDomainService preferenceParameterDomainService;
  private final PreferenceParameterTypeDomainService preferenceParameterTypeDomainService;
  private final AssertParameterDomainService assertParameterDomainService;

  @Override
  @Transactional
  public void addTestCaseStep(final TestCaseStepAddRequestDto testCaseStepAddRequestDto) {
    checkValidityOfTestCaseStepType(testCaseStepAddRequestDto);
    final TestCase testCase =
        testCaseDomainService
            .findById(testCaseStepAddRequestDto.getTestCaseId())
            .orElseThrow(
                () ->
                    new TestCaseNotFoundException(
                        NOT_FOUND, TEST_CASE_NOT_FOUND_CODE, "error.test.case.not.found"));
    final TestCaseStep preparedTestCaseStep =
        testCaseStepTransformer.TestCaseStepAddRequestDtoToTestCaseStep(
            testCaseStepAddRequestDto, testCase);
    final TestCaseStep newTestCaseStep = testCaseStepDomainService.add(preparedTestCaseStep);
    if (Objects.nonNull(testCaseStepAddRequestDto.getPreferenceParameters())) {
      addPreferenceParameters(testCaseStepAddRequestDto, newTestCaseStep);
    }
    if (Objects.nonNull(testCaseStepAddRequestDto.getAssertParameters())) {
      addAssertParameters(testCaseStepAddRequestDto, newTestCaseStep);
    }
  }

  /**
   * @param bulkTestCaseStepsAddRequestDto
   */
  @Override
  public void addTestCaseStepsInBulk(
      final BulkTestCaseStepsAddRequestDto bulkTestCaseStepsAddRequestDto) {


  }

  private void checkValidityOfTestCaseStepType(
      final TestCaseStepAddRequestDto testCaseStepAddRequestDto) {
    final boolean isNotAValidTestCaseStepType =
        TestCaseStepType.isNotExist(testCaseStepAddRequestDto.getTestCaseStepType());
    if (isNotAValidTestCaseStepType) {
      throw new InvalidTestCaseStepTypeException(
          BAD_REQUEST, INVALID_TEST_CASE_STEP_TYPE_CODE, "error.invalid.test.case.step.type");
    }
  }

  private void addPreferenceParameters(
      final TestCaseStepAddRequestDto testCaseStepAddRequestDto,
      final TestCaseStep newTestCaseStep) {
    final List<PreferenceParameterType> preferenceParameterTypeList =
        getPreferenceParameterTypes(testCaseStepAddRequestDto.getPreferenceParameters());
    final List<PreferenceParameter> preferenceParameterList =
        preferenceParameterTransformer.preferenceParametersMapToPreferenceParameterList(
            testCaseStepAddRequestDto.getPreferenceParameters(),
            newTestCaseStep,
            preferenceParameterTypeList);
    preferenceParameterDomainService.addAll(preferenceParameterList);
  }

  private void addAssertParameters(
      final TestCaseStepAddRequestDto testCaseStepAddRequestDto,
      final TestCaseStep newTestCaseStep) {
    final List<AssertParameter> assertParameterList =
        assertParameterTransformer.preferenceParametersMapToPreferenceParameterList(
            testCaseStepAddRequestDto.getAssertParameters(), newTestCaseStep);
    assertParameterDomainService.addAll(assertParameterList);
  }

  private List<PreferenceParameterType> getPreferenceParameterTypes(
      final LinkedHashMap<String, String> preferenceParameters) {
    return preferenceParameters.entrySet().stream()
        .map(
            entry -> {
              final Long typeId = Long.valueOf(entry.getKey());
              PreferenceParameterType preferenceParameterType =
                  preferenceParameterTypeDomainService
                      .findById(typeId)
                      .orElseThrow(
                          () ->
                              new PreferenceParameterTypeNotFoundException(
                                  NOT_FOUND,
                                  PREFERENCE_PARAMETER_TYPE_NOT_FOUND_CODE,
                                  "error.preference.parameter.type.not.found"));
              return preferenceParameterType;
            })
        .collect(Collectors.toList());
  }

  @Override
  public TestCaseStepsResponseDto getTestCaseSteps(final long testCaseId) {
    final TestCase testCase =
        testCaseDomainService
            .findById(testCaseId)
            .orElseThrow(
                () ->
                    new TestCaseNotFoundException(
                        NOT_FOUND, TEST_CASE_NOT_FOUND_CODE, "error.test.case.not.found"));
    final List<TestCaseStep> testCaseStepList = testCaseStepDomainService.findByTestCase(testCase);
    final List<TestCaseStepDto> testCaseStepDtoList =
        testCaseStepTransformer.testCaseStepListToTestCaseStepDtoList(testCaseStepList);
    return TestCaseStepsResponseDto.builder().testCaseStepDtoList(testCaseStepDtoList).build();
  }

  @Override
  public TestCaseStepResponseDto getTestCaseStep(final long testCaseStepId) {
    final TestCaseStep testCaseStep =
        testCaseStepDomainService
            .findById(testCaseStepId)
            .orElseThrow(
                () ->
                    new TestCaseStepNotFoundException(
                        NOT_FOUND,
                        TEST_CASE_STEP_NOT_FOUND_CODE,
                        "error.test.case.step.not.found"));
    final TestCaseStepDto testCaseStepDto =
        testCaseStepTransformer.transformTestCaseStepToDto(testCaseStep);
    return TestCaseStepResponseDto.builder().testCaseStepDto(testCaseStepDto).build();
  }

  @Override
  public void deleteTestCaseStep(final long testCaseStepId) {
    final TestCaseStep testCaseStep =
        testCaseStepDomainService
            .findById(testCaseStepId)
            .orElseThrow(
                () ->
                    new TestCaseStepNotFoundException(
                        NOT_FOUND,
                        TEST_CASE_STEP_NOT_FOUND_CODE,
                        "error.test.case.step.not.found"));
    testCaseStepDomainService.delete(testCaseStep);
  }

  @Override
  public void updateTestCaseStep(final TestCaseStepUpdateRequestDto testCaseStepUpdateRequestDto) {
    final TestCaseStep testCaseStep =
        testCaseStepDomainService
            .findById(testCaseStepUpdateRequestDto.getTestCaseStepId())
            .orElseThrow(
                () ->
                    new TestCaseStepNotFoundException(
                        NOT_FOUND,
                        TEST_CASE_STEP_NOT_FOUND_CODE,
                        "error.test.case.step.not.found"));
    testCaseStep.setTestCaseStepName(testCaseStepUpdateRequestDto.getTestCaseStepName());
    testCaseStep.setDescription(testCaseStepUpdateRequestDto.getDescription());
    testCaseStep.setExecutionOrder(testCaseStepUpdateRequestDto.getExecutionOrder());
    testCaseStepDomainService.update(testCaseStep);
  }
}
