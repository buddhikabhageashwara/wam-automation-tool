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
