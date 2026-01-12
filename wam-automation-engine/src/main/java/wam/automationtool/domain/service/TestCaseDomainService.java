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

package wam.automationtool.domain.service;

import wam.automationtool.domain.entity.testcase.TestCase;
import wam.automationtool.domain.entity.testplan.TestPlan;
import wam.automationtool.domain.repository.TestCaseRepository;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TestCaseDomainService {

    private final TestCaseRepository testCaseRepository;

    @Autowired
    public TestCaseDomainService(final TestCaseRepository testCaseRepository) {
        this.testCaseRepository = testCaseRepository;
    }

    /**
     * Adds a new TestCase to the repository.
     *
     * @param testCase The TestCase entity to add.
     */
    public void add(final TestCase testCase) {
        testCaseRepository.save(testCase);
    }

    /**
     * Updates an existing TestCase in the repository.
     *
     * @param testCase The TestCase entity to update.
     */
    public void update(final TestCase testCase) {
        testCaseRepository.save(testCase);
    }

    /**
     * Deletes a TestCase from the repository.
     *
     * @param testCase The TestCase entity to delete.
     */
    public void delete(final TestCase testCase) {
        testCase.setIsDeleted(true);
        testCaseRepository.save(testCase);
    }

    /**
     * Finds a TestCase by its ID.
     *
     * @param id The ID of the TestCase to find.
     * @return An Optional containing the found TestCase, or empty if not found.
     */
    public Optional<TestCase> findById(final Long id) {
        return testCaseRepository.findByIdAndIsDeleted(id, false);
    }

    /**
     * Finds a list of TestCases associated with a specific TestPlan.
     *
     * @param testPlan The TestPlan entity to find TestCases for.
     * @return A List containing the TestCases associated with the specified TestPlan.
     */
    public List<TestCase> findByTestPlan(final TestPlan testPlan) {
        return testCaseRepository.findByTestPlanAndIsDeletedOrderByExecutionOrder(testPlan, false);
    }

    /**
     * Finds a list of TestCases by a list of TestCase entities.
     *
     * @param testCases A list of TestCase entities to look up by their IDs.
     * @return A List containing the found TestCases.
     */
    public List<TestCase> findByTestCases(final List<TestCase> testCases) {
        final List<Long> testCaseIdList = testCases.stream()
                .map(TestCase::getId)
                .filter(Objects::nonNull)
                .toList();
        return testCaseRepository.findByIdInAndIsDeleted(testCaseIdList, false);
    }

    /**
     * Finds all TestCases that are not marked as deleted.
     *
     * @return A list of all active TestCases.
     */
    public List<TestCase> findAll() {
        return testCaseRepository.findByIsDeletedOrderByExecutionOrder(false);
    }

    /**
     * Finds a TestCase by its testCaseName.
     *
     * @param testCaseName The name of the TestCase to find.
     * @return An Optional containing the found TestCase, or empty if not found.
     */
    public Optional<TestCase> findByTestCaseName(final String testCaseName) {
        return testCaseRepository.findByTestCaseNameAndIsDeleted(testCaseName, false);
    }
}
