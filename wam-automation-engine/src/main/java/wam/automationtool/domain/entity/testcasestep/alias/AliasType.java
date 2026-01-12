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

package wam.automationtool.domain.entity.testcasestep.alias;

import lombok.Getter;

@Getter
public enum AliasType {

  AGENT_EXECUTION("AGENT_EXECUTION"),
  SERVER_LOG("SERVER_LOG"),
  CACHED_DATA("CACHED_DATA"),
  MYSQL_DB_CONNECTION("MYSQL_DB_CONNECTION"),
  MONGO_DB_CONNECTION("MONGO_DB_CONNECTION");

  private final String id;

  AliasType(final String id) {
    this.id = id;
  }

  public static boolean isNotExist(final String aliasTypeId) {
    for (final AliasType type : values()) {
      if (type.getId().equals(aliasTypeId)) {
        return false;
      }
    }
    return true;
  }

}
