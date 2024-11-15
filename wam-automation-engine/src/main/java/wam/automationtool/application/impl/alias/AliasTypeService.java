package wam.automationtool.application.impl.alias;

import wam.automationtool.application.dto.testcase.TestCaseAddRequestDto;
import wam.automationtool.application.dto.testcase.TestCaseResponseDto;
import wam.automationtool.application.dto.testcase.TestCaseUpdateRequestDto;
import wam.automationtool.application.dto.testcase.TestCasesResponseDto;

public interface AliasTypeService {

  void addTestCase(TestCaseAddRequestDto testCaseAddRequestDto);

  TestCasesResponseDto getTestCases(long testPlanId);

  TestCaseResponseDto getTestCase(long testCaseId);

  void updateTestCase(long testCaseId, TestCaseUpdateRequestDto testCaseUpdateRequestDto);

  void deleteTestCase(long testCaseId);
}
