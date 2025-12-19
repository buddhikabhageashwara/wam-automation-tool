package wam.automationtool.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Data
@Builder
@Accessors(chain = true)
public class WAMAutomationUserDetailsDto {
  private String firstName;
  private String lastName;
  private String userEmail;
  private String userId;
  private String token;
  @JsonProperty("isSuperAdmin")
  private boolean isSuperAdmin;
  private List<String> permissionTypeList;
}
