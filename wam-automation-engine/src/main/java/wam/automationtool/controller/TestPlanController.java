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
import wam.automationtool.application.dto.testplan.TestPlanAddRequestDto;
import wam.automationtool.application.dto.testplan.TestPlanResponseDto;
import wam.automationtool.application.dto.testplan.TestPlanUpdateRequestDto;
import wam.automationtool.application.dto.testplan.TestPlansResponseDto;
import wam.automationtool.application.impl.testplan.TestPlanService;

@RestController
@RequestMapping(WAM_AUTOMATION_BASE_PATH + "testplans")
@RequiredArgsConstructor
public final class TestPlanController {

  private final TestPlanService testPlanService;

  @PostMapping
  public ResponseEntity<Void> addTestPlan(
      @RequestBody @Valid final TestPlanAddRequestDto testPlanAddRequestDto) {
    testPlanService.addTestPlan(testPlanAddRequestDto);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @GetMapping
  public ResponseEntity<TestPlansResponseDto> getTestPlans() {
    return ResponseEntity.ok(testPlanService.getTestPlans());
  }

  @GetMapping("/{testplan-id}")
  public ResponseEntity<TestPlanResponseDto> getTestPlan(
      @PathVariable("testplan-id") long testPlanId) {
    return ResponseEntity.ok(testPlanService.getTestPlan(testPlanId));
  }

  @PutMapping("/{testplan-id}")
  public ResponseEntity<Void> updateTestPlan(
      @PathVariable("testplan-id") long testPlanId,
      @RequestBody @Valid final TestPlanUpdateRequestDto testPlanUpdateRequestDto) {
    testPlanService.updateTestPlan(testPlanId, testPlanUpdateRequestDto);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{testplan-id}")
  public ResponseEntity<Void> deleteTestPlan(@PathVariable("testplan-id") long testPlanId) {
    testPlanService.deleteTestPlan(testPlanId);
    return ResponseEntity.noContent().build();
  }
}
