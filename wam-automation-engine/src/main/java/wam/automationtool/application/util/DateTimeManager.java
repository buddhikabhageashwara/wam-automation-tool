package wam.automationtool.application.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateTimeManager {

  public static String getCurrentUTCDateTime() {
    final DateTimeFormatter dateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS").withZone(ZoneId.of("UTC"));
    final String currentUTCDateTime = dateTimeFormatter.format(Instant.now());
    return currentUTCDateTime;
  }
}
