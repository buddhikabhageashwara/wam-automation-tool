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

import lombok.Getter;

@Getter
public class OSInfo {

  private final String osName;

  private OSInfo(final String osName) {
    this.osName = osName == null ? "" : osName.toLowerCase();
  }

  public static OSInfo detect() {
    return new OSInfo(System.getProperty("os.name"));
  }

  public boolean isWindows() {
    return osName.contains("win");
  }

  public boolean isLinux() {
    return osName.contains("linux");
  }

  public boolean isMac() {
    return osName.contains("mac");
  }

  public String driverExeSuffix() {
    return isWindows() ? ".exe" : "";
  }

  public String pathSeparator() {
    return isWindows() ? ";" : ":";
  }
}
