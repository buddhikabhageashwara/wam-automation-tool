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
