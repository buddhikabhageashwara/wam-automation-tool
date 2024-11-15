package wam.automationtool.domain.repository;

import wam.automationtool.domain.entity.testcasestep.parameter.AssertParameter;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssertParameterRepository extends JpaRepository<AssertParameter, Long> {

    Optional<AssertParameter> findByIdAndIsDeleted(Long id, boolean isDeleted);

    List<AssertParameter> findByIsDeleted(boolean isDeleted);

    List<AssertParameter> findByTestCaseStepIdAndIsDeleted(Long testCaseStepId, boolean isDeleted);

    List<AssertParameter> findByIdInAndIsDeleted(List<Long> assertParameterIds, boolean isDeleted);
}
