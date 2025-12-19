package wam.automationtool.application.impl.testplan;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static wam.automationtool.application.config.AppConstant.AuthConstants.TEST_PLAN_ALREADY_EXIST_CODE;
import static wam.automationtool.application.config.AppConstant.AuthConstants.TEST_PLAN_NOT_FOUND_CODE;

import java.util.List;
import java.util.Optional;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import wam.automationtool.application.dto.testplan.TestPlanAddRequestDto;
import wam.automationtool.application.dto.testplan.TestPlanDto;
import wam.automationtool.application.dto.testplan.TestPlanResponseDto;
import wam.automationtool.application.dto.testplan.TestPlanUpdateRequestDto;
import wam.automationtool.application.dto.testplan.TestPlansResponseDto;
import wam.automationtool.application.exception.TestPlanAlreadyExistException;
import wam.automationtool.application.exception.TestPlanNotFoundException;
import wam.automationtool.application.transform.TestPlanTransformer;
import wam.automationtool.domain.entity.testplan.TestPlan;
import wam.automationtool.domain.service.TestPlanDomainService;
import wam.automationtool.infrastructure.provider.AuthDetailsProvider;

@Service
@Slf4j
@RequiredArgsConstructor
public class TestPlanImpl extends AuthDetailsProvider implements TestPlanService {

  private final TestPlanTransformer testPlanTransformer;
  private final TestPlanDomainService testPlanDomainService;

  @Override
  @Transactional
  public void addTestPlan(final TestPlanAddRequestDto testPlanAddRequestDto) {
    final boolean isTestPlanExist =
        testPlanDomainService
            .findByTestPlanName(testPlanAddRequestDto.getTestPlanName())
            .isPresent();
    if (isTestPlanExist) {
      throw new TestPlanAlreadyExistException(
          BAD_REQUEST, TEST_PLAN_ALREADY_EXIST_CODE, "error.test.plan.already.exist");
    }
    final TestPlan testPlan =
        testPlanTransformer.testPlanAddRequestDtoToTestPlan(testPlanAddRequestDto);
    testPlanDomainService.add(testPlan);
  }

  @Override
  public TestPlansResponseDto getTestPlans() {
    final List<TestPlan> testPlanList = testPlanDomainService.findAllByIdDesc();
    final List<TestPlanDto> testPlanDtoList =
        testPlanTransformer.testPlanListToTestPlanDtoList(testPlanList);
    return TestPlansResponseDto.builder().testPlanDtoList(testPlanDtoList).build();
  }

  @Override
  public TestPlanResponseDto getTestPlan(final long testPlanId) {
    TestPlan testPlan =
        testPlanDomainService
            .findById(testPlanId)
            .orElseThrow(
                () ->
                    new TestPlanNotFoundException(
                        NOT_FOUND, TEST_PLAN_NOT_FOUND_CODE, "error.test.plan.not.found"));
    TestPlanDto testPlanDto = testPlanTransformer.testPlanToTestPlanDto(testPlan);
    return TestPlanResponseDto.builder().testPlanDto(testPlanDto).build();
  }

  @Override
  public void updateTestPlan(
      final long testPlanId, final TestPlanUpdateRequestDto testPlanUpdateRequestDto) {
    final TestPlan existingTestPlan =
        testPlanDomainService
            .findById(testPlanId)
            .orElseThrow(
                () ->
                    new TestPlanNotFoundException(
                        NOT_FOUND, TEST_PLAN_NOT_FOUND_CODE, "error.test.plan.not.found"));
    final Optional<TestPlan> existingTestPlanByName =
        testPlanDomainService.findByTestPlanName(testPlanUpdateRequestDto.getTestPlanName());
    final boolean isTestPlanExist = existingTestPlanByName.isPresent();
    final boolean isNotSameTestPlan =
        isTestPlanExist && (existingTestPlan.getId() != existingTestPlanByName.get().getId());
    if (isNotSameTestPlan && isTestPlanExist) {
      throw new TestPlanAlreadyExistException(
          BAD_REQUEST, TEST_PLAN_ALREADY_EXIST_CODE, "error.test.plan.already.exist");
    }
    testPlanTransformer.testPlanUpdateRequestDtoToTestPlan(
        existingTestPlan, testPlanUpdateRequestDto);
    testPlanDomainService.update(existingTestPlan);
  }

  @Override
  public void deleteTestPlan(final long testPlanId) {
    final TestPlan testPlan =
        testPlanDomainService
            .findById(testPlanId)
            .orElseThrow(
                () ->
                    new TestPlanNotFoundException(
                        NOT_FOUND, TEST_PLAN_NOT_FOUND_CODE, "error.test.plan.not.found"));
    testPlanDomainService.delete(testPlan);
  }
}
