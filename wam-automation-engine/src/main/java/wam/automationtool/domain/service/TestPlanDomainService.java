package wam.automationtool.domain.service;

import wam.automationtool.domain.entity.testplan.TestPlan;
import wam.automationtool.domain.repository.TestPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class TestPlanDomainService {

    private final TestPlanRepository testPlanRepository;

    @Autowired
    public TestPlanDomainService(final TestPlanRepository testPlanRepository) {
        this.testPlanRepository = testPlanRepository;
    }

    /**
     * Adds a new TestPlan to the repository.
     *
     * @param testPlan The TestPlan entity to add.
     */
    public void add(final TestPlan testPlan) {
        testPlanRepository.save(testPlan);
    }

    /**
     * Updates an existing TestPlan in the repository.
     *
     * @param testPlan The TestPlan entity to update.
     */
    public void update(final TestPlan testPlan) {
        testPlanRepository.save(testPlan);
    }

    /**
     * Deletes a TestPlan from the repository.
     *
     * @param testPlan The TestPlan entity to delete.
     */
    public void delete(final TestPlan testPlan) {
        testPlan.setIsDeleted(true);
        testPlanRepository.save(testPlan);
    }

    /**
     * Finds a TestPlan by its ID.
     *
     * @param id The ID of the TestPlan to find.
     * @return An Optional containing the found TestPlan, or empty if not found.
     */
    public Optional<TestPlan> findById(final Long id) {
    return testPlanRepository.findByIdAndIsDeleted(id, false);
    }

    /**
     * Finds TestPlans by a list of IDs.
     *
     * @param testPlans A list of TestPlan entities to find.
     * @return A List containing the found TestPlans.
     */
    public List<TestPlan> findByTestPlan(final List<TestPlan> testPlans) {
        final List<Long> testPlanIds = testPlans.stream()
                .map(TestPlan::getId)
                .filter(Objects::nonNull)
                .toList();
        return testPlanRepository.findByIdInAndIsDeleted(testPlanIds, false);
    }

    /**
     * Finds all TestPlans that are not marked as deleted.
     *
     * @return A list of all active TestPlans.
     */
    public List<TestPlan> findAll() {
        return testPlanRepository.findByIsDeleted(false);
    }

    public List<TestPlan> findAllByIdDesc() {
        return testPlanRepository.findByIsDeletedOrderByIdDesc(false);
    }

    /**
     * Finds a TestPlan by its name.
     *
     * @param testPlanName The name of the TestPlan to find.
     * @return An Optional containing the found TestPlan, or empty if not found.
     */
    public Optional<TestPlan> findByTestPlanName(final String testPlanName) {
        return testPlanRepository.findByTestPlanNameAndIsDeleted(testPlanName, false);
    }
}
