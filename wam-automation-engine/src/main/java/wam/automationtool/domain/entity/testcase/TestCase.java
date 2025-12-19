package wam.automationtool.domain.entity.testcase;

import com.fasterxml.jackson.annotation.JsonFormat;import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import wam.automationtool.domain.entity.BaseEntity;
import wam.automationtool.domain.entity.testcasestep.TestCaseStep;
import wam.automationtool.domain.entity.testplan.TestPlan;

import java.time.LocalDateTime;import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "test_case",
        indexes = {
                @Index(name = "test_case_pkey", columnList = "ID", unique = true),
        })
public class TestCase extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID", unique = true, nullable = false, updatable = false)
  private Long id;

  @Column(name = "testCaseName", nullable = false, columnDefinition = "LONGTEXT")
  private String testCaseName;

  @Column(name = "description", columnDefinition = "LONGTEXT")
  private String description;

  // TODO: check executionScheduledDate, executionExpiryDate, executionFrequency definitions.
  //  it seems like currently not using them and these fields can be used to run a test case
  //  from a specific date to expire date for a defined frequency
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS")
  @Column(name = "executionScheduledDate")
  private LocalDateTime executionScheduledDate;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS")
  @Column(name = "executionExpiryDate")
  private LocalDateTime executionExpiryDate;

  @Column(name = "executionFrequency")
  private String executionFrequency;

  @Column(name = "executionOrder")
  private long executionOrder;

  // Many-to-One relationship with TestPlan
  @ManyToOne
  @JoinColumn(name = "test_plan_id", nullable = false)
  private TestPlan testPlan;

  // One-to-Many relationship with TestCaseStep
  @OneToMany(mappedBy = "testCase", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<TestCaseStep> testCaseSteps;
}
