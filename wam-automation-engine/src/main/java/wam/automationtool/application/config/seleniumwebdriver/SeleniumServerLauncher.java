package wam.automationtool.application.config.seleniumwebdriver;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SeleniumServerLauncher {

  @Value("${selenium.grid.base.url}")
  private String seleniumGridBaseURL;

  @Value("${selenium.jar.name}")
  private String seleniumJARName;

  @Value("${selenium.jar.download.link}")
  private String seleniumJARDownloadLink;

  @PostConstruct
  public void init() {
    try {
      startSeleniumServer();
    } catch (final IOException ioException) {
      log.error("Error starting Selenium Server", ioException);
    }
  }

  private void startSeleniumServer() throws IOException {

    if (isSeleniumServerRunning()) {
      log.info("✅ Selenium Server is already running. Connecting to the existing instance.");
      return;
    }

    final String workingDirectory = System.getProperty("user.dir");
    final String seleniumJarPath = workingDirectory + File.separator + seleniumJARName;

    final File seleniumJarFile = new File(seleniumJarPath);
    if (!seleniumJarFile.exists()) {
      log.info("Selenium server JAR not found. Downloading...");
      SeleniumDownloader.downloadSeleniumServer(seleniumJARName, seleniumJARDownloadLink);
    }

    // ✅ NEW (only change): download drivers before starting Selenium Server
    BrowserDriverDownloader.downloadRequiredDrivers(workingDirectory);

    final String command = "java -jar " + seleniumJarPath + " standalone";
    final ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
    processBuilder.inheritIO();

    // 🔥 STRATEGY 2: override PATH only for Selenium Server process
    final String driverDir = workingDirectory + File.separator + "drivers";
    final Map<String, String> env = processBuilder.environment();

    final String originalPath = env.getOrDefault("PATH", "");
    env.put("PATH", driverDir + ";" + originalPath);

    log.info("✅ Starting Selenium Server with driver directory: {}", driverDir);

    processBuilder.start(); // DO NOT waitFor()
  }

  private boolean isSeleniumServerRunning() {
    try {
      final URL url = new URL(seleniumGridBaseURL + "/status");
      final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("GET");
      connection.setConnectTimeout(3000);
      connection.setReadTimeout(3000);

      if (connection.getResponseCode() == 200) {
        log.info("✅ Selenium Server is already running.");
        return true;
      }
    } catch (final IOException ioException) {
      log.warn("⚠ Selenium Server not running: {}", ioException.getMessage());
    }
    return false;
  }
}
