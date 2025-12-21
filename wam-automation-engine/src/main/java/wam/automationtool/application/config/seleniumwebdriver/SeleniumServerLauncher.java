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
            log.error("❌ Selenium Server startup failed after retries. Application startup will be aborted.", exception);
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
                log.error("❌ Selenium startup attempt {}/{} failed: {}", attempt, MAX_ATTEMPTS,
                        exception.getMessage(), exception);
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
        // ✅ if we already started a process, don't start again
        if (seleniumProcess != null && seleniumProcess.isAlive()) {
            log.info("✅ Selenium Server process is already started (still alive). Waiting for readiness...");
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
        BrowserDriverDownloader.downloadRequiredDriversWithRetries(workingDirectory, osInfo, MAX_ATTEMPTS);
        final String driverDir = workingDirectory + File.separator + "drivers";
        final Path chromeDriver = Paths.get(driverDir, "chromedriver.exe");
        final Path geckoDriver  = Paths.get(driverDir, "geckodriver.exe");
        final Path edgeDriver   = Paths.get(driverDir, "msedgedriver.exe");
        log.info("✅ Starting Selenium Server with driver directory: {}", driverDir);
        log.info("chromedriver exists: {} -> {}", Files.exists(chromeDriver), chromeDriver.toAbsolutePath());
        log.info("geckodriver exists:  {} -> {}", Files.exists(geckoDriver), geckoDriver.toAbsolutePath());
        log.info("msedgedriver exists: {} -> {}", Files.exists(edgeDriver), edgeDriver.toAbsolutePath());
        // ✅ Build command safely (avoid split(" ") issues)
        final List<String> command = new ArrayList<>();
        command.add("java");
        // ✅ Force Selenium Standalone to know exact driver locations (critical for Edge)
        if (Files.exists(chromeDriver)) {
            command.add("-Dwebdriver.chrome.driver=" + chromeDriver.toAbsolutePath());
        }
        if (Files.exists(geckoDriver)) {
            command.add("-Dwebdriver.gecko.driver=" + geckoDriver.toAbsolutePath());
        }
        if (osInfo.isWindows() && Files.exists(edgeDriver)) {
            command.add("-Dwebdriver.edge.driver=" + edgeDriver.toAbsolutePath());
        }
        command.add("-jar");
        command.add(seleniumJarPath);
        command.add("standalone");
        command.add("--log-level");
        command.add("FINE");
        final ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.inheritIO();
        // ✅ Override PATH only for Selenium Server process (drivers first)
        final Map<String, String> env = processBuilder.environment();
        final String originalPath = env.getOrDefault("PATH", "");
        env.put("PATH", driverDir + osInfo.pathSeparator() + originalPath);
        // ✅ Make sure Selenium isn't forced into offline mode by env (if your machine has internet)
        // If you intentionally want offline, you can remove these two lines.
        env.remove("SE_OFFLINE");
        env.put("SE_OFFLINE", "false");
        // ✅ Helpful diagnostics
        log.info("Selenium command: {}", String.join(" ", command));
        log.info("Selenium PATH starts with drivers dir: {}", env.get("PATH").startsWith(driverDir));
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
        throw new IllegalStateException("Selenium Server did not become ready within " + SERVER_STARTUP_TIMEOUT_MS + "ms");
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
