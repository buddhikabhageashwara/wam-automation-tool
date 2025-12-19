package wam.automationtool.application.dto.execute;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import wam.automationtool.application.dto.alias.AliasDto;
import wam.automationtool.application.dto.testcasestep.TestCaseStepDto;

@Builder
@Getter
@Setter
public class TestCaseStepExecuteRequestDto {

  private String token;
  private String executionId;
  final TestCaseStepDto testCaseStepDto;
  private List<AliasDto> aliasDtoList;
  private boolean isAgentRequest;
  private String testCaseStepType;
}
