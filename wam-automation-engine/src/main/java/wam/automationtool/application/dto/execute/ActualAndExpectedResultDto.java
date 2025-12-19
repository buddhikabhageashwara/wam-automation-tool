package wam.automationtool.application.dto.execute;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class ActualAndExpectedResultDto {

  private String actualResult;
  private String expectedResult;
}
