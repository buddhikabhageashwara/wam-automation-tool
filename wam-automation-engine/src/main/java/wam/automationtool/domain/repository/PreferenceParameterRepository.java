package wam.automationtool.domain.repository;

import wam.automationtool.domain.entity.testcasestep.parameter.PreferenceParameter;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PreferenceParameterRepository extends JpaRepository<PreferenceParameter, Long> {

    Optional<PreferenceParameter> findByIdAndIsDeleted(Long id, boolean isDeleted);
    List<PreferenceParameter> findByIsDeleted(boolean isDeleted);
    List<PreferenceParameter> findByTestCaseStepIdAndIsDeleted(Long testCaseStepId, boolean isDeleted);
    List<PreferenceParameter> findByIdInAndIsDeleted(List<Long> preferenceParameterIds, boolean isDeleted);

}
