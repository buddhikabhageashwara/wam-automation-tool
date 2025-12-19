package wam.automationtool.application.util;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static wam.automationtool.application.config.AppConstant.AuthConstants.TEST_CASE_STEP_EXECUTION_FAIL_CODE;
import static wam.automationtool.application.config.AppConstant.TestCaseStepTypeConstant.AGENT_URL;

import java.util.List;
import java.util.Objects;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import wam.automationtool.application.dto.alias.AliasDto;
import wam.automationtool.application.dto.alias.AliasParameterDto;
import wam.automationtool.application.dto.execute.TestCaseStepExecuteRequestDto;
import wam.automationtool.application.dto.execute.TestCaseStepExecuteResponseDto;
import wam.automationtool.application.dto.parameter.PreferenceParameterDto;
import wam.automationtool.application.exception.TestCaseStepExecutionFailException;

public class AgentRequestManager {

  public static String getAgentURL(
      final TestCaseStepExecuteRequestDto testCaseStepExecuteRequestDto) {
    final List<PreferenceParameterDto> preferenceParameterDtoList =
        testCaseStepExecuteRequestDto.getTestCaseStepDto().getPreferenceParameterDtoList();
    final List<AliasDto> aliasDtoList = testCaseStepExecuteRequestDto.getAliasDtoList();
    final String parameterValue = getParameterValueByName(preferenceParameterDtoList, AGENT_URL);
    if (Objects.isNull(parameterValue)) {
      return null;
    }
    final List<String> extractedAliasList = AliasManager.extractAlias(parameterValue, aliasDtoList);
    return getAliasParameterValue(aliasDtoList, extractedAliasList, AGENT_URL);
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
      final List<AliasDto> aliasDtoList,
      final List<String> extractedAliasList,
      final String targetParameterName) {
    return aliasDtoList.stream()
        .filter(aliasDto -> extractedAliasList.contains(aliasDto.getAliasName()))
        .map(AliasDto::getAliasParameterDtoList)
        .filter(Objects::nonNull)
        .flatMap(List::stream)
        .filter(
            aliasParameterDto -> targetParameterName.equals(aliasParameterDto.getParameterName()))
        .map(AliasParameterDto::getParameterValue)
        .findFirst()
        .orElse(null);
  }

  public static TestCaseStepExecuteResponseDto submitToAgent(
      final TestCaseStepExecuteRequestDto testCaseStepExecuteRequestDto, final String remoteURL) {
    try {
      testCaseStepExecuteRequestDto.setAgentRequest(true);
      final WebClient webClient =
          WebClient.builder()
              .baseUrl(remoteURL)
              .defaultHeader("Authorization", "Bearer " + testCaseStepExecuteRequestDto.getToken())
              .build();
      final TestCaseStepExecuteResponseDto testCaseStepExecuteResponseDto =
          webClient
              .post()
              .body(Mono.just(testCaseStepExecuteRequestDto), TestCaseStepExecuteRequestDto.class)
              .retrieve()
              .bodyToMono(TestCaseStepExecuteResponseDto.class)
              .block();
      return testCaseStepExecuteResponseDto;
    } catch (final WebClientResponseException webClientResponseException) {
      throw new TestCaseStepExecutionFailException(
          BAD_REQUEST,
          TEST_CASE_STEP_EXECUTION_FAIL_CODE,
          webClientResponseException.getResponseBodyAsString());
    } catch (final Exception exception) {
      throw new TestCaseStepExecutionFailException(
          BAD_REQUEST, TEST_CASE_STEP_EXECUTION_FAIL_CODE, exception.getMessage());
    }
  }
}
