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

package wam.automationtool.application.util;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static wam.automationtool.application.config.AppConstant.AuthConstants.TEST_CASE_STEP_EXECUTION_FAIL_CODE;
import static wam.automationtool.application.config.pre.action.seed.TestCaseStepAliasParameterType.ALIAS_PARAMETER_TYPE_AGENT_URL;
import static wam.automationtool.application.config.pre.action.seed.TestCaseStepPreferenceParameterType.TCS_PREFERENCE_PARAMETER_TYPE_WAIT_TIME;

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
            preferenceParameterDtoList, TCS_PREFERENCE_PARAMETER_TYPE_WAIT_TIME.getParameterName());
    if (Objects.isNull(tcsPreferenceParameterValue)) {
      log.debug(
          "Agent URL preference parameter is null. test case step id: {}",
          testCaseStepExecuteRequestDto.getTestCaseStepDto().getId());
      return null;
    }
    final AliasDto extractedAlias =
        AliasManager.extractAlias(tcsPreferenceParameterValue, aliasDtoList);
    final String agentUrl = getAliasParameterValue(extractedAlias, ALIAS_PARAMETER_TYPE_AGENT_URL.getParameterName());
    log.debug(
        "Resolved agent URL. test case step id: {}, agentUrl: {}",
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
    final String testCaseStepId =
        String.valueOf(testCaseStepExecuteRequestDto.getTestCaseStepDto().getId());
    try {
      if (Objects.isNull(remoteURL) || remoteURL.isBlank()) {
        log.error("Remote URL is null/blank. test case step id: {}", testCaseStepId);
        throw new IllegalArgumentException("remoteURL cannot be null/blank");
      }
      // Ensure final target URL includes /{testCaseStepId}
      final String finalUrl = buildFinalUrl(remoteURL, testCaseStepId);
      final String origin = extractOrigin(finalUrl);
      log.info(
          "Submitting test case step to agent. test case step id: {}, url: {}, origin: {}",
          testCaseStepId,
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
      final TestCaseStepExecuteResponseDto testCaseStepExecuteResponseDto =
          webClient
              .post()
              .bodyValue(testCaseStepExecuteRequestDto)
              .retrieve()
              .bodyToMono(TestCaseStepExecuteResponseDto.class)
              .doOnSubscribe(s -> log.debug("Agent request started. test case step id: {}",
                      testCaseStepId))
              .doOnSuccess(r -> log.info("Agent request succeeded. test case step id: {}",
                      testCaseStepId))
              .doOnError(
                  e -> log.error("Agent request failed. test case step id: {}, url: {}",
                          testCaseStepId, finalUrl, e))
              .block();
      return testCaseStepExecuteResponseDto;
    } catch (final WebClientResponseException webClientResponseException) {
      log.error(
          "Agent responded with error. test case step id: {}, status: {}, responseBody: {}",
          testCaseStepId,
          webClientResponseException.getStatusCode(),
          webClientResponseException.getResponseBodyAsString(),
          webClientResponseException);
      throw new TestCaseStepExecutionFailException(
          BAD_REQUEST,
          TEST_CASE_STEP_EXECUTION_FAIL_CODE,
          webClientResponseException.getResponseBodyAsString());
    } catch (final Exception exception) {
      log.error(
          "Unexpected error while submitting to agent. test case step id: {}, remoteURL: {}",
          testCaseStepId,
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
      log.error("Invalid remoteURL for Origin extraction. remoteURL: {}", remoteURL);
      throw new IllegalArgumentException(
          "remoteURL must include a valid host (and optionally scheme). Provided: " + remoteURL);
    }
    final String origin =
        (port == -1) ? (scheme + "://" + host) : (scheme + "://" + host + ":" + port);
    log.debug("Extracted Origin from remoteURL. remoteURL: {}, origin: {}", remoteURL, origin);
    return origin;
  }
}
