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

import wam.automationtool.domain.entity.testplan.TestPlan;
import wam.automationtool.domain.repository.TestPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class TestPlanDomainService {

    private final TestPlanRepository testPlanRepository;

    @Autowired
    public TestPlanDomainService(final TestPlanRepository testPlanRepository) {
        this.testPlanRepository = testPlanRepository;
    }

    /**
     * Adds a new TestPlan to the repository.
     *
     * @param testPlan The TestPlan entity to add.
     */
    public void add(final TestPlan testPlan) {
        testPlanRepository.save(testPlan);
    }

    /**
     * Updates an existing TestPlan in the repository.
     *
     * @param testPlan The TestPlan entity to update.
     */
    public void update(final TestPlan testPlan) {
        testPlanRepository.save(testPlan);
    }

    /**
     * Deletes a TestPlan from the repository.
     *
     * @param testPlan The TestPlan entity to delete.
     */
    public void delete(final TestPlan testPlan) {
        testPlan.setIsDeleted(true);
        testPlanRepository.save(testPlan);
    }

    /**
     * Finds a TestPlan by its ID.
     *
     * @param id The ID of the TestPlan to find.
     * @return An Optional containing the found TestPlan, or empty if not found.
     */
    public Optional<TestPlan> findById(final Long id) {
    return testPlanRepository.findByIdAndIsDeleted(id, false);
    }

    /**
     * Finds TestPlans by a list of IDs.
     *
     * @param testPlans A list of TestPlan entities to find.
     * @return A List containing the found TestPlans.
     */
    public List<TestPlan> findByTestPlan(final List<TestPlan> testPlans) {
        final List<Long> testPlanIds = testPlans.stream()
                .map(TestPlan::getId)
                .filter(Objects::nonNull)
                .toList();
        return testPlanRepository.findByIdInAndIsDeleted(testPlanIds, false);
    }

    /**
     * Finds all TestPlans that are not marked as deleted.
     *
     * @return A list of all active TestPlans.
     */
    public List<TestPlan> findAll() {
        return testPlanRepository.findByIsDeleted(false);
    }

    public List<TestPlan> findAllByIdDesc() {
        return testPlanRepository.findByIsDeletedOrderByIdDesc(false);
    }

    /**
     * Finds a TestPlan by its name.
     *
     * @param testPlanName The name of the TestPlan to find.
     * @return An Optional containing the found TestPlan, or empty if not found.
     */
    public Optional<TestPlan> findByTestPlanName(final String testPlanName) {
        return testPlanRepository.findByTestPlanNameAndIsDeleted(testPlanName, false);
    }
}
