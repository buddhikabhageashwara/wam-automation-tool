package wam.automationtool.application.dto.cache;

import java.io.Serializable;
import java.util.Map;

import lombok.Builder;
import lombok.Data;
import wam.automationtool.application.dto.report.FileDetailsDto;

@Builder
@Data
public class CacheDataDto implements Serializable {
  private static final long serialVersionUID = 1L; // Add a serialVersionUID for safe serialization

  private FileDetailsDto fileDetailsDto;

  private String reportLogsFolderPath;

  private Map<String, String> stringCacheMap;
}
