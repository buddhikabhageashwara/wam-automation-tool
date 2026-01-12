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

package wam.automationtool.application.config;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static wam.automationtool.application.config.AppConstant.AuthConstants.INVALID_TEST_CASE_STEP_TYPE_CODE;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wam.automationtool.application.exception.InvalidTestCaseStepTypeException;
import wam.automationtool.application.impl.testcasestep.execute.TestCaseStepExecutor;
import wam.automationtool.domain.entity.testcasestep.TestCaseStepType;

@Slf4j
@Service
public class TestCaseStepExecutorFactory {

  private final Map<TestCaseStepType, TestCaseStepExecutor> testCaseStepTypeExecutorMap;

  @Autowired
  public TestCaseStepExecutorFactory(List<TestCaseStepExecutor> executors) {
    this.testCaseStepTypeExecutorMap = new HashMap<>();
    executors.forEach(executor -> this.testCaseStepTypeExecutorMap.put(executor.getTestCaseStepType(), executor));
  }

  public TestCaseStepExecutor getTestCaseStepExecutor(final TestCaseStepType testCaseStepType) {
    final TestCaseStepExecutor executor = testCaseStepTypeExecutorMap.get(testCaseStepType);
    if (Objects.isNull(executor)) {
      throw new InvalidTestCaseStepTypeException(
              BAD_REQUEST, INVALID_TEST_CASE_STEP_TYPE_CODE, "error.invalid.test.case.step.type");
    }
    return executor;
  }
}

