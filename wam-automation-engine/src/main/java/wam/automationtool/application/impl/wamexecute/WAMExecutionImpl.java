package wam.automationtool.application.impl.wamexecute;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static wam.automationtool.application.config.AppConstant.AuthConstants.TEST_CASE_NOT_FOUND_CODE;
import static wam.automationtool.application.config.AppConstant.AuthConstants.TEST_PLAN_NOT_FOUND_CODE;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import wam.automationtool.application.config.TestCaseStepExecutorFactory;
import wam.automationtool.application.dto.JWTTokenDto;
import wam.automationtool.application.dto.WAMAutomationUserDetailsDto;
import wam.automationtool.application.dto.alias.AliasDto;
import wam.automationtool.application.dto.cache.CacheDataDto;
import wam.automationtool.application.dto.execute.TestCaseStepExecuteRequestDto;
import wam.automationtool.application.dto.execute.TestCaseStepExecuteResponseDto;
import wam.automationtool.application.dto.report.FileDetailsDto;
import wam.automationtool.application.dto.report.TestCaseExecutionSummaryDto;
import wam.automationtool.application.dto.report.TestCaseStepExecutionDto;
import wam.automationtool.application.dto.report.TestCaseStepExecutionSummaryDto;
import wam.automationtool.application.dto.testcase.TestCaseDto;
import wam.automationtool.application.dto.testcasestep.TestCaseStepDto;
import wam.automationtool.application.dto.testplan.TestPlanDto;
import wam.automationtool.application.exception.TestCaseNotFoundException;
import wam.automationtool.application.exception.TestPlanNotFoundException;
import wam.automationtool.application.impl.testcasestep.execute.TestCaseStepExecutor;
import wam.automationtool.application.transform.AliasTransformer;
import wam.automationtool.application.transform.TestCaseStepTransformer;
import wam.automationtool.application.transform.TestCaseTransformer;
import wam.automationtool.application.transform.TestPlanTransformer;
import wam.automationtool.application.util.ReportGeneratorUtil;
import wam.automationtool.application.util.WAMAutomationJWTTokenUtil;
import wam.automationtool.application.util.WAMCacheManager;
import wam.automationtool.domain.entity.testcase.TestCase;
import wam.automationtool.domain.entity.testcasestep.TestCaseStep;
import wam.automationtool.domain.entity.testcasestep.TestCaseStepExecutionStatus;
import wam.automationtool.domain.entity.testcasestep.TestCaseStepType;
import wam.automationtool.domain.entity.testcasestep.alias.Alias;
import wam.automationtool.domain.entity.testplan.TestPlan;
import wam.automationtool.domain.service.AliasDomainService;
import wam.automationtool.domain.service.TestCaseDomainService;
import wam.automationtool.domain.service.TestCaseStepDomainService;
import wam.automationtool.domain.service.TestPlanDomainService;
import wam.automationtool.infrastructure.provider.AuthDetailsProvider;

@Service
@Slf4j
@RequiredArgsConstructor
public class WAMExecutionImpl extends AuthDetailsProvider implements WAMExecutionService {

  private final TestPlanDomainService testPlanDomainService;
  private final TestCaseDomainService testCaseDomainService;
  private final TestCaseStepDomainService testCaseStepDomainService;
  private final AliasDomainService aliasDomainService;
  private final TestPlanTransformer testPlanTransformer;
  private final TestCaseTransformer testCaseTransformer;
  private final TestCaseStepTransformer testCaseStepTransformer;
  private final AliasTransformer aliasTransformer;
  private final TestCaseStepExecutorFactory testCaseStepExecutorFactory;
  private final WAMAutomationJWTTokenUtil wamAutomationJWTTokenUtil;
  private final WAMCacheManager wamCacheManager;

  @Value("${report.base.path}")
  private String reportBasePath;

  @Value("${report.resource.path}")
  private String reportResourcePath;

  @Override
  public void executeByTestPlan(final long testPlanId) {
    final String executionId = String.valueOf(UUID.randomUUID());
    final TestPlan testPlan =
        testPlanDomainService
            .findById(testPlanId)
            .orElseThrow(
                () ->
                    new TestPlanNotFoundException(
                        NOT_FOUND, TEST_PLAN_NOT_FOUND_CODE, "error.test.plan.not.found"));
    final WAMAutomationUserDetailsDto userDetails = getWAMAutomationUserDetails();
    final JWTTokenDto jwtTokenDto =
            wamAutomationJWTTokenUtil.regenerateTokenWithNewTTL(userDetails.getToken());
    final Thread testCaseExecutionThread =
        new Thread(
            () -> {
              try {
                final List<TestCase> testCaseList = testCaseDomainService.findByTestPlan(testPlan);
                final String reportFilePath =
                    initiateReportGeneration(executionId, testPlan.getId(), wamCacheManager);
                final List<AliasDto> aliasDtoList = getAliasDtoList();
                testCaseList.stream()
                    .forEach(
                        testCase -> {
                          addTestCaseDetailsToReport(reportFilePath, testCase);
                          reportGenerationAndExecuteTestCaseSteps(
                              reportFilePath, testCase, aliasDtoList, executionId, jwtTokenDto.getToken());
                        });
                completeReportGeneration(testCaseList.size(), reportFilePath);
              } finally {
                wamCacheManager.removeItemFromCache(executionId);
              }
            },
            executionId);
    testCaseExecutionThread.start();
  }

  @Override
  public void executeByTestCase(final long testCaseId) {
    final String executionId = String.valueOf(UUID.randomUUID());
    final TestCase testCase =
        testCaseDomainService
            .findById(testCaseId)
            .orElseThrow(
                () ->
                    new TestCaseNotFoundException(
                        NOT_FOUND, TEST_CASE_NOT_FOUND_CODE, "error.test.case.not.found"));
    final WAMAutomationUserDetailsDto userDetails = getWAMAutomationUserDetails();
    final JWTTokenDto jwtTokenDto =
            wamAutomationJWTTokenUtil.regenerateTokenWithNewTTL(userDetails.getToken());
    final Thread testCaseExecutionThread =
        new Thread(
            () -> {
              try {
                final List<AliasDto> aliasDtoList = getAliasDtoList();
                final String reportFilePath =
                    initiateReportGeneration(executionId, testCase.getTestPlan().getId(), wamCacheManager);
                addTestCaseDetailsToReport(reportFilePath, testCase);
                reportGenerationAndExecuteTestCaseSteps(
                    reportFilePath, testCase, aliasDtoList, executionId, jwtTokenDto.getToken());
                completeReportGeneration(1, reportFilePath);
              } finally {
                wamCacheManager.removeItemFromCache(executionId);
              }
            },
            executionId);
    testCaseExecutionThread.start();
  }

  @Override
  public TestCaseStepExecuteResponseDto executeByTestCaseStep(
      final long testCaseStepId,
      final TestCaseStepExecuteRequestDto testCaseStepExecuteRequestDto) {
    final TestCaseStepExecutor testCaseStepExecutor =
        testCaseStepExecutorFactory.getTestCaseStepExecutor(
            TestCaseStepType.valueOf(testCaseStepExecuteRequestDto.getTestCaseStepType()));
    final TestCaseStepExecuteResponseDto testCaseStepExecuteResponseDto =
        testCaseStepExecutor.execute(testCaseStepExecuteRequestDto);
    return testCaseStepExecuteResponseDto;
  }

  private List<AliasDto> getAliasDtoList() {
    final List<Alias> aliasList = aliasDomainService.findAll();
    final List<AliasDto> aliasDtoList = aliasTransformer.aliasListToAliasDtoList(aliasList);
    return aliasDtoList;
  }

  private void addTestCaseDetailsToReport(final String reportFilePath, final TestCase testCase) {
    final TestCaseDto testCaseDto = testCaseTransformer.testCaseToTestCaseDto(testCase);
    ReportGeneratorUtil.appendTestCaseDetails(reportFilePath, testCaseDto);
  }

  private void reportGenerationAndExecuteTestCaseSteps(
      final String reportFilePath,
      final TestCase testCase,
      final List<AliasDto> aliasDtoList,
      final String executionId,
      final String token) {
    ReportGeneratorUtil.createTestCaseStepDetailsTable(reportFilePath);
    final List<TestCaseStep> testCaseStepList = testCaseStepDomainService.findByTestCase(testCase);
    final int totalTestCaseSteps = testCaseStepList.size();
    final AtomicInteger passedTestCaseSteps = new AtomicInteger();
    final AtomicInteger failedTestCaseSteps = new AtomicInteger();
    testCaseStepList.stream()
        .forEach(
            testCaseStep -> {
              executeTestCaseStep(testCaseStep, executionId, aliasDtoList,
                      passedTestCaseSteps, failedTestCaseSteps, reportFilePath, token);
            });
    completeTestCaseStepReportingDetails(
        reportFilePath, totalTestCaseSteps, passedTestCaseSteps, failedTestCaseSteps, executionId);
  }

  private void executeTestCaseStep(final TestCaseStep testCaseStep, final String executionId,
                                   final List<AliasDto> aliasDtoList,
                                   final AtomicInteger passedTestCaseSteps,
                                   final AtomicInteger failedTestCaseSteps,
                                   final String reportFilePath,
                                   final String token) {
    final TestCaseStepExecutor testCaseStepExecutor =
            testCaseStepExecutorFactory.getTestCaseStepExecutor(
                    TestCaseStepType.valueOf(testCaseStep.getTestCaseStepType()));
    final TestCaseStepDto testCaseStepDto =
            testCaseStepTransformer.transformTestCaseStepToDto(testCaseStep);
    final TestCaseStepExecuteRequestDto testCaseStepExecuteRequestDto =
            testCaseStepTransformer.toTestCaseStepExecuteRequestDto(
                    aliasDtoList, testCaseStepDto, executionId, token);
    final TestCaseStepExecuteResponseDto testCaseStepExecuteResponseDto =
            testCaseStepExecutor.execute(testCaseStepExecuteRequestDto);
    final TestCaseStepExecutionDto testCaseStepExecutionDto =
            testCaseStepTransformer.testCaseStepExecuteResponseDtoToTestCaseStepExecutionDto(
                    testCaseStepExecuteResponseDto, testCaseStep);
    calculateTestCaseStepExecutionResults(
            passedTestCaseSteps,
            failedTestCaseSteps,
            testCaseStepExecuteResponseDto.getStatus());
    ReportGeneratorUtil.appendTestCaseStepDetails(
            reportFilePath, testCaseStepExecutionDto);
  }

  private void calculateTestCaseStepExecutionResults(
      final AtomicInteger passedTestCaseSteps,
      final AtomicInteger failedTestCaseSteps,
      final String status) {
    if (TestCaseStepExecutionStatus.PASSED.toString().equals(status)) {
      passedTestCaseSteps.addAndGet(1);
    } else {
      failedTestCaseSteps.addAndGet(1);
    }
  }

  private void completeTestCaseStepReportingDetails(
      final String reportFilePath,
      final int totalTestCaseSteps,
      final AtomicInteger passedTestCaseSteps,
      final AtomicInteger failedTestCaseSteps,
      final String executionId) {
    ReportGeneratorUtil.endTestCaseStepDetailsTable(reportFilePath);
    final TestCaseStepExecutionSummaryDto testCaseStepExecutionSummaryDto =
        TestCaseStepExecutionSummaryDto.builder()
            .totalTestCaseSteps(totalTestCaseSteps)
            .passedTestCaseSteps(passedTestCaseSteps.get())
            .failedTestCaseSteps(failedTestCaseSteps.get())
            .build();
    ReportGeneratorUtil.appendTestCaseStepExecutionSummary(
        reportFilePath, testCaseStepExecutionSummaryDto);
    final CacheDataDto cacheDataDto = wamCacheManager.getCacheDataDto(executionId);
    final FileDetailsDto fileDetailsDto =
        Objects.isNull(cacheDataDto)
            ? FileDetailsDto.builder().build()
            : cacheDataDto.getFileDetailsDto();
    ReportGeneratorUtil.appendFileDetails(reportFilePath, fileDetailsDto);
  }

  private String initiateReportGeneration(final String executionId, final long testPlanId,
                                          final WAMCacheManager wamCacheManager) {
    final TestPlan testPlan = testPlanDomainService.findById(testPlanId).get();
    final TestPlanDto testPlanDto = testPlanTransformer.testPlanToTestPlanDto(testPlan);
    final String reportFilePath =
        ReportGeneratorUtil.initiateReportGeneration(
            executionId, reportBasePath, reportResourcePath, wamCacheManager);
    ReportGeneratorUtil.appendTestPlanDetails(reportFilePath, testPlanDto);
    return reportFilePath;
  }

  private void completeReportGeneration(final int numberOfTestCases, final String reportFilePath) {
    final TestCaseExecutionSummaryDto testCaseExecutionSummaryDto =
        testCaseTransformer.toTestCaseExecutionSummaryDto(numberOfTestCases, 0, 0);
    ReportGeneratorUtil.appendTestCaseExecutionSummary(reportFilePath, testCaseExecutionSummaryDto);
    ReportGeneratorUtil.completeReportGeneration(reportFilePath);
  }
}
