package wam.automationtool.controller;

import static wam.automationtool.application.config.AppConstant.WAM_AUTOMATION_BASE_PATH;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import wam.automationtool.application.dto.testcasestep.TestCaseStepAddRequestDto;
import wam.automationtool.application.dto.testcasestep.TestCaseStepResponseDto;
import wam.automationtool.application.dto.testcasestep.TestCaseStepUpdateRequestDto;
import wam.automationtool.application.dto.testcasestep.TestCaseStepsResponseDto;
import wam.automationtool.application.impl.testcasestep.TestCaseStepService;

@RestController
@RequestMapping(WAM_AUTOMATION_BASE_PATH + "testcasesteps")
@RequiredArgsConstructor
public final class TestCaseStepController {

  private final TestCaseStepService testCaseStepService;

  @PostMapping
  public ResponseEntity<Void> addTestCaseStep(
      @RequestBody @Valid final TestCaseStepAddRequestDto testCaseStepAddRequestDto) {
    testCaseStepService.addTestCaseStep(testCaseStepAddRequestDto);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @PatchMapping
  public ResponseEntity<Void> updateTestCaseStep(
      @RequestBody @Valid final TestCaseStepUpdateRequestDto testCaseStepUpdateRequestDto) {
    testCaseStepService.updateTestCaseStep(testCaseStepUpdateRequestDto);
    return ResponseEntity.status(HttpStatus.OK).build();
  }

  @GetMapping("/testcases/{testcase-id}")
  public ResponseEntity<TestCaseStepsResponseDto> getTestCaseSteps(
      @PathVariable("testcase-id") long testCaseId) {
    return ResponseEntity.ok(testCaseStepService.getTestCaseSteps(testCaseId));
  }

  @GetMapping("/{testcasestep-id}")
  public ResponseEntity<TestCaseStepResponseDto> getTestCaseStep(
      @PathVariable("testcasestep-id") long testCaseStepId) {
    return ResponseEntity.ok(testCaseStepService.getTestCaseStep(testCaseStepId));
  }

  @DeleteMapping("/{testcasestep-id}")
  public ResponseEntity<Void> deleteTestCaseStep(
      @PathVariable("testcasestep-id") final long testCaseStepId) {
    testCaseStepService.deleteTestCaseStep(testCaseStepId);
    return ResponseEntity.noContent().build();
  }
}
