package wam.automationtool.application.exception;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;

/** Custom response dto for exceptions. */
@Data
@AllArgsConstructor
public class ExceptionResponse {
  private Date timestamp;
  private String message;
  private String details;
}
