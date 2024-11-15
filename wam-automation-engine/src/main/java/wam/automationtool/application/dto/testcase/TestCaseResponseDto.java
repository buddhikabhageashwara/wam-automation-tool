package wam.automationtool.application.dto.testcase;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class TestCaseResponseDto {

  private TestCaseDto  testCaseDto;
}
