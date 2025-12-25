package wam.automationtool.application.util;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static wam.automationtool.application.config.AppConstant.AliasParameterTypeConstant.ALIAS_PARAMETER_TYPE_AGENT_URL;
import static wam.automationtool.application.config.AppConstant.AuthConstants.TEST_CASE_STEP_EXECUTION_FAIL_CODE;
import static wam.automationtool.application.config.AppConstant.TestCaseStepPreferenceParameterTypeConstant.TCS_PREFERENCE_PARAMETER_TYPE_AGENT_URL;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import wam.automationtool.application.dto.alias.AliasDto;
import wam.automationtool.application.dto.alias.AliasParameterDto;
import wam.automationtool.application.dto.execute.TestCaseStepExecuteRequestDto;
import wam.automationtool.application.dto.execute.TestCaseStepExecuteResponseDto;
import wam.automationtool.application.dto.parameter.PreferenceParameterDto;
import wam.automationtool.application.exception.TestCaseStepExecutionFailException;

@Slf4j
public class AgentRequestManager {

  public static String getAgentURL(
      final TestCaseStepExecuteRequestDto testCaseStepExecuteRequestDto) {
    final List<PreferenceParameterDto> preferenceParameterDtoList =
        testCaseStepExecuteRequestDto.getTestCaseStepDto().getPreferenceParameterDtoList();
    final List<AliasDto> aliasDtoList = testCaseStepExecuteRequestDto.getAliasDtoList();
    final String tcsPreferenceParameterValue =
        getParameterValueByName(
            preferenceParameterDtoList, TCS_PREFERENCE_PARAMETER_TYPE_AGENT_URL);
    if (Objects.isNull(tcsPreferenceParameterValue)) {
      log.debug(
          "Agent URL preference parameter is null. stepId={}",
          testCaseStepExecuteRequestDto.getTestCaseStepDto().getId());
      return null;
    }
    final AliasDto extractedAlias =
        AliasManager.extractAlias(tcsPreferenceParameterValue, aliasDtoList);
    final String agentUrl = getAliasParameterValue(extractedAlias, ALIAS_PARAMETER_TYPE_AGENT_URL);
    log.debug(
        "Resolved agent URL. stepId={}, agentUrl={}",
        testCaseStepExecuteRequestDto.getTestCaseStepDto().getId(),
        agentUrl);
    return agentUrl;
  }

  private static String getParameterValueByName(
      final List<PreferenceParameterDto> preferenceParameterDtoList, final String parameterName) {
    return preferenceParameterDtoList.stream()
        .filter(preference -> parameterName.equals(preference.getParameterName()))
        .map(PreferenceParameterDto::getParameterValue)
        .findFirst()
        .orElse(null);
  }

  private static String getAliasParameterValue(
      final AliasDto extractedAlias, final String aliasParameterType) {
    return extractedAlias.getAliasParameterDtoList().stream()
        .filter(
            aliasParameterDto -> aliasParameterType.equals(aliasParameterDto.getParameterName()))
        .map(AliasParameterDto::getParameterValue)
        .findFirst()
        .orElse(null);
  }

  public static TestCaseStepExecuteResponseDto submitToAgent(
      final TestCaseStepExecuteRequestDto testCaseStepExecuteRequestDto, String remoteURL) {
    final String stepId =
        String.valueOf(testCaseStepExecuteRequestDto.getTestCaseStepDto().getId());
    try {
      if (Objects.isNull(remoteURL) || remoteURL.isBlank()) {
        log.error("Remote URL is null/blank. stepId={}", stepId);
        throw new IllegalArgumentException("remoteURL cannot be null/blank");
      }
      // Ensure final target URL includes /{stepId}
      final String finalUrl = buildFinalUrl(remoteURL, stepId);
      final String origin = extractOrigin(finalUrl);
      log.info(
          "Submitting test case step to agent. stepId={}, url={}, origin={}",
          stepId,
          finalUrl,
          origin);
      testCaseStepExecuteRequestDto.setAgentRequest(true);
      final WebClient webClient =
          WebClient.builder()
              .baseUrl(finalUrl)
              .defaultHeader(HttpHeaders.ORIGIN, origin)
              .defaultHeader(
                  HttpHeaders.AUTHORIZATION, "Bearer " + testCaseStepExecuteRequestDto.getToken())
              .build();
      final TestCaseStepExecuteResponseDto response =
          webClient
              .post()
              .bodyValue(testCaseStepExecuteRequestDto)
              .retrieve()
              .bodyToMono(TestCaseStepExecuteResponseDto.class)
              .doOnSubscribe(s -> log.debug("Agent request started. stepId={}", stepId))
              .doOnSuccess(r -> log.info("Agent request succeeded. stepId={}", stepId))
              .doOnError(
                  e -> log.error("Agent request failed. stepId={}, url={}", stepId, finalUrl, e))
              .block();
      return response;
    } catch (final WebClientResponseException webClientResponseException) {
      log.error(
          "Agent responded with error. stepId={}, status={}, responseBody={}",
          stepId,
          webClientResponseException.getStatusCode(),
          webClientResponseException.getResponseBodyAsString(),
          webClientResponseException);
      throw new TestCaseStepExecutionFailException(
          BAD_REQUEST,
          TEST_CASE_STEP_EXECUTION_FAIL_CODE,
          webClientResponseException.getResponseBodyAsString());
    } catch (final Exception exception) {
      log.error(
          "Unexpected error while submitting to agent. stepId={}, remoteURL={}",
          stepId,
          remoteURL,
          exception);
      throw new TestCaseStepExecutionFailException(
          BAD_REQUEST, TEST_CASE_STEP_EXECUTION_FAIL_CODE, exception.getMessage());
    }
  }

  private static String buildFinalUrl(final String remoteURL, final String stepId) {
    final String trimmed = remoteURL.trim();
    final String withoutTrailingSlash =
        trimmed.endsWith("/") ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
    return withoutTrailingSlash + "/" + stepId;
  }

  /**
   * Builds the Origin header value from a URL. - Returns: scheme://host or scheme://host:port -
   * Supports inputs without scheme by assuming https (you can change to http if needed).
   */
  private static String extractOrigin(final String remoteURL) {
    String url = remoteURL.trim();
    // If scheme is missing, assume https (adjust if your agents are http).
    if (!url.startsWith("http://") && !url.startsWith("https://")) {
      url = "https://" + url;
    }
    final URI uri = URI.create(url);
    final String scheme = uri.getScheme();
    final String host = uri.getHost();
    final int port = uri.getPort();
    if (Objects.isNull(scheme) || Objects.isNull(host)) {
      log.error("Invalid remoteURL for Origin extraction. remoteURL={}", remoteURL);
      throw new IllegalArgumentException(
          "remoteURL must include a valid host (and optionally scheme). Provided: " + remoteURL);
    }
    final String origin =
        (port == -1) ? (scheme + "://" + host) : (scheme + "://" + host + ":" + port);
    log.debug("Extracted Origin from remoteURL. remoteURL={}, origin={}", remoteURL, origin);
    return origin;
  }
}
