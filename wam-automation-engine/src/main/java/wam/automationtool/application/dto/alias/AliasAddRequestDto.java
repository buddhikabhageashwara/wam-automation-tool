package wam.automationtool.application.dto.alias;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.util.LinkedHashMap;

@Builder
@Getter
@Setter
public class AliasAddRequestDto {

  @NotBlank(message = "error.alias.name.empty")
  private String aliasName;

  private String aliasType;

  private LinkedHashMap<String, String> aliasParameters;
}
