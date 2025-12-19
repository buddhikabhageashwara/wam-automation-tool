package wam.automationtool.domain.entity.testcasestep;

import jakarta.persistence.CascadeType;
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
import wam.automationtool.domain.entity.testcase.TestCase;
import wam.automationtool.domain.entity.testcasestep.parameter.AssertParameter;
import wam.automationtool.domain.entity.testcasestep.parameter.PreferenceParameter;

import java.util.List;import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "test_case_step",
        indexes = {
                @Index(name = "test_case_step_pkey", columnList = "ID", unique = true),
        })
public class TestCaseStep extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID", unique = true, nullable = false, updatable = false)
  private Long id;

  @Column(name = "testCaseStepType", nullable = false)
  private String testCaseStepType;

  @Column(name = "executionOrder")
  private long executionOrder;

  @Column(name = "testCaseStepName", nullable = false)
  private String testCaseStepName;

  // Many-to-One relationship with TestCase
  @ManyToOne
  @JoinColumn(name = "test_case_id", nullable = false)
  private TestCase testCase;

  // One-to-Many relationship with PreferenceParameter
  @OneToMany(mappedBy = "testCaseStep", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<PreferenceParameter> preferenceParameters;

  // One-to-Many relationship with AssertParameter
  @OneToMany(mappedBy = "testCaseStep", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<AssertParameter> assertParameters;
}
