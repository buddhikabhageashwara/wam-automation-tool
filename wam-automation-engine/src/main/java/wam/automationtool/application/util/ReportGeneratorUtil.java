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

package wam.automationtool.application.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import wam.automationtool.application.dto.cache.CacheDataDto;
import wam.automationtool.application.dto.report.FileDetailsDto;
import wam.automationtool.application.dto.report.TestCaseExecutionSummaryDto;
import wam.automationtool.application.dto.report.TestCaseStepExecutionDto;
import wam.automationtool.application.dto.report.TestCaseStepExecutionSummaryDto;
import wam.automationtool.application.dto.testcase.TestCaseDto;
import wam.automationtool.application.dto.testplan.TestPlanDto;

@Slf4j
public class ReportGeneratorUtil {

  private static final String BOOTSTRAP_CSS =
      "https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css";

  public static String initiateReportGeneration(
      final String executionId,
      final String fileBasePath,
      final String resourcePath,
      final WAMCacheManager wamCacheManager) {
    final String timestamp =
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss.SSSSSS"));
    final String folderName = "Report-" + timestamp;
    final String folderPath = fileBasePath + File.separator + folderName;
    final String randomNumber = executionId;
    final String fileName = "WAM-TC-Execution-Report-".concat(randomNumber).concat(".html");
    final String filePath = folderPath + File.separator + fileName;
    try {
      createFolder(folderPath);
      createFolder(folderPath + File.separator + "images");
      createFolder(folderPath + File.separator + "logs");
      copyResourcesToFolder(resourcePath, folderPath + File.separator + "images");
      generateHtmlReport(filePath, executionId);
      final CacheDataDto cacheDataDto =
          CacheDataDto.builder().reportLogsFolderPath(folderPath + File.separator + "logs").build();
      wamCacheManager.addToCache(executionId, cacheDataDto);
    } catch (final IOException ioException) {
      log.error("An error occurred: {}", ioException.getMessage());
      return null;
    }
    return filePath;
  }

  private static void createFolder(final String folderPath) throws IOException {
    final File folder = new File(folderPath);
    if (!folder.exists() && !folder.mkdirs()) {
      throw new IOException("Failed to create directory: " + folderPath);
    }
  }

  private static void copyResourcesToFolder(
      final String sourcePathStr, final String destinationPathStr) throws IOException {
    final Path sourcePath = Paths.get(sourcePathStr);
    final Path destinationPath = Paths.get(destinationPathStr);
    // Check if the source is a file
    if (Files.isRegularFile(sourcePath)) {
      // Ensure the destination directory exists
      Files.createDirectories(destinationPath);
      // Copy the file directly to the destination
      final Path destinationFile = destinationPath.resolve(sourcePath.getFileName());
      Files.copy(sourcePath, destinationFile, StandardCopyOption.REPLACE_EXISTING);
      log.info("Copied file: {} to {}", sourcePath, destinationFile);
    } else if (Files.isDirectory(sourcePath)) {
      // Walk through the directory and copy files/subdirectories
      final AtomicReference<Path> destination = new AtomicReference<>();
      Files.walk(sourcePath)
          .forEach(
              source -> {
                try {
                  destination.set(destinationPath.resolve(sourcePath.relativize(source)));
                  if (Files.isDirectory(source)) {
                    Files.createDirectories(destination.get());
                  } else {
                    // Copy file with binary-safe options
                    Files.copy(source, destination.get(), StandardCopyOption.REPLACE_EXISTING);
                  }
                } catch (IOException e) {
                  log.error("Failed to copy file: {} to {}", source, destination.get(), e);
                }
              });
    } else {
      log.error("Invalid source path: {}", sourcePath);
      throw new IOException("Source path is neither a file nor a directory.");
    }
  }

  private static void generateHtmlReport(final String filePath, final String executionId)
      throws IOException {
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
      writer.write(generateHtmlContent(executionId));
    }
  }

  private static String generateHtmlContent(final String executionId) {
    String html =
        "<!DOCTYPE html>\n<html lang='en'>\n<head>\n"
            + "<meta charset='UTF-8'>\n<meta name='viewport' content='width=device-width, initial-scale=1.0'>\n"
            + "<link rel='stylesheet' href='"
            + BOOTSTRAP_CSS
            + "'>\n"
            + "<style>\n"
            + "body.light-mode { background-color: #f8f9fa; color: #212529; }\n"
            + "body.dark-mode { background-color: #212529; color: #f8f9fa; }\n"
            +

            // Apply button styling ONLY to the dark-mode toggle button
            ".dark-mode #toggleButton { background-color: #343a40; color: white; }\n"
            + ".light-mode #toggleButton { background-color: #007bff; color: white; }\n"
            + ".jumbotron { padding: 2rem 1rem; margin-bottom: 2rem; }\n"
            + ".light-mode .jumbotron { background-color: #e9ecef; }\n"
            + ".dark-mode .jumbotron { background-color: #343a40; }\n"
            + ".light-mode .test-case-detail-heading { background-color: #e9ecef; }\n"
            + ".dark-mode .test-case-detail-heading { background-color: #343a40; }\n"
            + "#toggleButton { float: right; }\n"
            +

            // Table styles
            "table { width: 100%; border-collapse: collapse; margin-top: 20px; }\n"
            + "th, td { border: 1px solid #dee2e6; padding: 8px; text-align: left; }\n"
            + ".light-mode table { background-color: white; color: #212529; }\n"
            + ".dark-mode table { background-color: #343a40; color: #f8f9fa; }\n"
            + ".dark-mode th { background-color: #495057; color: #f8f9fa; }\n"
            +

            // Hover styles
            ".light-mode tr:hover { background-color: #f1f1f1; color: #212529; }\n"
            +

            // ✅ FIX: enforce readable text color on hover in dark mode (td/th/a)
            ".dark-mode tr:hover { background-color: #495057; color: #f8f9fa; }\n"
            + ".dark-mode table td, .dark-mode table th { color: #f8f9fa; }\n"
            + ".dark-mode table tbody tr:hover td, .dark-mode table tbody tr:hover th { color: #f8f9fa !important; }\n"
            + ".dark-mode table tbody tr:hover a { color: #f8f9fa !important; }\n"
            +

            // ✅ NEW: Collapse toggle button + square icon (no underline, bigger, with background)
            ".collapse-toggle-btn { padding: 0; border: none; background: transparent; cursor: pointer; }\n"
            + ".collapse-toggle-btn:focus { outline: none; box-shadow: none; }\n"
            + ".collapse-toggle-btn:hover { text-decoration: none; }\n"
            + ".collapse-icon { display: inline-flex; align-items: center; justify-content: center; "
            + "width: 30px; height: 30px; border-radius: 6px; font-weight: 700; font-size: 20px; line-height: 1; }\n"
            +

            // Light mode icon look
            ".light-mode .collapse-icon { background-color: #e9ecef; border: 1px solid #ced4da; color: #007bff; }\n"
            + ".light-mode .collapse-toggle-btn:hover .collapse-icon { background-color: #dee2e6; }\n"
            +

            // Dark mode icon look
            ".dark-mode .collapse-icon { background-color: #495057; border: 1px solid #6c757d; color: #ffffff; }\n"
            + ".dark-mode .collapse-toggle-btn:hover .collapse-icon { background-color: #5a6268; }\n"
            + "</style>\n<title>WAM Automation Test Case Execution Report</title>\n"
            + "</head>\n<body class='container-fluid my-4 light-mode'>\n"
            +

            // Load JS libs first
            "<script src='https://code.jquery.com/jquery-3.5.1.slim.min.js'></script>\n"
            + "<script src='https://cdn.jsdelivr.net/npm/popper.js@1.16.1/dist/umd/popper.min.js'></script>\n"
            + "<script src='https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/js/bootstrap.min.js'></script>\n"
            +

            // Page content
            "<button id='toggleButton' class='btn'>Dark Mode</button>\n<br><br><br>\n"
            +

            // Dark mode toggle script
            "<script>\n"
            + "document.getElementById('toggleButton').addEventListener('click', function() {\n"
            + "  var body = document.body;\n"
            + "  if (body.classList.contains('light-mode')) {\n"
            + "    body.classList.remove('light-mode');\n"
            + "    body.classList.add('dark-mode');\n"
            + "  } else {\n"
            + "    body.classList.remove('dark-mode');\n"
            + "    body.classList.add('light-mode');\n"
            + "  }\n"
            + "});\n</script>\n"
            + "<h1><img src='images/logo.png' alt='WAM Logo' height='50'> WAM Automation Test Case Execution Report</h1>\n"
            + "<h3>Execution Id: "
            + executionId
            + "</h3><hr>\n";

    return html;
  }

  public static void appendTestPlanDetails(final String fileName, final TestPlanDto testPlanDto) {
    try {
      try (final BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
        writer.write("<section class='jumbotron'>\n<h2>Test Plan Details</h2>\n");
        writer.write("<p><strong>Test Plan Id:</strong> " + testPlanDto.getId() + "</p>\n");
        writer.write(
            "<p><strong>Test Plan Name:</strong> " + testPlanDto.getTestPlanName() + "</p>\n");
        writer.write(
            "<p><strong>Description:</strong> "
                + testPlanDto.getDescription()
                + "</p>\n</section><hr>\n");
      }
    } catch (final IOException ioException) {
      log.error("An error occurred: {}", ioException.getMessage());
    }
  }

  public static void appendTestCaseDetails(final String fileName, final TestCaseDto testCaseDto) {
    try {
      try (final BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
        writer.write("<br>");
        writer.write(
            "<section>\n<h2 class='test-case-detail-heading' style='padding:1%;'>[Test Case ID: "
                + testCaseDto.getId()
                + "] - ["
                + testCaseDto.getTestCaseName()
                + "]</h2>\n");
        writer.write(
            "<p><strong>Execution Order:</strong> " + testCaseDto.getExecutionOrder() + "</p>\n");
        writer.write(
            "<p><strong>Description:</strong> "
                + testCaseDto.getDescription()
                + "</p>\n</section>\n");
      }
    } catch (final IOException ioException) {
      log.error("An error occurred: {}", ioException.getMessage());
    }
  }

  public static void createTestCaseStepDetailsTable(final String fileName) {
    try {
      final String uniqueId =
          "table_" + System.currentTimeMillis() + "_" + (int) (Math.random() * 1000);

      final String collapseId = "collapse_" + uniqueId;
      final String iconId = "icon_" + uniqueId;
      final String searchInputId = "searchInput_" + uniqueId;

      try (final BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {

        writer.write("<section class='mt-3'>\n");

        // Header + toggle button (square icon + / -)
        writer.write("<div class='d-flex align-items-center justify-content-between'>\n");
        writer.write("<h5 class='mb-0'>Test Case Steps Details</h5>\n");
        writer.write(
            "<button class='collapse-toggle-btn' type='button' "
                + "data-toggle='collapse' data-target='#"
                + collapseId
                + "' "
                + "aria-expanded='false' aria-controls='"
                + collapseId
                + "'>"
                + "<span class='collapse-icon' id='"
                + iconId
                + "'>+</span>"
                + "</button>\n");
        writer.write("</div>\n");

        // Collapsible container (default collapsed => no 'show' class)
        writer.write("<div id='" + collapseId + "' class='collapse mt-2'>\n");
        writer.write("<div class='card card-body p-2'>\n");

        // Search bar (inside collapse so it hides too)
        writer.write(
            "<input class='form-control mb-2' id='"
                + searchInputId
                + "' type='text' placeholder='Search...'>\n");

        // Search filtering script
        writer.write("<script>\n");
        writer.write(
            "document.getElementById('"
                + searchInputId
                + "').addEventListener('keyup', function() {\n");
        writer.write("  let filter = this.value.toUpperCase();\n");
        writer.write("  let rows = document.querySelectorAll('#" + uniqueId + " tbody tr');\n");
        writer.write("  rows.forEach(row => {\n");
        writer.write("    let match = row.textContent.toUpperCase().includes(filter);\n");
        writer.write("    row.style.display = match ? '' : 'none';\n");
        writer.write("  });\n");
        writer.write("});\n");
        writer.write("</script>\n");

        // Icon toggle (+ / -) using Bootstrap collapse events
        writer.write("<script>\n");
        writer.write("$('#" + collapseId + "').on('show.bs.collapse', function () {\n");
        writer.write("  document.getElementById('" + iconId + "').textContent = '-';\n");
        writer.write("});\n");
        writer.write("$('#" + collapseId + "').on('hide.bs.collapse', function () {\n");
        writer.write("  document.getElementById('" + iconId + "').textContent = '+';\n");
        writer.write("});\n");
        writer.write("</script>\n");

        // Table start
        writer.write("<div class='table-responsive'>\n");
        writer.write("<table id='" + uniqueId + "' class='table table-striped table-hover'>\n");
        writer.write("<thead class='thead-dark'><tr>\n");
        writer.write(
            "<th>Start Time</th><th>End Time</th><th>TCS Id</th><th>Execution Order</th><th>TCS Type</th>\n");
        writer.write(
            "<th>TCS Name</th><th>Expected Result</th><th>Actual Result</th><th>Status</th>\n");
        writer.write("</tr></thead>\n<tbody>\n");
      }
    } catch (final IOException ioException) {
      log.error("An error occurred: {}", ioException.getMessage());
    }
  }

  public static void appendTestCaseStepDetails(
      final String fileName, final TestCaseStepExecutionDto testCaseStepExecutionDto) {
    try {
      try (final BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
        writer.write("<tr>\n<td>" + testCaseStepExecutionDto.getStartTime() + "</td>\n");
        writer.write("<td>" + testCaseStepExecutionDto.getEndTime() + "</td>\n");
        writer.write("<td>TCS-" + testCaseStepExecutionDto.getId() + "</td>\n");
        writer.write("<td>" + testCaseStepExecutionDto.getExecutionOrder() + "</td>\n");
        writer.write("<td>" + testCaseStepExecutionDto.getTestCaseStepType() + "</td>\n");
        writer.write("<td>" + testCaseStepExecutionDto.getTestCaseStepName() + "</td>\n");
        writer.write("<td>" + escapeHtml(testCaseStepExecutionDto.getExpectedResult()) + "</td>\n");
        writer.write("<td>" + escapeHtml(testCaseStepExecutionDto.getActualResult()) + "</td>\n");
        writer.write("<td>" + testCaseStepExecutionDto.getStatus() + "</td>\n</tr>\n");
      }
    } catch (final IOException ioException) {
      log.error("An error occurred: {}", ioException.getMessage());
    }
  }

  public static void endTestCaseStepDetailsTable(final String fileName) {
    try {
      try (final BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
        writer.write("</tbody>\n</table>\n</div>\n"); // table-responsive
        writer.write("</div>\n</div>\n"); // card-body + collapse
        writer.write("</section>\n"); // section
      }
    } catch (final IOException ioException) {
      log.error("An error occurred: {}", ioException.getMessage());
    }
  }

  public static void appendTestCaseStepExecutionSummary(
      final String fileName,
      final TestCaseStepExecutionSummaryDto testCaseStepExecutionSummaryDto) {
    try {
      try (final BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
        writer.write("<section>\n<h5>Test Case Step Execution Summary</h5>\n");
        writer.write(
            "<p><strong>Total Test Case Steps:</strong> "
                + testCaseStepExecutionSummaryDto.getTotalTestCaseSteps()
                + "</p>\n");
        writer.write(
            "<p><strong>Passed:</strong> "
                + testCaseStepExecutionSummaryDto.getPassedTestCaseSteps()
                + "</p>\n");
        writer.write(
            "<p><strong>Failed:</strong> "
                + testCaseStepExecutionSummaryDto.getFailedTestCaseSteps()
                + "</p>\n</section>\n");
      }
    } catch (final IOException ioException) {
      log.error("An error occurred: {}", ioException.getMessage());
    }
  }

  public static void appendFileDetails(
      final String reportHtmlFilePath, final FileDetailsDto fileDetailsDto) {
    try {
      if (Objects.isNull(fileDetailsDto)
          || Objects.isNull(fileDetailsDto.getFileNameList())
          || fileDetailsDto.getFileNameList().isEmpty()) {
        log.error("fileDetailsDto or fileNameList is null/empty: unable to append file details");
        return;
      }
      final Path reportPath = Paths.get(reportHtmlFilePath).toAbsolutePath().normalize();
      final Path reportDir = reportPath.getParent(); // .../Report-xxxx/
      if (Objects.isNull(reportDir)) {
        log.error("report directory not found: reportHtmlFilePath: {}", reportHtmlFilePath);
        return;
      }
      try (final BufferedWriter writer =
          new BufferedWriter(new FileWriter(reportHtmlFilePath, true))) {
        writer.write("<section>\n<h2>Log Files</h2>\n");
        writer.write(
            "<table class='table table-bordered'>\n<thead><tr><th>File</th></tr></thead>\n<tbody>\n");

        for (final String logFilePathStr : fileDetailsDto.getFileNameList()) {
          if (Objects.isNull(logFilePathStr) || logFilePathStr.isBlank()) {
            continue;
          }
          final Path logPath = Paths.get(logFilePathStr).toAbsolutePath().normalize();
          // Build a relative path so the report remains portable when the whole folder is moved
          String href;
          try {
            final Path rel = reportDir.relativize(logPath);
            href = rel.toString().replace("\\", "/"); // URLs must use /
          } catch (final Exception ex) {
            // fallback: if relativize fails, at least use file name under logs/
            final String fileNameOnly = logPath.getFileName().toString();
            href = "logs/" + fileNameOnly;
          }
          // Display label you requested: "logs\FILE_NAME"
          final String displayName = "logs\\" + logPath.getFileName();
          // Basic HTML escaping for display text (avoid breaking HTML)
          final String safeDisplay = escapeHtml(displayName);
          writer.write("<tr><td>");
          writer.write("<a href=\"" + href + "\" target=\"_blank\" rel=\"noopener noreferrer\">");
          writer.write(safeDisplay);
          writer.write("</a>");
          writer.write("</td></tr>\n");
        }
        writer.write("</tbody>\n</table>\n</section><hr>\n");
      }
    } catch (final IOException ioException) {
      log.error("append file details failed: reason: {}", ioException.getMessage());
    } catch (final Exception exception) {
      log.error("append file details failed: reason: {}", exception.getMessage());
    }
  }

  private static String escapeHtml(final String input) {
    if (input == null) {
      return "";
    }
    return input
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;");
  }

  public static void appendTestCaseExecutionSummary(
      final String fileName, final TestCaseExecutionSummaryDto summaryDto) {
    try {
      try (final BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
        writer.write("<br><br>");
        writer.write("<section class='jumbotron'>\n<h2>Test Case Execution Summary</h2>\n");
        writer.write(
            "<p><strong>Total Test Cases:</strong> " + summaryDto.getTotalTestCases() + "</p>\n");
        writer.write("<p><strong>Passed:</strong> " + summaryDto.getPassedTestCases() + "</p>\n");
        writer.write(
            "<p><strong>Failed:</strong> "
                + summaryDto.getFailedTestCases()
                + "</p>\n</section>\n");
      }
    } catch (final IOException ioException) {
      log.error("An error occurred: {}", ioException.getMessage());
    }
  }

  public static void completeReportGeneration(final String fileName) {
    try {
      try (final BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
        writer.write("</body>\n</html>\n");
      }
    } catch (final IOException ioException) {
      log.error("An error occurred: {}", ioException.getMessage());
    }
  }
}
