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

package wam.automationtool.domain.entity.testcasestep;

import lombok.Getter;

@Getter
public enum TestCaseStepType {
  A_SEND_HTTP_REQUEST("A_SEND_HTTP_REQUEST"),
  A_LOG_FILE_READING_START("A_LOG_FILE_READING_START"),
  A_LOG_FILE_READING_END("A_LOG_FILE_READING_END"),
  A_LOG_FILE_LINE_READ("A_LOG_FILE_READ"),
  A_LOG_FILE_EXTRACT("A_LOG_FILE_EXTRACT"),
  A_LOG_FILE_DELETE("A_LOG_FILE_DELETE"),
  A_ADD_CACHE_ITEM("A_ADD_CACHE_ITEM"),
  A_REMOVE_EXECUTION_CACHE("A_REMOVE_EXECUTION_CACHE"),
  A_REMOVE_CACHE_ITEM("A_REMOVE_CACHE_ITEM"),
  A_REMOVE_ALL_EXECUTION_CACHE("A_REMOVE_ALL_EXECUTION_CACHE"),
  A_WAIT("A_WAIT"),
  A_MYSQL_DB_VERIFICATION("A_MYSQL_DB_VERIFICATION"),
  W_OPEN_BROWSER("W_OPEN_BROWSER"),
  W_CLOSE_BROWSER("W_CLOSE_BROWSER"),
  W_ELEMENT_VALUE_INPUT("W_ELEMENT_VALUE_INPUT"),
  W_ELEMENT_CLICK("W_ELEMENT_CLICK"),
  M_OPEN_APP("M_OPEN_APP");

  private final String id;

  TestCaseStepType(final String id) {
    this.id = id;
  }

  public static boolean isNotExist(final String testCaseStepTypeId) {
    for (final TestCaseStepType type : values()) {
      if (type.getId().equals(testCaseStepTypeId)) {
        return false;
      }
    }
    return true;
  }
}
