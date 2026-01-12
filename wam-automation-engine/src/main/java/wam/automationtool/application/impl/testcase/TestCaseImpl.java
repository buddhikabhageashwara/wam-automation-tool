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

package wam.automationtool.application.impl.testcase;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static wam.automationtool.application.config.AppConstant.AuthConstants.TEST_CASE_ALREADY_EXIST_CODE;
import static wam.automationtool.application.config.AppConstant.AuthConstants.TEST_CASE_NOT_FOUND_CODE;
import static wam.automationtool.application.config.AppConstant.AuthConstants.TEST_PLAN_NOT_FOUND_CODE;

import java.util.List;
import java.util.Optional;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import wam.automationtool.application.dto.testcase.TestCaseAddRequestDto;
import wam.automationtool.application.dto.testcase.TestCaseDto;
import wam.automationtool.application.dto.testcase.TestCaseResponseDto;
import wam.automationtool.application.dto.testcase.TestCaseUpdateRequestDto;
import wam.automationtool.application.dto.testcase.TestCasesResponseDto;
import wam.automationtool.application.exception.TestCaseAlreadyExistException;
import wam.automationtool.application.exception.TestCaseNotFoundException;
import wam.automationtool.application.exception.TestPlanNotFoundException;
import wam.automationtool.application.transform.TestCaseTransformer;
import wam.automationtool.domain.entity.testcase.TestCase;
import wam.automationtool.domain.entity.testplan.TestPlan;
import wam.automationtool.domain.service.TestCaseDomainService;
import wam.automationtool.domain.service.TestPlanDomainService;
import wam.automationtool.infrastructure.provider.AuthDetailsProvider;

@Service
@Slf4j
@RequiredArgsConstructor
public class TestCaseImpl extends AuthDetailsProvider implements TestCaseService {

  private final TestCaseTransformer testCaseTransformer;
  private final TestCaseDomainService testCaseDomainService;
  private final TestPlanDomainService testPlanDomainService;

  @Override
  @Transactional
  public void addTestCase(final TestCaseAddRequestDto testCaseAddRequestDto) {
    final boolean isTestCaseExist =
        testCaseDomainService
            .findByTestCaseName(testCaseAddRequestDto.getTestCaseName())
            .isPresent();
    final TestPlan existingTestPlan =
        testPlanDomainService
            .findById(testCaseAddRequestDto.getTestPlanId())
            .orElseThrow(
                () ->
                    new TestPlanNotFoundException(
                        NOT_FOUND, TEST_PLAN_NOT_FOUND_CODE, "error.test.plan.not.found"));
    if (isTestCaseExist) {
      throw new TestCaseAlreadyExistException(
          BAD_REQUEST, TEST_CASE_ALREADY_EXIST_CODE, "error.test.case.already.exist");
    }
    final TestCase testCase =
        testCaseTransformer.testCaseAddRequestDtoToTestCase(
            testCaseAddRequestDto, existingTestPlan);
    testCaseDomainService.add(testCase);
  }

  @Override
  public TestCasesResponseDto getTestCases(final long testPlanId) {
    final TestPlan existingTestPlan =
        testPlanDomainService
            .findById(testPlanId)
            .orElseThrow(
                () ->
                    new TestPlanNotFoundException(
                        NOT_FOUND, TEST_PLAN_NOT_FOUND_CODE, "error.test.plan.not.found"));
    final List<TestCase> testCaseList = testCaseDomainService.findByTestPlan(existingTestPlan);
    final List<TestCaseDto> testCaseDtoList =
        testCaseTransformer.testCaseListToTestCaseDtoList(testCaseList);
    return TestCasesResponseDto.builder().testCaseDtoList(testCaseDtoList).build();
  }

  @Override
  public TestCaseResponseDto getTestCase(final long testCaseId) {
    TestCase testCase =
        testCaseDomainService
            .findById(testCaseId)
            .orElseThrow(
                () ->
                    new TestCaseNotFoundException(
                        NOT_FOUND, TEST_CASE_NOT_FOUND_CODE, "error.test.case.not.found"));
    TestCaseDto testCaseDto = testCaseTransformer.testCaseToTestCaseDto(testCase);
    return TestCaseResponseDto.builder().testCaseDto(testCaseDto).build();
  }

  @Override
  public void updateTestCase(
      final long testCaseId, final TestCaseUpdateRequestDto testCaseUpdateRequestDto) {
    final TestCase existingTestCase =
        testCaseDomainService
            .findById(testCaseId)
            .orElseThrow(
                () ->
                    new TestCaseNotFoundException(
                        NOT_FOUND, TEST_CASE_NOT_FOUND_CODE, "error.test.case.not.found"));
    final Optional<TestCase> existingTestCaseByName =
        testCaseDomainService.findByTestCaseName(testCaseUpdateRequestDto.getTestCaseName());
    final boolean isTestCaseExist = existingTestCaseByName.isPresent();
    final boolean isNotSameTestCase =
        isTestCaseExist && (existingTestCase.getId() != existingTestCaseByName.get().getId());
    if (isNotSameTestCase && isTestCaseExist) {
      throw new TestCaseAlreadyExistException(
          BAD_REQUEST, TEST_CASE_ALREADY_EXIST_CODE, "error.test.case.already.exist");
    }
    testCaseTransformer.testCaseUpdateRequestDtoToTestCase(
        existingTestCase, testCaseUpdateRequestDto);
    testCaseDomainService.update(existingTestCase);
  }

  @Override
  public void deleteTestCase(final long testCaseId) {
    final TestCase testCase =
        testCaseDomainService
            .findById(testCaseId)
            .orElseThrow(
                () ->
                    new TestCaseNotFoundException(
                        NOT_FOUND, TEST_CASE_NOT_FOUND_CODE, "error.test.case.not.found"));
    testCaseDomainService.delete(testCase);
  }
}
