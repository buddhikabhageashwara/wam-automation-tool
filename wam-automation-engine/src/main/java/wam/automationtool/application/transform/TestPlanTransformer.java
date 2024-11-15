package wam.automationtool.application.transform;

import static wam.automationtool.domain.entity.testplan.ExecutionFrequency.checkValidityAndGetFrequency;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import wam.automationtool.application.dto.testplan.TestPlanAddRequestDto;
import wam.automationtool.application.dto.testplan.TestPlanDto;
import wam.automationtool.application.dto.testplan.TestPlanUpdateRequestDto;
import wam.automationtool.domain.entity.testplan.TestPlan;

@Service
public class TestPlanTransformer {

  final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");

  public TestPlan testPlanAddRequestDtoToTestPlan(
      final TestPlanAddRequestDto testPlanAddRequestDto) {
    final String defaultOldDate = LocalDateTime.of(1970, 1, 1, 0, 0).toString();
    return TestPlan.builder()
        .testPlanName(testPlanAddRequestDto.getTestPlanName())
        .description(testPlanAddRequestDto.getDescription())
        .executionScheduledDate(
            Optional.ofNullable(testPlanAddRequestDto.getExecutionScheduledDate())
                .map(date -> LocalDateTime.parse(date, formatter))
                .orElse(LocalDateTime.parse(defaultOldDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
        .executionExpiryDate(
            Optional.ofNullable(testPlanAddRequestDto.getExecutionExpiryDate())
                .map(date -> LocalDateTime.parse(date, formatter))
                .orElse(LocalDateTime.parse(defaultOldDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
        .executionFrequency(
            String.valueOf(
                checkValidityAndGetFrequency(testPlanAddRequestDto.getExecutionFrequency())))
        .build();
  }

  public List<TestPlanDto> testPlanListToTestPlanDtoList(final List<TestPlan> testPlanList) {
    return testPlanList.stream().map(this::testPlanToTestPlanDto).collect(Collectors.toList());
  }

  public TestPlanDto testPlanToTestPlanDto(final TestPlan testPlan) {
    return TestPlanDto.builder()
        .id(testPlan.getId())
        .testPlanName(testPlan.getTestPlanName())
        .description(testPlan.getDescription())
        .executionScheduledDate(
            Optional.ofNullable(testPlan.getExecutionScheduledDate())
                .map(formatter::format)
                .orElse(null))
        .executionExpiryDate(
            Optional.ofNullable(testPlan.getExecutionExpiryDate())
                .map(formatter::format)
                .orElse(null))
        .executionFrequency(testPlan.getExecutionFrequency())
        .numberOfTestCases(Optional.ofNullable(testPlan.getTestCases()).map(List::size).orElse(0))
        .build();
  }

  public void testPlanUpdateRequestDtoToTestPlan(
      final TestPlan existingTestPlan, final TestPlanUpdateRequestDto testPlanUpdateRequestDto) {
    existingTestPlan.setTestPlanName(testPlanUpdateRequestDto.getTestPlanName());
    existingTestPlan.setDescription(testPlanUpdateRequestDto.getDescription());
    existingTestPlan.setExecutionScheduledDate(
        LocalDateTime.parse(testPlanUpdateRequestDto.getExecutionScheduledDate(), formatter));
    existingTestPlan.setExecutionExpiryDate(
        LocalDateTime.parse(testPlanUpdateRequestDto.getExecutionExpiryDate(), formatter));
    existingTestPlan.setExecutionFrequency(
        String.valueOf(
            checkValidityAndGetFrequency(testPlanUpdateRequestDto.getExecutionFrequency())));
  }
}
