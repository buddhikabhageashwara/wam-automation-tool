package wam.automationtool.domain.repository;

import wam.automationtool.domain.entity.testplan.TestPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TestPlanRepository extends JpaRepository<TestPlan, Long> {

  Optional<TestPlan> findByIdAndIsDeleted(Long id, boolean isDeleted);

    List<TestPlan> findByIsDeleted(boolean isDeleted);

    Optional<TestPlan> findByTestPlanNameAndIsDeleted(String testPlanName, boolean isDeleted);

    List<TestPlan> findByIdInAndIsDeleted(List<Long> testPlanIds, boolean isDeleted);

    List<TestPlan> findByIsDeletedOrderByIdDesc(boolean isDeleted);
}
