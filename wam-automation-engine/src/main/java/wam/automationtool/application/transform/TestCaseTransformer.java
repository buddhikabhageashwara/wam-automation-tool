package wam.automationtool.application.transform;

import static wam.automationtool.domain.entity.testplan.ExecutionFrequency.checkValidityAndGetFrequency;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import wam.automationtool.application.dto.report.TestCaseExecutionSummaryDto;
import wam.automationtool.application.dto.testcase.TestCaseAddRequestDto;
import wam.automationtool.application.dto.testcase.TestCaseDto;
import wam.automationtool.application.dto.testcase.TestCaseUpdateRequestDto;
import wam.automationtool.domain.entity.testcase.TestCase;
import wam.automationtool.domain.entity.testplan.TestPlan;

@Service
public class TestCaseTransformer {

  final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");

  public TestCase testCaseAddRequestDtoToTestCase(
      final TestCaseAddRequestDto testCaseAddRequestDto, final TestPlan testPlan) {
    final String defaultOldDate = LocalDateTime.of(1970, 1, 1, 0, 0).toString();
    return TestCase.builder()
        .testCaseName(testCaseAddRequestDto.getTestCaseName())
        .description(testCaseAddRequestDto.getDescription())
        .executionOrder(testCaseAddRequestDto.getExecutionOrder())
        .executionScheduledDate(
            Optional.ofNullable(testCaseAddRequestDto.getExecutionScheduledDate())
                .map(date -> LocalDateTime.parse(date, formatter))
                .orElse(LocalDateTime.parse(defaultOldDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
        .executionExpiryDate(
            Optional.ofNullable(testCaseAddRequestDto.getExecutionExpiryDate())
                .map(date -> LocalDateTime.parse(date, formatter))
                .orElse(LocalDateTime.parse(defaultOldDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
        .executionFrequency(
            String.valueOf(
                checkValidityAndGetFrequency(testCaseAddRequestDto.getExecutionFrequency())))
        .testPlan(testPlan)
        .build();
  }

  public List<TestCaseDto> testCaseListToTestCaseDtoList(final List<TestCase> testCaseList) {
    return testCaseList.stream().map(this::testCaseToTestCaseDto).collect(Collectors.toList());
  }

  public TestCaseDto testCaseToTestCaseDto(final TestCase testCase) {
    return TestCaseDto.builder()
        .id(testCase.getId())
        .testCaseName(testCase.getTestCaseName())
        .description(testCase.getDescription())
        .testPlanId(testCase.getTestPlan().getId())
        .numberOfTestCaseSteps(
            Optional.ofNullable(testCase.getTestCaseSteps()).map(List::size).orElse(0))
        .executionScheduledDate(
            Optional.ofNullable(testCase.getExecutionScheduledDate())
                .map(formatter::format)
                .orElse(null))
        .executionExpiryDate(
            Optional.ofNullable(testCase.getExecutionExpiryDate())
                .map(formatter::format)
                .orElse(null))
        .executionFrequency(testCase.getExecutionFrequency())
        .executionOrder(testCase.getExecutionOrder())
        .build();
  }

  public void testCaseUpdateRequestDtoToTestCase(
      final TestCase existingTestCase, final TestCaseUpdateRequestDto testCaseUpdateRequestDto) {
    existingTestCase.setTestCaseName(testCaseUpdateRequestDto.getTestCaseName());
    existingTestCase.setDescription(testCaseUpdateRequestDto.getDescription());
    existingTestCase.setExecutionScheduledDate(
        LocalDateTime.parse(testCaseUpdateRequestDto.getExecutionScheduledDate(), formatter));
    existingTestCase.setExecutionExpiryDate(
        LocalDateTime.parse(testCaseUpdateRequestDto.getExecutionExpiryDate(), formatter));
    existingTestCase.setExecutionFrequency(
        String.valueOf(
            checkValidityAndGetFrequency(testCaseUpdateRequestDto.getExecutionFrequency())));
    existingTestCase.setExecutionOrder(testCaseUpdateRequestDto.getExecutionOrder());
  }

  public TestCaseExecutionSummaryDto toTestCaseExecutionSummaryDto(
          final int totalTestCases, final int passedTestCases,
          final int failedTestCases) {
    return TestCaseExecutionSummaryDto.builder()
        .totalTestCases(totalTestCases)
        .passedTestCases(passedTestCases)
        .failedTestCases(failedTestCases)
        .build();
  }
}
