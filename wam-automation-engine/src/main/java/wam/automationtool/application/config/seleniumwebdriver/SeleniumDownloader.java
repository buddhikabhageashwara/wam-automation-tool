package wam.automationtool.application.config.seleniumwebdriver;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SeleniumDownloader {

    public static void downloadSeleniumServer(
            final String seleniumJARName, final String seleniumJARDownloadLink) throws IOException {
        final String workingDirectory = System.getProperty("user.dir");
        final File seleniumJar = new File(workingDirectory, seleniumJARName);
        if (seleniumJar.exists()) {
            log.info("Selenium JAR already exists. Skipping download.");
            return;
        }
        log.info("Downloading Selenium Server from: {}", seleniumJARDownloadLink);
        try (final InputStream inputStream = new URL(seleniumJARDownloadLink).openStream()) {
            Files.copy(inputStream, seleniumJar.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (final IOException ioException) {
            log.error("Failed to download Selenium Server: {}", ioException.getMessage(), ioException);
            throw ioException;
        }
    }
}

