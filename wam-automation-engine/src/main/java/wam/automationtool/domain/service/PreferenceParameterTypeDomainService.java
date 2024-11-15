package wam.automationtool.domain.service;

import wam.automationtool.domain.entity.testcasestep.parameter.PreferenceParameterType;
import wam.automationtool.domain.repository.PreferenceParameterTypeRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PreferenceParameterTypeDomainService {

    private final PreferenceParameterTypeRepository preferenceParameterTypeRepository;

    @Autowired
    public PreferenceParameterTypeDomainService(final PreferenceParameterTypeRepository preferenceParameterTypeRepository) {
        this.preferenceParameterTypeRepository = preferenceParameterTypeRepository;
    }

    public void add(final PreferenceParameterType preferenceParameterType) {
        preferenceParameterTypeRepository.save(preferenceParameterType);
    }

    public void update(final PreferenceParameterType preferenceParameterType) {
        preferenceParameterTypeRepository.save(preferenceParameterType);
    }

    public void delete(final PreferenceParameterType preferenceParameterType) {
        preferenceParameterType.setIsDeleted(true);
        preferenceParameterTypeRepository.save(preferenceParameterType);
    }

    public Optional<PreferenceParameterType> findById(final Long id) {
        return preferenceParameterTypeRepository.findByIdAndIsDeleted(id, false);
    }

    public List<PreferenceParameterType> findAll() {
        return preferenceParameterTypeRepository.findByIsDeleted(false);
    }

    public Optional<PreferenceParameterType> findByParameterName(final String parameterName) {
        return preferenceParameterTypeRepository.findByParameterNameAndIsDeleted(parameterName, false);
    }
}
