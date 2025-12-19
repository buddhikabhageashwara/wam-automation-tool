package wam.automationtool.application.config.seleniumwebdriver;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
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
            return; // Exit if Selenium Server is already running
        }
        final String workingDirectory = System.getProperty("user.dir"); // Application's working directory
        final String seleniumJarPath = workingDirectory + File.separator + seleniumJARName;
        final File seleniumJarFile = new File(seleniumJarPath);
        if (!seleniumJarFile.exists()) {
            log.info("Selenium server JAR not found. Downloading...");
            SeleniumDownloader.downloadSeleniumServer(seleniumJARName, seleniumJARDownloadLink); // Download the JAR if not present
        }
        final String command = "java -jar " + seleniumJarPath + " standalone";
        final ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
        processBuilder.inheritIO();
        final Process process = processBuilder.start();
        try {
            final int exitCode = process.waitFor();
            log.info("Selenium Server exited with code: {}", exitCode);
        } catch (final InterruptedException e) {
            log.error("Error while waiting for Selenium Server process", e);
            Thread.currentThread().interrupt();  // Restore interrupted status
        }
    }

    private boolean isSeleniumServerRunning() {
        try {
            final URL url = new URL(seleniumGridBaseURL + "/status");
            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            final int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                log.info("✅ Selenium Server is already running.");
                return true;
            }
        } catch (final IOException ioException) {
            log.warn("⚠ Selenium Server is not running or there was a connection issue. {}", ioException.getMessage());
        }
        return false;
    }
}

