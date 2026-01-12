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
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SeleniumDownloader {

    private SeleniumDownloader() {}

    public static void downloadSeleniumServer(final String seleniumJARName,
                                              final String seleniumJARDownloadLink) throws IOException {
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

