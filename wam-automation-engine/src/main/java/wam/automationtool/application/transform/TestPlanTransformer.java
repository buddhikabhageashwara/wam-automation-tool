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
