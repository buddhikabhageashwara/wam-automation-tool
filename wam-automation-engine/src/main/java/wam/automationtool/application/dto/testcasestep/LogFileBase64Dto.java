package wam.automationtool.application.dto.testcasestep;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogFileBase64Dto {

    private String handleKey;
    private String fileName;
    private String extension;
    private String tempLogFileLocation;
    private long sizeBytes;
    private String base64;
}
