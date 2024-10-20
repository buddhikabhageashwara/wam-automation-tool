package wam.automationtool.application.dto.home;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class HomeDetailsResponseDto {

  private List<Map<String, String>> homeDetails;
}
