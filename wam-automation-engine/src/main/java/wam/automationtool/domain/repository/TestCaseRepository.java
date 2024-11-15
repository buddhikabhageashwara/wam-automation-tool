package wam.automationtool.domain.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import wam.automationtool.domain.entity.testcase.TestCase;
import wam.automationtool.domain.entity.testplan.TestPlan;

@Repository
public interface TestCaseRepository extends JpaRepository<TestCase, Long> {

  Optional<TestCase> findByIdAndIsDeleted(Long id, boolean isDeleted);

  List<TestCase> findByIsDeletedOrderByExecutionOrder(boolean isDeleted);

  Optional<TestCase> findByTestCaseNameAndIsDeleted(String testCaseName, boolean isDeleted);

  List<TestCase> findByIdInAndIsDeleted(List<Long> testCaseIds, boolean isDeleted);

  List<TestCase> findByTestPlanAndIsDeletedOrderByExecutionOrder(
      TestPlan testPlan, boolean isDeleted);
}
