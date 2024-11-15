package wam.automationtool.application.impl.testplan;

import wam.automationtool.application.dto.testplan.TestPlanAddRequestDto;
import wam.automationtool.application.dto.testplan.TestPlanResponseDto;
import wam.automationtool.application.dto.testplan.TestPlanUpdateRequestDto;
import wam.automationtool.application.dto.testplan.TestPlansResponseDto;

public interface TestPlanService {

  void addTestPlan(TestPlanAddRequestDto testPlanAddRequestDto);

  TestPlansResponseDto getTestPlans();

  TestPlanResponseDto getTestPlan(long testPlanId);

  void updateTestPlan(long testPlanId, TestPlanUpdateRequestDto testPlanUpdateRequestDto);

  void deleteTestPlan(long testPlanId);
}
