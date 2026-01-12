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
