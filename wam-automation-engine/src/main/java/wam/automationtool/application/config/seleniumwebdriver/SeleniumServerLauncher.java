package wam.automationtool.application.config.seleniumwebdriver;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SeleniumServerLauncher {

  private static final int MAX_ATTEMPTS = 3;
  private static final long RETRY_SLEEP_MS = 2000;
  private static final long SERVER_STARTUP_TIMEOUT_MS = 45_000;
  private static final long SERVER_POLL_INTERVAL_MS = 700;

  @Value("${selenium.grid.base.url}")
  private String seleniumGridBaseURL;

  @Value("${selenium.jar.name}")
  private String seleniumJARName;

  @Value("${selenium.jar.download.link}")
  private String seleniumJARDownloadLink;

  // ✅ prevents multiple server starts
  private volatile Process seleniumProcess;

  private static void sleepQuietly(final long ms) {
    try {
      Thread.sleep(ms);
    } catch (final InterruptedException interruptedException) {
      Thread.currentThread().interrupt();
    }
  }

  @PostConstruct
  public void init() {
    try {
      startSeleniumServerWithRetries();
    } catch (final Exception exception) {
      log.error(
          "❌ Selenium Server startup failed after retries. Application startup will be aborted.",
          exception);
      throw new IllegalStateException("Selenium Server startup failed", exception);
    }
  }

  private void startSeleniumServerWithRetries() throws IOException {
    for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
      try {
        log.info("⏳ Selenium startup attempt {}/{}", attempt, MAX_ATTEMPTS);
        startSeleniumServerOnce();
        waitUntilServerUpOrThrow();
        log.info("✅ Selenium Server is UP and ready.");
        return;
      } catch (final Exception exception) {
        log.error(
            "❌ Selenium startup attempt {}/{} failed: {}",
            attempt,
            MAX_ATTEMPTS,
            exception.getMessage(),
            exception);

        if (attempt == MAX_ATTEMPTS) {
          throw exception;
        }
        sleepQuietly(RETRY_SLEEP_MS);
      }
    }
  }

  private void startSeleniumServerOnce() throws IOException {
    if (isSeleniumServerRunning()) {
      log.info("✅ Selenium Server is already running. Connecting to the existing instance.");
      return;
    }
    if (seleniumProcess != null && seleniumProcess.isAlive()) {
      log.info(
          "✅ Selenium Server process is already started (still alive). Waiting for readiness...");
      return;
    }
    final String workingDirectory = System.getProperty("user.dir");
    final String seleniumJarPath = workingDirectory + File.separator + seleniumJARName;
    final File seleniumJarFile = new File(seleniumJarPath);
    if (!seleniumJarFile.exists()) {
      log.info("Selenium server JAR not found. Downloading...");
      SeleniumDownloader.downloadSeleniumServer(seleniumJARName, seleniumJARDownloadLink);
    }
    final OSInfo osInfo = OSInfo.detect();
    // ✅ download drivers before starting Selenium Server
    BrowserDriverDownloader.downloadRequiredDriversWithRetries(
        workingDirectory, osInfo, MAX_ATTEMPTS);
    final String driverDir = workingDirectory + File.separator + "drivers";
    final String suffix = osInfo.driverExeSuffix();
    final Path chromeDriver = Paths.get(driverDir, "chromedriver" + suffix);
    final Path geckoDriver = Paths.get(driverDir, "geckodriver" + suffix);
    // Edge only on Windows
    final Path edgeDriver = osInfo.isWindows() ? Paths.get(driverDir, "msedgedriver.exe") : null;
    log.info("✅ Starting Selenium Server with driver directory: {}", driverDir);
    log.info(
        "OS: windows={}, linux={}, mac={}", osInfo.isWindows(), osInfo.isLinux(), osInfo.isMac());
    // ✅ Build command safely
    final List<String> command = new ArrayList<>();
    command.add("java");
    // Explicit driver locations
    if (Files.exists(chromeDriver)) {
      command.add("-Dwebdriver.chrome.driver=" + chromeDriver.toAbsolutePath());
    }
    if (Files.exists(geckoDriver)) {
      command.add("-Dwebdriver.gecko.driver=" + geckoDriver.toAbsolutePath());
    }
    if (osInfo.isWindows() && edgeDriver != null && Files.exists(edgeDriver)) {
      command.add("-Dwebdriver.edge.driver=" + edgeDriver.toAbsolutePath());
    }
    command.add("-jar");
    command.add(seleniumJarPath);
    command.add("standalone");
    // ✅ Reduce noise: INFO (use FINE only when debugging)
    command.add("--log-level");
    command.add("INFO");
    final ProcessBuilder processBuilder = new ProcessBuilder(command);
    // ✅ Override PATH only for Selenium Server process
    final Map<String, String> env = processBuilder.environment();
    final String originalPath = env.getOrDefault("PATH", "");
    env.put("PATH", driverDir + osInfo.pathSeparator() + originalPath);
    // Avoid accidentally forcing offline mode via env
    env.put("SE_OFFLINE", "false");
    // ✅ Redirect Selenium logs to file instead of your app console
    Files.createDirectories(Paths.get(workingDirectory, "logs"));
    final File seleniumOut =
        Paths.get(workingDirectory, "logs", "selenium-server.out.log").toFile();
    final File seleniumErr =
        Paths.get(workingDirectory, "logs", "selenium-server.err.log").toFile();
    processBuilder.redirectOutput(ProcessBuilder.Redirect.appendTo(seleniumOut));
    processBuilder.redirectError(ProcessBuilder.Redirect.appendTo(seleniumErr));
    log.info("Selenium command: {}", String.join(" ", command));
    log.info("Selenium stdout log: {}", seleniumOut.getAbsolutePath());
    log.info("Selenium stderr log: {}", seleniumErr.getAbsolutePath());
    seleniumProcess = processBuilder.start();
  }

  private void waitUntilServerUpOrThrow() {
    final long start = System.currentTimeMillis();
    while (System.currentTimeMillis() - start < SERVER_STARTUP_TIMEOUT_MS) {
      if (isSeleniumServerRunning()) {
        return;
      }
      sleepQuietly(SERVER_POLL_INTERVAL_MS);
    }
    throw new IllegalStateException(
        "Selenium Server did not become ready within " + SERVER_STARTUP_TIMEOUT_MS + "ms");
  }

  private boolean isSeleniumServerRunning() {
    try {
      final URL url = new URL(seleniumGridBaseURL + "/status");
      final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("GET");
      connection.setConnectTimeout(3000);
      connection.setReadTimeout(3000);
      return connection.getResponseCode() == 200;
    } catch (final IOException ioException) {
      log.warn("⚠ Selenium Server not running: {}", ioException.getMessage());
      return false;
    }
  }
}
