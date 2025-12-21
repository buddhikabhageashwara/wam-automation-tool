package wam.automationtool.application.config.seleniumwebdriver;

import lombok.Getter;

@Getter
public class OSInfo {

  private final String osName;

  private OSInfo(final String osName) {
    this.osName = osName == null ? "" : osName.toLowerCase();
  }

  public static OSInfo detect() {
    return new OSInfo(System.getProperty("os.name"));
  }

  public boolean isWindows() {
    return osName.contains("win");
  }

  public boolean isLinux() {
    return osName.contains("linux");
  }

  public boolean isMac() {
    return osName.contains("mac");
  }

  public String driverExeSuffix() {
    return isWindows() ? ".exe" : "";
  }

  public String pathSeparator() {
    return isWindows() ? ";" : ":";
  }
}
