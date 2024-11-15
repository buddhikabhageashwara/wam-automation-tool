package wam.automationtool.application.dto.testcase;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class TestCasesResponseDto {

  private List<TestCaseDto>  testCaseDtoList;
}
