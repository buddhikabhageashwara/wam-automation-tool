package wam.automationtool.application.config.seleniumwebdriver;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.manager.SeleniumManager;
import org.openqa.selenium.manager.SeleniumManagerOutput;

@Slf4j
public class BrowserDriverDownloader {

    private BrowserDriverDownloader() {
        // utility class
    }

    public static void downloadRequiredDrivers(final String workingDirectory) {
        final Path driversDir = Paths.get(workingDirectory, "drivers");

        try {
            Files.createDirectories(driversDir);
        } catch (final IOException e) {
            log.error("Failed to create drivers directory: {}", driversDir, e);
            return;
        }

        resolveAndCopy(driversDir, "chromedriver.exe", new ChromeOptions());
        resolveAndCopy(driversDir, "geckodriver.exe", new FirefoxOptions());
        resolveAndCopy(driversDir, "msedgedriver.exe", new EdgeOptions());

        // Safari: not supported on Windows
        // IE: legacy; if needed, ship IEDriverServer.exe yourself into drivers/
    }

    private static void resolveAndCopy(final Path driversDir,
                                       final String expectedExeName,
                                       final Capabilities capabilities) {
        try {
            final SeleniumManagerOutput.Result result =
                    SeleniumManager.getInstance().getDriverPath(capabilities, false);

            final String driverPathStr = result.getDriverPath();

            if (driverPathStr == null || driverPathStr.isBlank()) {
                log.warn("Driver not available for {} (empty path).", expectedExeName);
                return;
            }

            final Path downloaded = Paths.get(driverPathStr);
            if (!Files.exists(downloaded)) {
                log.warn("Driver not found on disk for {}. Resolved path: {}", expectedExeName, downloaded);
                return;
            }

            final Path target = driversDir.resolve(expectedExeName);
            Files.copy(downloaded, target, StandardCopyOption.REPLACE_EXISTING);

            final File targetFile = target.toFile();
            targetFile.setExecutable(true);

            log.info("✅ Driver ready: {} -> {}", downloaded, target);

        } catch (final Exception e) {
            log.error("❌ Failed to prepare driver {}. Reason: {}", expectedExeName, e.getMessage(), e);
        }
    }

}

