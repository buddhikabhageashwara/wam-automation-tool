package wam.automationtool.application.dto.cache;

import java.io.Serializable;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import wam.automationtool.application.dto.report.FileDetailsDto;

@Builder
@Getter
@Setter
@Data
public class CacheDataDto implements Serializable {
  private static final long serialVersionUID = 1L; // Add a serialVersionUID for safe serialization

  private FileDetailsDto fileDetailsDto;
}
