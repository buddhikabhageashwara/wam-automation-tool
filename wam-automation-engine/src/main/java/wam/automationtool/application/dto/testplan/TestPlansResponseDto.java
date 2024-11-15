package wam.automationtool.application.dto.testplan;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class TestPlansResponseDto {

  private List<TestPlanDto>  testPlanDtoList;
}
