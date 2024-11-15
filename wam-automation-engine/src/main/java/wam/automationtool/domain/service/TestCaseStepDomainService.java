package wam.automationtool.domain.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wam.automationtool.domain.entity.testcase.TestCase;
import wam.automationtool.domain.entity.testcasestep.TestCaseStep;
import wam.automationtool.domain.repository.TestCaseStepRepository;

@Service
public class TestCaseStepDomainService {

  private final TestCaseStepRepository testCaseStepRepository;

  @Autowired
  public TestCaseStepDomainService(final TestCaseStepRepository testCaseStepRepository) {
    this.testCaseStepRepository = testCaseStepRepository;
  }

  /**
   * Adds a new TestCaseStep to the repository.
   *
   * @param testCaseStep The TestCaseStep entity to add.
   */
  public TestCaseStep add(final TestCaseStep testCaseStep) {
    return testCaseStepRepository.saveAndFlush(testCaseStep);
  }

  /**
   * Updates an existing TestCaseStep in the repository.
   *
   * @param testCaseStep The TestCaseStep entity to update.
   */
  public void update(final TestCaseStep testCaseStep) {
    testCaseStepRepository.save(testCaseStep);
  }

  /**
   * Deletes a TestCaseStep from the repository.
   *
   * @param testCaseStep The TestCaseStep entity to delete.
   */
  public void delete(final TestCaseStep testCaseStep) {
    testCaseStep.setIsDeleted(true);
    testCaseStepRepository.save(testCaseStep);
  }

  /**
   * Finds a TestCaseStep by its ID.
   *
   * @param id The ID of the TestCaseStep to find.
   * @return An Optional containing the found TestCaseStep, or empty if not found.
   */
  public Optional<TestCaseStep> findById(final Long id) {
    return testCaseStepRepository.findByIdAndIsDeletedOrderByExecutionOrder(id, false);
  }

  public List<TestCaseStep> findByTestCase(final TestCase testCase) {
    return testCaseStepRepository.findByTestCaseAndIsDeletedOrderByExecutionOrder(testCase, false);
  }

  /**
   * Finds a list of TestCaseStep entities by their IDs.
   *
   * @param testCaseSteps A list of TestCaseStep entities.
   * @return A List containing the found TestCaseSteps.
   */
  public List<TestCaseStep> findByTestCaseSteps(final List<TestCaseStep> testCaseSteps) {
    final List<Long> testCaseStepIdList =
        testCaseSteps.stream().map(TestCaseStep::getId).filter(Objects::nonNull).toList();
    return testCaseStepRepository.findByIdInAndIsDeleted(testCaseStepIdList, false);
  }

  /**
   * Finds all TestCaseStep entities that are not marked as deleted.
   *
   * @return A list of all active TestCaseStep entities.
   */
  public List<TestCaseStep> findAll() {
    return testCaseStepRepository.findByIsDeleted(false);
  }

  /**
   * Finds a TestCaseStep by its type.
   *
   * @param testCaseStepType The type of the TestCaseStep.
   * @return An Optional containing the found TestCaseStep, or empty if not found.
   */
  public Optional<TestCaseStep> findByTestCaseStepType(final String testCaseStepType) {
    return testCaseStepRepository.findByTestCaseStepTypeAndIsDeleted(testCaseStepType, false);
  }
}
