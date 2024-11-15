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
import lombok.extern.slf4j.Slf4j;
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
      final String executionId, final String fileBasePath, final String resourcePath) {
    final String timestamp =
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss.SSSSSS"));
    final String folderName = "Report-" + timestamp;
    final String folderPath = fileBasePath + File.separator + folderName;
    final String randomNumber = executionId;
    final String fileName = "WAM-TC-Execution-Report-".concat(randomNumber).concat(".html");
    final String filePath = folderPath + File.separator + fileName;

    try {
      // Create the main report directory if it doesn't exist
      File folder = new File(folderPath);
      if (!folder.exists() && !folder.mkdirs()) {
        log.error("Failed to create directory: {}", folderPath);
        return null;
      }

      // Create the "images" subfolder within the report folder
      File imagesFolder = new File(folderPath + File.separator + "images");
      if (!imagesFolder.exists() && !imagesFolder.mkdirs()) {
        log.error("Failed to create images directory: {}", imagesFolder.getPath());
        return null;
      }

      // Create the "logs" subfolder within the report folder
      File logsFolder = new File(folderPath + File.separator + "logs");
      if (!logsFolder.exists() && !logsFolder.mkdirs()) {
        log.error("Failed to create logs directory: {}", logsFolder.getPath());
        return null;
      }

      // Copy files from resourcePath to the "images" folder
      Path sourcePath = Paths.get(resourcePath);
      Path destinationPath = imagesFolder.toPath();

      Files.walk(sourcePath)
          .forEach(
              source -> {
                try {
                  Path destination = destinationPath.resolve(sourcePath.relativize(source));
                  Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                  log.error("Failed to copy file: {} to {}", source, destinationPath);
                }
              });

      // Create and write to the report file
      try (final BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
        writer.write("<!DOCTYPE html>\n<html lang='en'>\n<head>\n");
        writer.write(
            "<meta charset='UTF-8'>\n<meta name='viewport' content='width=device-width, initial-scale=1.0'>\n");
        writer.write("<link rel='stylesheet' href='" + BOOTSTRAP_CSS + "'>\n");

        // Add CSS for light and dark mode, including jumbotron styling
        writer.write("<style>\n");
        writer.write("body.light-mode { background-color: #f8f9fa; color: #212529; }\n");
        writer.write("body.dark-mode { background-color: #212529; color: #f8f9fa; }\n");
        writer.write(
            ".dark-mode button { background-color: #343a40; color: white; border: none; padding: 10px; cursor: pointer; }\n");
        writer.write(
            ".light-mode button { background-color: #007bff; color: white; border: none; padding: 10px; cursor: pointer; }\n");

        // Jumbotron styling for both modes
        writer.write(
            ".jumbotron { padding: 2rem 1rem; margin-bottom: 2rem; border-radius: 0.3rem; }\n");
        writer.write(".light-mode .jumbotron { background-color: #e9ecef; color: #212529; }\n");
        writer.write(".dark-mode .jumbotron { background-color: #343a40; color: #f8f9fa; }\n");

        writer.write("table, th, td { border: 1px solid #ddd; }\n");
        writer.write("table { width: 100%; border-collapse: collapse; }\n");
        writer.write("th, td { padding: 8px; text-align: left; }\n");
        writer.write(".dark-mode table, .dark-mode th, .dark-mode td { color: #f8f9fa; }\n");
        writer.write(".light-mode table, .light-mode th, .light-mode td { color: #212529; }\n");

        // Right align the toggle button
        writer.write("#toggleButton { float: right; }\n");
        writer.write("hr { border: 0; height: 1px; background-color: #6c757d; }\n");
        writer.write(".dark-mode hr { background-color: #f8f9fa; }\n");
        writer.write("</style>\n");

        writer.write("<title>WAM Automation Test Case Execution Report</title>\n");
        writer.write("</head>\n<body class='container-fluid my-4 light-mode'>\n");

        // Add dark mode toggle button
        writer.write(
            "<button id='toggleButton' class='btn'>Dark Mode</button>\n <br> <br> <br> \n");

        // Add JavaScript to toggle dark mode
        writer.write("<script>\n");
        writer.write(
            "document.getElementById('toggleButton').addEventListener('click', function() {\n");
        writer.write("  var body = document.body;\n");
        writer.write("  if (body.classList.contains('light-mode')) {\n");
        writer.write("    body.classList.remove('light-mode');\n");
        writer.write("    body.classList.add('dark-mode');\n");
        writer.write("  } else {\n");
        writer.write("    body.classList.remove('dark-mode');\n");
        writer.write("    body.classList.add('light-mode');\n");
        writer.write("  }\n");
        writer.write("});\n");
        writer.write("</script>\n");

        writer.write(
            "<h1><img src='images/logo.png' alt='WAM Logo' height='50'> WAM Automation Test Case Execution Report</h1>\n");
        writer.write("<h3>Execution Id: " + executionId + "</h3><hr>\n");
      }
    } catch (final IOException e) {
      log.error("An error occurred: {}", e.getMessage());
    }
    return filePath;
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
    } catch (final IOException e) {
      log.error("An error occurred: {}", e.getMessage());
    }
  }

  public static void appendTestCaseDetails(final String fileName, final TestCaseDto testCaseDto) {
    try {
      try (final BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
        writer.write("<section>\n<h2>Test Case Details</h2>\n");
        writer.write("<p><strong>Test Case Id:</strong> " + testCaseDto.getId() + "</p>\n");
        writer.write(
            "<p><strong>Execution Order:</strong> " + testCaseDto.getExecutionOrder() + "</p>\n");
        writer.write(
            "<p><strong>Test Case Name:</strong> " + testCaseDto.getTestCaseName() + "</p>\n");
        writer.write(
            "<p><strong>Description:</strong> "
                + testCaseDto.getDescription()
                + "</p>\n</section>\n");
      }
    } catch (final IOException e) {
      log.error("An error occurred: {}", e.getMessage());
    }
  }

  public static void createTestCaseStepDetailsTable(final String fileName) {
    try {
      // Generate a unique identifier by appending the current time (milliseconds) and a random
      // number
      String uniqueId =
          "table_"
              + System.currentTimeMillis()
              + "_"
              + (int) (Math.random() * 1000); // Random number between 0 and 999

      try (final BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
        writer.write("<section>\n<h2>Test Case Steps Details</h2>\n");

        // Add the search bar with a unique ID
        writer.write(
            "<input class='form-control mb-2' id='searchInput_"
                + uniqueId
                + "' type='text' placeholder='Search...'>\n");

        // JavaScript for filtering table rows based on search input
        writer.write("<script>\n");
        writer.write(
            "document.getElementById('searchInput_"
                + uniqueId
                + "').addEventListener('keyup', function() {\n");
        writer.write("let filter = this.value.toUpperCase();\n");
        writer.write("let rows = document.querySelectorAll('#" + uniqueId + " tbody tr');\n");
        writer.write("rows.forEach(row => {\n");
        writer.write("let match = row.textContent.toUpperCase().includes(filter);\n");
        writer.write("row.style.display = match ? '' : 'none';\n");
        writer.write("});\n");
        writer.write("});\n</script>\n");

        // Start the table with a unique ID
        writer.write(
            "<div class='table-responsive'>\n<table id='"
                + uniqueId
                + "' class='table table-striped table-hover'>\n");
        writer.write("<thead class='thead-dark'><tr>\n");
        writer.write(
            "<th>Start Time</th><th>End Time</th><th>TCS Id</th><th>Execution Order</th><th>TCS Type</th>\n");
        writer.write(
            "<th>TCS Name</th><th>Expected Result</th><th>Actual Result</th><th>Status</th>\n");
        writer.write("</tr></thead>\n<tbody>\n");
      }
    } catch (final IOException e) {
      log.error("An error occurred: {}", e.getMessage());
    }
  }

  public static void appendTestCaseStepDetails(
      final String fileName, TestCaseStepExecutionDto testCaseStepExecutionDto) {
    try {
      try (final BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
        writer.write("<tr>\n<td>" + testCaseStepExecutionDto.getStartTime() + "</td>\n");
        writer.write("<td>" + testCaseStepExecutionDto.getEndTime() + "</td>\n");
        writer.write("<td>TCS-" + testCaseStepExecutionDto.getId() + "</td>\n");
        writer.write("<td>" + testCaseStepExecutionDto.getExecutionOrder() + "</td>\n");
        writer.write("<td>" + testCaseStepExecutionDto.getTestCaseStepType() + "</td>\n");
        writer.write("<td>" + testCaseStepExecutionDto.getTestCaseStepName() + "</td>\n");
        writer.write("<td>" + testCaseStepExecutionDto.getExpectedResult() + "</td>\n");
        writer.write("<td>" + testCaseStepExecutionDto.getActualResult() + "</td>\n");
        writer.write("<td>" + testCaseStepExecutionDto.getStatus() + "</td>\n</tr>\n");
      }
    } catch (final IOException e) {
      log.error("An error occurred: {}", e.getMessage());
    }
  }

  public static void endTestCaseStepDetailsTable(final String fileName) {
    try {
      try (final BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
        writer.write("</tbody>\n</table>\n</div>\n</section>\n");
      }
    } catch (final IOException e) {
      log.error("An error occurred: {}", e.getMessage());
    }
  }

  public static void appendTestCaseStepExecutionSummary(
      final String fileName, TestCaseStepExecutionSummaryDto summaryDto) {
    try {
      try (final BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
        writer.write("<section>\n<h2>Test Case Step Execution Summary</h2>\n");
        writer.write(
            "<p><strong>Total Test Case Steps:</strong> "
                + summaryDto.getTotalTestCaseSteps()
                + "</p>\n");
        writer.write(
            "<p><strong>Passed:</strong> " + summaryDto.getPassedTestCaseSteps() + "</p>\n");
        writer.write(
            "<p><strong>Failed:</strong> "
                + summaryDto.getFailedTestCaseSteps()
                + "</p>\n</section>\n");
      }
    } catch (final IOException e) {
      log.error("An error occurred: {}", e.getMessage());
    }
  }

  public static void appendFileDetails(final String fileName, final FileDetailsDto fileDetailsDto) {
    try {
      if (Objects.isNull(fileDetailsDto)
          || Objects.isNull(fileDetailsDto.getFileNameList())
          || fileDetailsDto.getFileNameList().isEmpty()) {
        log.error("FileDetailsDto or its fileNameList is null. Unable to append file details.");
        return;
      }
      try (final BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
        writer.write("<section>\n<h2>Log Files</h2>\n");
        writer.write(
            "<table class='table table-bordered'>\n<thead><tr><th>File</th></tr></thead>\n<tbody>\n");
        for (String file : fileDetailsDto.getFileNameList()) {
          writer.write("<tr><td>" + file + "</td></tr>\n");
        }
        writer.write("</tbody>\n</table>\n</section><hr>\n");
      }
    } catch (final IOException e) {
      log.error("An error occurred: {}", e.getMessage());
    }
  }

  public static void appendTestCaseExecutionSummary(
      final String fileName, final TestCaseExecutionSummaryDto summaryDto) {
    try {
      try (final BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
        writer.write("<section class='jumbotron'>\n<h2>Test Case Execution Summary</h2>\n");
        writer.write(
            "<p><strong>Total Test Cases:</strong> " + summaryDto.getTotalTestCases() + "</p>\n");
        /*  writer.write(
            "<p><strong>Passed:</strong> " + summaryDto.getPassedTestCaseSteps() + "</p>\n");
        writer.write(
            "<p><strong>Failed:</strong> "
                + summaryDto.getFailedTestCaseSteps()
                + "</p>\n</section>\n");*/
      }
    } catch (final IOException e) {
      log.error("An error occurred: {}", e.getMessage());
    }
  }

  public static void completeReportGeneration(final String fileName) {
    try {
      try (final BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
        writer.write("</body>\n</html>\n");
      }
    } catch (final IOException e) {
      log.error("An error occurred: {}", e.getMessage());
    }
  }
}
