package wam.automationtool.domain.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import wam.automationtool.domain.entity.testcase.TestCase;
import wam.automationtool.domain.entity.testcasestep.TestCaseStep;

import java.util.List;
import java.util.Optional;

@Repository
public interface TestCaseStepRepository extends JpaRepository<TestCaseStep, Long> {

  @EntityGraph(attributePaths = {"preferenceParameters", "preferenceParameters.preferenceParameterType", "assertParameters"})
  List<TestCaseStep> findByTestCaseAndIsDeletedOrderByExecutionOrder(
      TestCase testCase, boolean isDeleted);

  Optional<TestCaseStep> findByIdAndIsDeletedOrderByExecutionOrder(Long id, boolean isDeleted);

  @EntityGraph(attributePaths = {"preferenceParameters", "preferenceParameters.preferenceParameterType", "assertParameters"})
    List<TestCaseStep> findByIsDeleted(boolean isDeleted);

    Optional<TestCaseStep> findByTestCaseStepTypeAndIsDeleted(String testCaseStepType, boolean isDeleted);

    List<TestCaseStep> findByIdInAndIsDeleted(List<Long> testCaseStepIds, boolean isDeleted);
}
