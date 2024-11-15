package wam.automationtool.domain.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import wam.automationtool.domain.entity.testcasestep.parameter.PreferenceParameterType;

@Repository
public interface PreferenceParameterTypeRepository extends JpaRepository<PreferenceParameterType, Long> {

    Optional<PreferenceParameterType> findByIdAndIsDeleted(Long id, boolean isDeleted);

    List<PreferenceParameterType> findByIsDeleted(boolean isDeleted);

    Optional<PreferenceParameterType> findByParameterNameAndIsDeleted(String parameterName, boolean isDeleted);
}

