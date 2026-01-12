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

package wam.automationtool.domain.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import wam.automationtool.domain.entity.testcase.TestCase;
import wam.automationtool.domain.entity.testcasestep.TestCaseStep;

import java.util.List;
import java.util.Optional;

@Repository
public interface TestCaseStepRepository extends JpaRepository<TestCaseStep, Long> {

  @EntityGraph(attributePaths = {"preferenceParameters", "preferenceParameters.preferenceParameterType", "assertParameters"})
  List<TestCaseStep> findByTestCaseAndIsDeletedOrderByExecutionOrder(
      TestCase testCase, boolean isDeleted);

  Optional<TestCaseStep> findByIdAndIsDeletedOrderByExecutionOrder(Long id, boolean isDeleted);

  @EntityGraph(attributePaths = {"preferenceParameters", "preferenceParameters.preferenceParameterType", "assertParameters"})
    List<TestCaseStep> findByIsDeleted(boolean isDeleted);

    Optional<TestCaseStep> findByTestCaseStepTypeAndIsDeleted(String testCaseStepType, boolean isDeleted);

    List<TestCaseStep> findByIdInAndIsDeleted(List<Long> testCaseStepIds, boolean isDeleted);
}
