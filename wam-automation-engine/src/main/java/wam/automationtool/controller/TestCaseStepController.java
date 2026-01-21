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
import wam.automationtool.application.dto.testcasestep.*;
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

  @PostMapping("/bulk")
  public ResponseEntity<Void> addTestCaseStepsInBulk(
      @RequestBody @Valid final BulkTestCaseStepsAddRequestDto bulkTestCaseStepsAddRequestDto) {
    testCaseStepService.addTestCaseStepsInBulk(bulkTestCaseStepsAddRequestDto);
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
