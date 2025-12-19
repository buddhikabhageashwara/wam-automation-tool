package wam.automationtool.application.util;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import java.net.MalformedURLException;
import java.net.URL;

public class PwaTest {

  public static void main(String[] args) throws MalformedURLException {
    testAndroidApp();
  }

  public static void testAndroidApp() throws MalformedURLException {

    AppiumDriverLocalService service = AppiumDriverLocalService.buildDefaultService();
    service.start();
    System.out.println("server started..");
    try {
      // Set options for the emulator
      UiAutomator2Options options =
          new UiAutomator2Options()
              .setUdid("emulator-5554") // Emulator ID
              .setDeviceName("sdk_gphone64_x86_64") // Emulator name
              .setPlatformName("Android")
              .setPlatformVersion("15") // Set your platform version
              .setAppPackage("com.android.chrome")
              .setAppActivity("com.google.android.apps.chrome.Main")
              .setAutomationName("uiautomator2"); // Specify Chrome as the browser

      // Initialize the Android Driver
      AndroidDriver driver = new AndroidDriver(new URL("http://127.0.0.1:4723"), options);

      try {
        driver.get("https://www.google.com");
        try {
          Thread.sleep(10000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      } finally {
        // Quit the driver
        driver.quit();
      }
    } finally {
      // Stop Appium service
      service.stop();
    }
  }
}
