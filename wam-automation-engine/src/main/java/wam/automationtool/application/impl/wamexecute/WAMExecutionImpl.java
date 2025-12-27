package wam.automationtool.application.impl.wamexecute;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static wam.automationtool.application.config.AppConstant.AuthConstants.TEST_CASE_NOT_FOUND_CODE;
import static wam.automationtool.application.config.AppConstant.AuthConstants.TEST_PLAN_NOT_FOUND_CODE;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Base64;
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
import wam.automationtool.application.dto.testcasestep.LogFileBase64Dto;
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

    private static boolean isBlank(final String value) {
        return Objects.isNull(value) || value.isBlank();
    }

    /**
     * Removes a possible "data:...;base64," prefix if FE sends a Data URL.
     */
    private static String stripDataUrlPrefix(final String base64) {
        final int commaIndex = base64.indexOf(',');
        if (base64.startsWith("data:") && commaIndex > -1) {
            return base64.substring(commaIndex + 1);
        }
        return base64;
    }

    /**
     * Prevents path traversal and illegal filename characters across OSes.
     */
    private static String sanitizeFileName(final String fileName) {
        // remove any path parts
        String name = fileName.replace("\\", "/");
        final int lastSlash = name.lastIndexOf('/');
        if (lastSlash >= 0) {
            name = name.substring(lastSlash + 1);
        }
        // replace illegal/suspicious chars
        name = name.replaceAll("[\\\\/:*?\"<>|]+", "_").trim();
        if (name.isBlank()) {
            return "log.txt";
        }
        return name;
    }

    private static String safeMsg(final Throwable throwable) {
        final String message = throwable.getMessage();
        return (Objects.isNull(message) || message.isBlank())
                ? throwable.getClass().getSimpleName()
                : message;
    }

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
                  final CacheDataDto cacheDataDto = wamCacheManager.getCacheDataDto(executionId);
                  final FileDetailsDto fileDetailsDto =
                          Objects.isNull(cacheDataDto)
                                  ? FileDetailsDto.builder().build()
                                  : cacheDataDto.getFileDetailsDto();
                  ReportGeneratorUtil.appendFileDetails(reportFilePath, fileDetailsDto);
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
                  final CacheDataDto cacheDataDto = wamCacheManager.getCacheDataDto(executionId);
                  final FileDetailsDto fileDetailsDto =
                          Objects.isNull(cacheDataDto)
                                  ? FileDetailsDto.builder().build()
                                  : cacheDataDto.getFileDetailsDto();
                  ReportGeneratorUtil.appendFileDetails(reportFilePath, fileDetailsDto);
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
        reportFilePath, totalTestCaseSteps, passedTestCaseSteps, failedTestCaseSteps);
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
      persistLogFileToCacheIfExists(executionId, testCaseStepExecuteResponseDto, testCaseStepDto.getId());
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

    /**
     * Persists the log file (if available as base64 in the step testCaseStepExecuteResponseDto) into the report logs folder,
     * and updates execution cache with the created log file name.
     *
     * <p>Flow:
     * <ul>
     *   <li>Checks whether testCaseStepExecuteResponseDto contains {@code logFileBase64Dto}.</li>
     *   <li>Loads cache data using {@code executionId}.</li>
     *   <li>Creates the log file under {@code reportLogsFolderPath}.</li>
     *   <li>Updates {@code cacheDataDto.fileDetailsDto.fileNameList} with the created file name.</li>
     * </ul>
     *
     * <p>Notes:
     * <ul>
     *   <li>This method does not throw exceptions intentionally; any failure is logged and flow continues.</li>
     *   <li>Uses a defensive copy for the list to avoid issues with unmodifiable lists.</li>
     * </ul>
     *
     * @param executionId execution identifier used for cache access
     * @param testCaseStepExecuteResponseDto test case step execution testCaseStepExecuteResponseDto
     */
    private void persistLogFileToCacheIfExists(final String executionId,
                                               final TestCaseStepExecuteResponseDto testCaseStepExecuteResponseDto,
                                               final long testCaseStepId) {
        try {
            if (Objects.isNull(testCaseStepExecuteResponseDto) ||
                    Objects.isNull(testCaseStepExecuteResponseDto.getLogFileBase64Dto())) {
                log.debug("log file base64 dto not found: executionId: {}, tcs id: {}",
                        testCaseStepExecuteResponseDto, testCaseStepId);
                return;
            }
            final CacheDataDto cacheDataDto = wamCacheManager.getCacheDataDto(executionId);
            if (Objects.isNull(cacheDataDto)) {
                log.warn("cache data not found: executionId: {}", executionId);
                return;
            }
            final String reportLogsFolderPath = cacheDataDto.getReportLogsFolderPath();
            if (Objects.isNull(reportLogsFolderPath) || reportLogsFolderPath.isBlank()) {
                log.warn("report logs folder path not found: executionId: {}", executionId);
                return;
            }
            final String createdLogFileName = createFileFromBase64(
                    reportLogsFolderPath,
                    testCaseStepExecuteResponseDto.getLogFileBase64Dto());
            if (Objects.isNull(createdLogFileName) || createdLogFileName.isBlank()) {
                log.warn("log file creation returned empty result: executionId: {}", executionId);
                return;
            }
            FileDetailsDto fileDetailsDto = cacheDataDto.getFileDetailsDto();
            if (Objects.isNull(fileDetailsDto)) {
                fileDetailsDto = FileDetailsDto.builder().build();
            }
            final List<String> existingList = fileDetailsDto.getFileNameList();
            final List<String> updatedList = Objects.nonNull(existingList)
                    ? new ArrayList<>(existingList)
                    : new ArrayList<>();
            updatedList.add(createdLogFileName);
            fileDetailsDto.setFileNameList(updatedList);
            cacheDataDto.setFileDetailsDto(fileDetailsDto);
            wamCacheManager.addToCache(executionId, cacheDataDto);
            log.info("log file persisted to cache: executionId: {}, logFile: {}", executionId, createdLogFileName);
        } catch (final Exception exception) {
            log.error("failed to persist log file to cache: executionId: {}, reason: {}",
                    executionId, exception.getMessage());
        }
    }

    /**
     * Creates a file from the given {@code logFileBase64Dto} under {@code targetFolderLocation}.
     *
     * <p>Behavior:
     * <ul>
     *   <li>Supports Windows and Linux paths via {@link java.nio.file.Path}.</li>
     *   <li>Creates the folder if it does not exist.</li>
     *   <li>Overwrites the file if it already exists.</li>
     *   <li>Does not throw exceptions; logs errors and returns null on failure.</li>
     * </ul>
     *
     * @param targetFolderLocation folder path where the file should be created
     * @param logFileBase64Dto dto containing base64 content and fileName
     * @return absolute file location if created successfully, otherwise null
     */
    public String createFileFromBase64(
            final String targetFolderLocation,
            final LogFileBase64Dto logFileBase64Dto) {
        try {
            if (Objects.isNull(logFileBase64Dto)) {
                log.warn("log file create skipped: reason: dto is null");
                return null;
            }
            if (isBlank(targetFolderLocation)) {
                log.warn("log file create skipped: reason: targetFolderLocation is blank");
                return null;
            }
            if (isBlank(logFileBase64Dto.getFileName())) {
                log.warn("log file create skipped: reason: fileName is blank");
                return null;
            }
            if (isBlank(logFileBase64Dto.getBase64())) {
                log.warn("log file create skipped: reason: base64 is blank");
                return null;
            }
            final Path folderPath = Paths.get(targetFolderLocation);
            Files.createDirectories(folderPath);
            final String safeFileName = sanitizeFileName(logFileBase64Dto.getFileName());
            final Path outputFilePath = folderPath.resolve(safeFileName);
            final byte[] bytes = Base64.getDecoder().decode(stripDataUrlPrefix(logFileBase64Dto.getBase64()));
            Files.write(
                    outputFilePath,
                    bytes,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );
            final String createdPath = outputFilePath.toAbsolutePath().toString();
            log.info("log file created successfully: file: {}, sizeBytes: {}",
                    createdPath, bytes.length);
            return createdPath;
        } catch (final Exception exception) {
            log.error("log file create failed: folder: {}, fileName: {}, reason: {}",
                    targetFolderLocation,
                    (logFileBase64Dto == null ? null : logFileBase64Dto.getFileName()),
                    safeMsg(exception));
            return null;
        }
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
      final AtomicInteger failedTestCaseSteps) {
    ReportGeneratorUtil.endTestCaseStepDetailsTable(reportFilePath);
    final TestCaseStepExecutionSummaryDto testCaseStepExecutionSummaryDto =
        TestCaseStepExecutionSummaryDto.builder()
            .totalTestCaseSteps(totalTestCaseSteps)
            .passedTestCaseSteps(passedTestCaseSteps.get())
            .failedTestCaseSteps(failedTestCaseSteps.get())
            .build();
    ReportGeneratorUtil.appendTestCaseStepExecutionSummary(
        reportFilePath, testCaseStepExecutionSummaryDto);
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
