package wam.automationtool.application.dto.report;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class FileDetailsDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

  private List<String> fileNameList;
}
