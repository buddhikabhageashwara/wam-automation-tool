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

import wam.automationtool.domain.entity.testcasestep.alias.parameter.AliasParameter;
import wam.automationtool.domain.repository.AliasParameterRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AliasParameterDomainService {

    private final AliasParameterRepository aliasParameterRepository;

    @Autowired
    public AliasParameterDomainService(final AliasParameterRepository aliasParameterRepository) {
        this.aliasParameterRepository = aliasParameterRepository;
    }

    /**
     * Adds a new AliasParameter to the repository.
     *
     * @param aliasParameter The AliasParameter entity to add.
     */
    public void add(final AliasParameter aliasParameter) {
        aliasParameterRepository.save(aliasParameter);
    }

    public void addAll(final List<AliasParameter> aliasParameterList) {
        aliasParameterRepository.saveAll(aliasParameterList);
    }

    /**
     * Updates an existing AliasParameter in the repository.
     *
     * @param aliasParameter The AliasParameter entity to update.
     */
    public void update(final AliasParameter aliasParameter) {
        aliasParameterRepository.save(aliasParameter);
    }

    /**
     * Deletes an AliasParameter from the repository.
     *
     * @param aliasParameter The AliasParameter entity to delete.
     */
    public void delete(final AliasParameter aliasParameter) {
        aliasParameterRepository.delete(aliasParameter);
    }

    /**
     * Finds an AliasParameter by its ID.
     *
     * @param id The ID of the AliasParameter to find.
     * @return An Optional containing the found AliasParameter, or empty if not found.
     */
    public Optional<AliasParameter> findById(final Long id) {
        return aliasParameterRepository.findByIdAndIsDeleted(id, false);
    }

    /**
     * Finds all AliasParameters that are not marked as deleted.
     *
     * @return A list of all active AliasParameters.
     */
    public List<AliasParameter> findAll() {
        return aliasParameterRepository.findByIsDeleted(false);
    }

    /**
     * Finds AliasParameters by their Alias ID.
     *
     * @param aliasId The ID of the Alias.
     * @return A list of AliasParameters associated with the given Alias ID.
     */
    public List<AliasParameter> findByAliasId(final Long aliasId) {
        return aliasParameterRepository.findByAliasIdAndIsDeleted(aliasId, false);
    }

    /**
     * Finds multiple AliasParameters by their IDs.
     *
     * @param aliasParameterIds A list of AliasParameter IDs to find.
     * @return A list of found AliasParameters.
     */
    public List<AliasParameter> findByIds(final List<Long> aliasParameterIds) {
        return aliasParameterRepository.findByIdInAndIsDeleted(aliasParameterIds, false);
    }
}
