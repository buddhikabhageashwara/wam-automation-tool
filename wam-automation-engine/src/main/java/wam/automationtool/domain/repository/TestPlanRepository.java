package wam.automationtool.domain.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import wam.automationtool.domain.entity.testplan.TestPlan;

@Repository
public interface TestPlanRepository extends JpaRepository<TestPlan, Long> {

  @EntityGraph(attributePaths = {"testCases"})
  Optional<TestPlan> findByIdAndIsDeleted(Long id, boolean isDeleted);

  List<TestPlan> findByIsDeleted(boolean isDeleted);

  Optional<TestPlan> findByTestPlanNameAndIsDeleted(String testPlanName, boolean isDeleted);

  List<TestPlan> findByIdInAndIsDeleted(List<Long> testPlanIds, boolean isDeleted);

  List<TestPlan> findByIsDeletedOrderByIdDesc(boolean isDeleted);
}
