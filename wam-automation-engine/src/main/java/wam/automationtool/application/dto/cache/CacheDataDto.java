package wam.automationtool.application.dto.cache;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import wam.automationtool.application.dto.report.FileDetailsDto;

@Builder
@Getter
@Setter
public class CacheDataDto {

  private FileDetailsDto fileDetailsDto;
}
