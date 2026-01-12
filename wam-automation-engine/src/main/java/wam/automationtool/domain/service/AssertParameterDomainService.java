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

import wam.automationtool.domain.entity.testcasestep.parameter.AssertParameter;
import wam.automationtool.domain.repository.AssertParameterRepository;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AssertParameterDomainService {

    private final AssertParameterRepository assertParameterRepository;

    @Autowired
    public AssertParameterDomainService(final AssertParameterRepository assertParameterRepository) {
        this.assertParameterRepository = assertParameterRepository;
    }

    /**
     * Adds a new AssertParameter to the repository.
     *
     * @param assertParameter The AssertParameter entity to add.
     */
    public void add(final AssertParameter assertParameter) {
        assertParameterRepository.save(assertParameter);
    }

    public void addAll(final List<AssertParameter> assertParameterList) {
        assertParameterRepository.saveAll(assertParameterList);
    }

    /**
     * Updates an existing AssertParameter in the repository.
     *
     * @param assertParameter The AssertParameter entity to update.
     */
    public void update(final AssertParameter assertParameter) {
        assertParameterRepository.save(assertParameter);
    }

    /**
     * Deletes an AssertParameter from the repository.
     *
     * @param assertParameter The AssertParameter entity to delete.
     */
    public void delete(final AssertParameter assertParameter) {
        assertParameterRepository.delete(assertParameter);
    }

    /**
     * Finds an AssertParameter by its ID.
     *
     * @param id The ID of the AssertParameter to find.
     * @return An Optional containing the found AssertParameter, or empty if not found.
     */
    public Optional<AssertParameter> findById(final Long id) {
        return assertParameterRepository.findByIdAndIsDeleted(id, false);
    }

    /**
     * Finds AssertParameters by their associated TestCaseStep ID.
     *
     * @param testCaseStepId The ID of the TestCaseStep to find associated AssertParameters for.
     * @return A List of AssertParameters associated with the given TestCaseStep.
     */
    public List<AssertParameter> findByTestCaseStepId(final Long testCaseStepId) {
        return assertParameterRepository.findByTestCaseStepIdAndIsDeleted(testCaseStepId, false);
    }

    /**
     * Finds all AssertParameters that are not marked as deleted.
     *
     * @return A list of all active AssertParameters.
     */
    public List<AssertParameter> findAll() {
        return assertParameterRepository.findByIsDeleted(false);
    }

    /**
     * Finds a list of AssertParameters by their IDs.
     *
     * @param assertParameters The list of AssertParameter entities.
     * @return A List containing the found AssertParameters.
     */
    public List<AssertParameter> findByIdIn(final List<AssertParameter> assertParameters) {
        final List<Long> assertParameterIds = assertParameters.stream()
                .map(AssertParameter::getId)
                .filter(Objects::nonNull)
                .toList();
        return assertParameterRepository.findByIdInAndIsDeleted(assertParameterIds, false);
    }
}
