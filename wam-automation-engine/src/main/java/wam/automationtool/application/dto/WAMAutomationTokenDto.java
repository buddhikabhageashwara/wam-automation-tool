package wam.automationtool.application.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Data
@Builder
@Accessors(chain = true)
public class WAMAutomationTokenDto {
  private String firstName;
  private String lastName;
  private String userEmail;
  private String userId;

  @JsonProperty("isSuperAdmin")
  private boolean isSuperAdmin;

  private List<String> permissionTypeList;
}
