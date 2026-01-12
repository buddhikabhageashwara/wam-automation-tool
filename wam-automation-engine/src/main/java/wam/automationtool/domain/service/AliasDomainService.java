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

import wam.automationtool.domain.entity.testcasestep.alias.Alias;
import wam.automationtool.domain.repository.AliasRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class AliasDomainService {

    private final AliasRepository aliasRepository;

    @Autowired
    public AliasDomainService(final AliasRepository aliasRepository) {
        this.aliasRepository = aliasRepository;
    }

    /**
     * Adds a new Alias to the repository.
     *
     * @param alias The Alias entity to add.
     */
    public Alias add(final Alias alias) {
        return aliasRepository.saveAndFlush(alias);
    }

    /**
     * Updates an existing Alias in the repository.
     *
     * @param alias The Alias entity to update.
     */
    public void update(final Alias alias) {
        aliasRepository.save(alias);
    }

    /**
     * Deletes an Alias from the repository.
     *
     * @param alias The Alias entity to delete.
     */
    public void delete(final Alias alias) {
        alias.setIsDeleted(true);
        aliasRepository.save(alias);
    }

    /**
     * Finds an Alias by its ID.
     *
     * @param id The ID of the Alias to find.
     * @return An Optional containing the found Alias, or empty if not found.
     */
    public Optional<Alias> findById(final Long id) {
        return aliasRepository.findByIdAndIsDeleted(id, false);
    }

    /**
     * Finds all Aliases that are not marked as deleted.
     *
     * @return A list of all active Aliases.
     */
    public List<Alias> findAll() {
        return aliasRepository.findByIsDeleted(false);
    }

    /**
     * Finds Aliases by their types that are not marked as deleted.
     *
     * @param aliasType The type of the Alias to find.
     * @return A list of Aliases of the specified type.
     */
    public List<Alias> findByAliasType(final String aliasType) {
        return aliasRepository.findByAliasTypeAndIsDeleted(aliasType, false);
    }

    public Optional<Alias> findByAliasName(final String aliasName) {
        return aliasRepository.findByAliasNameAndIsDeleted(aliasName, false);
    }

    /**
     * Finds multiple Aliases by their IDs that are not marked as deleted.
     *
     * @param aliases The list of Alias entities to find.
     * @return A List containing the found Aliases.
     */
    public List<Alias> findByAliasList(final List<Alias> aliases) {
        final List<Long> aliasIdList = aliases.stream()
                .map(Alias::getId)
                .filter(Objects::nonNull)
                .toList();
        return aliasRepository.findByIdInAndIsDeleted(aliasIdList, false);
    }
}
