import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class LogFileProcessor {

  public static void main(String[] args) {
    //Scanner scanner = new Scanner(System.in);

    // Configurable file paths
    System.out.print("Enter the path of the source log file: ");
    String sourceFilePath =
        "C:\\Users\\BuddhikaAlwis\\IdeaProjects\\wam-automation-tool\\wam-automation-engine\\PackageModel.log";

    System.out.print("Enter the path of the destination log file: ");
    String destinationFilePath = "C:\\Users\\BuddhikaAlwis\\Desktop\\grafana-test\\loki\\PackageModel.log";

    //scanner.close();

    processLogFiles(sourceFilePath, destinationFilePath);
  }

  public static void processLogFiles(String sourceFilePath, String destinationFilePath) {
    // Try-with-resources to handle file opening and closing automatically
    try (BufferedReader reader = new BufferedReader(new FileReader(sourceFilePath));
        BufferedWriter writer = new BufferedWriter(new FileWriter(destinationFilePath))) {

      String line;
      while ((line = reader.readLine()) != null) {
        // Write the line to the destination file immediately
        writer.write(line);
        writer.newLine(); // Write a newline character
        writer.flush(); // Ensure data is written immediately
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }

      System.out.println("Log file processing completed successfully.");
    } catch (IOException e) {
      System.err.println("Error processing log files: " + e.getMessage());
    }
  }
}
