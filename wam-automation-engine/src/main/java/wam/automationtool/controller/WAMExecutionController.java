package wam.automationtool.controller;

import static wam.automationtool.application.config.AppConstant.WAM_AUTOMATION_BASE_PATH;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import wam.automationtool.application.dto.execute.TestCaseStepExecuteRequestDto;
import wam.automationtool.application.dto.execute.TestCaseStepExecuteResponseDto;
import wam.automationtool.application.impl.wamexecute.WAMExecutionService;

@RestController
@RequestMapping(WAM_AUTOMATION_BASE_PATH + "executions")
@RequiredArgsConstructor
public final class WAMExecutionController {

  private final WAMExecutionService wamExecutionService;

  @PostMapping("/testplans/{testplan-id}")
  public ResponseEntity<Void> executeByTestPlan(
      @PathVariable("testplan-id") final long testPlanId) {
    wamExecutionService.executeByTestPlan(testPlanId);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/testcases/{testcase-id}")
  public ResponseEntity<Void> executeByTestCase(
      @PathVariable("testcase-id") final long testcaseId) {
    wamExecutionService.executeByTestCase(testcaseId);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/testcasesteps/{testcasestep-id}")
  public ResponseEntity<TestCaseStepExecuteResponseDto> executeByTestCaseStep(
      @PathVariable("testcasestep-id") final long testCaseStepId,
      @RequestBody @Valid final TestCaseStepExecuteRequestDto testCaseStepExecuteRequestDto) {
    return ResponseEntity.ok(
        wamExecutionService.executeByTestCaseStep(testCaseStepId, testCaseStepExecuteRequestDto));
  }
}
