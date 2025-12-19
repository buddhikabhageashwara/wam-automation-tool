package wam.automationtool.application.config.seleniumwebdriver;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

@Component
public class SeleniumServerRunner implements CommandLineRunner {

    private static final String SELENIUM_JAR_NAME = "selenium-server-4.19.0.jar";
    private static final int SELENIUM_PORT = 4444;
    private static final String SELENIUM_STATUS_URL = "http://localhost:4444/status";

    @Override
    public void run(String... args) {
        if (isSeleniumServerRunning()) {
            System.out.println("✅ Selenium Server is already running. Using the existing instance.");
        } else {
            System.out.println("⏳ Selenium Server is not running. Starting a new instance...");
            startSeleniumServer();
        }
    }

    private void startSeleniumServer() {
        String workingDirectory = System.getProperty("user.dir");
        String seleniumJarPath = workingDirectory + File.separator + SELENIUM_JAR_NAME;

        File seleniumJarFile = new File(seleniumJarPath);
        if (!seleniumJarFile.exists()) {
            System.err.println("❌ Selenium server JAR not found at: " + seleniumJarPath);
            return;
        }

        String command = "java -jar " + seleniumJarPath + " standalone";
        ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
        processBuilder.inheritIO();

        try {
            Process process = processBuilder.start();
            System.out.println("✅ Selenium Server started successfully.");
        } catch (IOException e) {
            System.err.println("❌ Failed to start Selenium Server: " + e.getMessage());
        }
    }

    private boolean isSeleniumServerRunning() {
        try {
            URL url = new URL(SELENIUM_STATUS_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                Scanner scanner = new Scanner(connection.getInputStream());
                StringBuilder response = new StringBuilder();
                while (scanner.hasNext()) {
                    response.append(scanner.nextLine());
                }
                scanner.close();

                System.out.println("✅ Selenium Server is already running: " + response);
                return true;
            }
        } catch (IOException e) {
            System.out.println("⚠ Selenium Server is not running.");
        }
        return false;
    }
}




