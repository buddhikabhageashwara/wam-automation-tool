package wam.automationtool.domain.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import wam.automationtool.domain.entity.testcase.TestCase;
import wam.automationtool.domain.entity.testplan.TestPlan;

@Repository
public interface TestCaseRepository extends JpaRepository<TestCase, Long> {

  @EntityGraph(attributePaths = {"testCaseSteps"})
  Optional<TestCase> findByIdAndIsDeleted(Long id, boolean isDeleted);

  @EntityGraph(attributePaths = {"testCaseSteps"})
  List<TestCase> findByIsDeletedOrderByExecutionOrder(boolean isDeleted);

  @EntityGraph(attributePaths = {"testCaseSteps"})
  Optional<TestCase> findByTestCaseNameAndIsDeleted(String testCaseName, boolean isDeleted);

  @EntityGraph(attributePaths = {"testCaseSteps"})
  List<TestCase> findByIdInAndIsDeleted(List<Long> testCaseIds, boolean isDeleted);

  @EntityGraph(attributePaths = {"testCaseSteps"})
  List<TestCase> findByTestPlanAndIsDeletedOrderByExecutionOrder(
      TestPlan testPlan, boolean isDeleted);
}
