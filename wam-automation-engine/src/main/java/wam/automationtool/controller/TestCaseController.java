package wam.automationtool.controller;

import static wam.automationtool.application.config.AppConstant.WAM_AUTOMATION_BASE_PATH;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import wam.automationtool.application.dto.testcase.TestCaseAddRequestDto;
import wam.automationtool.application.dto.testcase.TestCaseResponseDto;
import wam.automationtool.application.dto.testcase.TestCaseUpdateRequestDto;
import wam.automationtool.application.dto.testcase.TestCasesResponseDto;
import wam.automationtool.application.impl.testcase.TestCaseService;

@RestController
@RequestMapping(WAM_AUTOMATION_BASE_PATH + "testcases")
@RequiredArgsConstructor
public final class TestCaseController {

  private final TestCaseService testCaseService;

  @PostMapping
  public ResponseEntity<Void> addTestCase(
      @RequestBody @Valid final TestCaseAddRequestDto testCaseAddRequestDto) {
    testCaseService.addTestCase(testCaseAddRequestDto);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @GetMapping("/testplans/{testplan-id}")
  public ResponseEntity<TestCasesResponseDto> getTestCases(@PathVariable("testplan-id") long testPlanId) {
    return ResponseEntity.ok(testCaseService.getTestCases(testPlanId));
  }

  @GetMapping("/{testcase-id}")
  public ResponseEntity<TestCaseResponseDto> getTestCase(
      @PathVariable("testcase-id") long testCaseId) {
    return ResponseEntity.ok(testCaseService.getTestCase(testCaseId));
  }

  @PutMapping("/{testcase-id}")
  public ResponseEntity<Void> updateTestCase(
      @PathVariable("testcase-id") long testCaseId,
      @RequestBody @Valid final TestCaseUpdateRequestDto testCaseUpdateRequestDto) {
    testCaseService.updateTestCase(testCaseId, testCaseUpdateRequestDto);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{testcase-id}")
  public ResponseEntity<Void> deleteTestCase(@PathVariable("testcase-id") long testCaseId) {
    testCaseService.deleteTestCase(testCaseId);
    return ResponseEntity.noContent().build();
  }
}
