/*
 * MIT License
 *
 * Copyright (c) 2024 buddhika bhageashwara alwis
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package wam.automationtool.application.config.seleniumwebdriver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.manager.SeleniumManager;
import org.openqa.selenium.manager.SeleniumManagerOutput;

@Slf4j
public class BrowserDriverDownloader {

  private static final long RETRY_SLEEP_MS = 2000;

  private BrowserDriverDownloader() {}

  public static void downloadRequiredDriversWithRetries(
      final String workingDirectory, final OSInfo osInfo, final int maxAttempts) {
    for (int attempt = 1; attempt <= maxAttempts; attempt++) {
      try {
        log.info("⏳ Driver provisioning attempt {}/{}", attempt, maxAttempts);
        downloadRequiredDrivers(workingDirectory, osInfo);
        log.info("✅ Driver provisioning completed.");
        return;
      } catch (final Exception exception) {
        log.error(
            "❌ Driver provisioning attempt {}/{} failed: {}",
            attempt,
            maxAttempts,
            exception.getMessage(),
            exception);
        if (attempt == maxAttempts) {
          throw exception;
        }
        sleepQuietly(RETRY_SLEEP_MS);
      }
    }
  }

  public static void downloadRequiredDrivers(final String workingDirectory, final OSInfo osInfo) {
    final Path driversDir = Paths.get(workingDirectory, "drivers");
    try {
      Files.createDirectories(driversDir);
    } catch (final IOException ioException) {
      throw new IllegalStateException("Failed to create drivers directory: " + driversDir, ioException);
    }
    final String suffix = osInfo.driverExeSuffix();
    final String chromeName = "chromedriver" + suffix;
    final String geckoName = "geckodriver" + suffix;
    final boolean chromeOk = resolveAndCopy(driversDir, chromeName, new ChromeOptions(), osInfo);
    final boolean firefoxOk = resolveAndCopy(driversDir, geckoName, new FirefoxOptions(), osInfo);
    if (!chromeOk) {
      throw new IllegalStateException(chromeName + " could not be prepared");
    }
    if (!firefoxOk) {
      throw new IllegalStateException(geckoName + " could not be prepared");
    }
    // ✅ Edge required only on Windows (your requirement)
    if (osInfo.isWindows()) {
      final String edgeName = "msedgedriver.exe";
      // IMPORTANT: Do NOT use SeleniumManager for Edge (it tries azureedge URL in your env).
      final boolean edgeOk = ManualEdgeDriverDownloader.downloadEdgeDriverWin64(driversDir);
      if (!edgeOk) {
        throw new IllegalStateException(edgeName + " could not be prepared on Windows");
      }
    } else {
      log.info("ℹ Non-Windows OS detected. Skipping Edge driver provisioning.");
    }
  }

  private static boolean resolveAndCopy(
      final Path driversDir,
      final String targetFileName,
      final Capabilities capabilities,
      final OSInfo osInfo) {
    try {
      final SeleniumManagerOutput.Result result =
          SeleniumManager.getInstance().getDriverPath(capabilities, false);
      final String driverPathStr = result.getDriverPath();
      if (driverPathStr == null || driverPathStr.isBlank()) {
        log.warn("Driver not available for {} (empty path).", targetFileName);
        return false;
      }
      final Path downloaded = Paths.get(driverPathStr);
      if (!Files.exists(downloaded)) {
        log.warn("Driver not found on disk for {}. Resolved path: {}", targetFileName, downloaded);
        return false;
      }
      final Path target = driversDir.resolve(targetFileName);
      Files.copy(downloaded, target, StandardCopyOption.REPLACE_EXISTING);
      final File targetFile = target.toFile();
      if (!osInfo.isWindows() && !targetFile.setExecutable(true)) {
        log.warn("Could not set executable permission for {}", target);
      }
      log.info("✅ Driver ready: {} -> {}", downloaded, target);
      return true;
    } catch (final Exception exception) {
      log.error(
          "❌ Failed to prepare driver {}. Reason: {}",
          targetFileName,
          exception.getMessage(),
          exception);
      return false;
    }
  }

  private static void sleepQuietly(final long ms) {
    try {
      Thread.sleep(ms);
    } catch (final InterruptedException interruptedException) {
      Thread.currentThread().interrupt();
    }
  }
}
