package wam.automationtool.domain.service;

import wam.automationtool.domain.entity.testcasestep.parameter.PreferenceParameter;
import wam.automationtool.domain.repository.PreferenceParameterRepository;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PreferenceParameterDomainService {

    private final PreferenceParameterRepository preferenceParameterRepository;

    @Autowired
    public PreferenceParameterDomainService(final PreferenceParameterRepository preferenceParameterRepository) {
        this.preferenceParameterRepository = preferenceParameterRepository;
    }

    /**
     * Adds a new PreferenceParameter to the repository.
     *
     * @param preferenceParameter The PreferenceParameter entity to add.
     */
    public void add(final PreferenceParameter preferenceParameter) {
        preferenceParameterRepository.save(preferenceParameter);
    }

    /**
     * Adds a new PreferenceParameter to the repository.
     *
     * @param preferenceParameter The PreferenceParameter entity to add.
     */
    public void addAll(final List<PreferenceParameter> preferenceParameterList) {
        preferenceParameterRepository.saveAll(preferenceParameterList);
    }

    /**
     * Updates an existing PreferenceParameter in the repository.
     *
     * @param preferenceParameter The PreferenceParameter entity to update.
     */
    public void update(final PreferenceParameter preferenceParameter) {
        preferenceParameterRepository.save(preferenceParameter);
    }

    /**
     * Deletes a PreferenceParameter from the repository.
     *
     * @param preferenceParameter The PreferenceParameter entity to delete.
     */
    public void delete(final PreferenceParameter preferenceParameter) {
        preferenceParameterRepository.delete(preferenceParameter);
    }

    /**
     * Finds a PreferenceParameter by its ID.
     *
     * @param id The ID of the PreferenceParameter to find.
     * @return An Optional containing the found PreferenceParameter, or empty if not found.
     */
    public Optional<PreferenceParameter> findById(final Long id) {
        return preferenceParameterRepository.findByIdAndIsDeleted(id, false);
    }

    /**
     * Finds PreferenceParameters by a list of IDs.
     *
     * @param preferenceParameters A list of PreferenceParameter entities to find by IDs.
     * @return A List containing the found PreferenceParameters.
     */
    public List<PreferenceParameter> findByPreferenceParameter(final List<PreferenceParameter> preferenceParameters) {
        final List<Long> preferenceParameterIdList = preferenceParameters.stream()
                .map(PreferenceParameter::getId)
                .filter(Objects::nonNull)
                .toList();
        return preferenceParameterRepository.findByIdInAndIsDeleted(preferenceParameterIdList, false);
    }

    /**
     * Finds all PreferenceParameters that are not marked as deleted.
     *
     * @return A list of all active PreferenceParameters.
     */
    public List<PreferenceParameter> findAll() {
        return preferenceParameterRepository.findByIsDeleted(false);
    }

    /**
     * Finds PreferenceParameters by the associated TestCaseStep ID.
     *
     * @param testCaseStepId The ID of the TestCaseStep.
     * @return A list of PreferenceParameters associated with the given TestCaseStep.
     */
    public List<PreferenceParameter> findByTestCaseStepId(final Long testCaseStepId) {
        return preferenceParameterRepository.findByTestCaseStepIdAndIsDeleted(testCaseStepId, false);
    }

}
