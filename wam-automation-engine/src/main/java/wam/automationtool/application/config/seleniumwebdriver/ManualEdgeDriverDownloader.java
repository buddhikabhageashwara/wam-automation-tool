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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ManualEdgeDriverDownloader {

  private static final Pattern VERSION_PATTERN =
      Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)\\.(\\d+)");
  private static final String NEW_HOST = "https://msedgedriver.microsoft.com";

  private ManualEdgeDriverDownloader() {}

  public static boolean downloadEdgeDriverWin64(final Path driversDir) {
    try {
      Files.createDirectories(driversDir);
      final String edgeVersion = detectEdgeVersionFromRegistry();
      if (edgeVersion == null) {
        log.error("❌ Could not detect Microsoft Edge version from registry.");
        return false;
      }
      final String zipUrl = NEW_HOST + "/" + edgeVersion + "/edgedriver_win64.zip";
      final Path zipPath = driversDir.resolve("edgedriver_win64.zip");
      log.info("⏳ Downloading EdgeDriver from: {}", zipUrl);
      downloadToFile(zipUrl, zipPath);
      final Path exe = extractFromZip(zipPath, "msedgedriver.exe", driversDir);
      if (Objects.isNull(exe) || !Files.exists(exe)) {
        log.error("❌ Failed to extract msedgedriver.exe from downloaded zip.");
        return false;
      }
      try {
        Files.deleteIfExists(zipPath);
      } catch (Exception ignore) {
      }
      log.info("✅ EdgeDriver ready: {}", exe.toAbsolutePath());
      return true;
    } catch (final Exception exception) {
      log.error("❌ Manual EdgeDriver download failed: {}", exception.getMessage(), exception);
      return false;
    }
  }

  private static String detectEdgeVersionFromRegistry() throws IOException, InterruptedException {
    // HKCU first
    String out =
        runAndCollect(
            new String[] {
              "cmd.exe", "/c", "reg query \"HKCU\\Software\\Microsoft\\Edge\\BLBeacon\" /v version"
            });
    String ver = extractVersion(out);
    if (Objects.nonNull(ver)) return ver;
    // HKLM fallback
    out =
        runAndCollect(
            new String[] {
              "cmd.exe", "/c", "reg query \"HKLM\\Software\\Microsoft\\Edge\\BLBeacon\" /v version"
            });
    ver = extractVersion(out);
    return ver;
  }

  private static String extractVersion(final String registryOutput) {
    if (Objects.isNull(registryOutput)) return null;
    final Matcher matcher = VERSION_PATTERN.matcher(registryOutput);
    if (matcher.find()) return matcher.group(0);
    return null;
  }

  private static String runAndCollect(final String[] command)
      throws IOException, InterruptedException {
    final ProcessBuilder processBuilder = new ProcessBuilder(command);
    processBuilder.redirectErrorStream(true);
    final Process process = processBuilder.start();
    final StringBuilder stringBuilder = new StringBuilder();
    try (BufferedReader bufferedReader =
        new BufferedReader(
            new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
      String line;
      while ((line = bufferedReader.readLine()) != null) {
        stringBuilder.append(line).append("\n");
      }
    }
    process.waitFor();
    return stringBuilder.toString();
  }

  private static void downloadToFile(final String url, final Path out) throws IOException {
    final HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
    conn.setRequestMethod("GET");
    conn.setConnectTimeout(15_000);
    conn.setReadTimeout(60_000);
    final int code = conn.getResponseCode();
    if (code != 200) {
      throw new IOException("Failed to download. HTTP " + code + " from " + url);
    }
    try (InputStream inputStream = conn.getInputStream()) {
      Files.copy(inputStream, out, StandardCopyOption.REPLACE_EXISTING);
    }
  }

  private static Path extractFromZip(final Path zipFile, final String fileName, final Path outDir)
      throws IOException {
    try (ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(zipFile))) {
      ZipEntry entry;
      while ((entry = zipInputStream.getNextEntry()) != null) {
        final String entryName = entry.getName().replace("/", "\\");
        if (!entry.isDirectory() && entryName.endsWith(fileName)) {
          final Path out = outDir.resolve(fileName);
          Files.copy(zipInputStream, out, StandardCopyOption.REPLACE_EXISTING);
          return out;
        }
      }
    }
    return null;
  }
}
