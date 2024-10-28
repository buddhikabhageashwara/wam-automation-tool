package wam.automationtool.domain.entity.testcasestep;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import wam.automationtool.domain.entity.BaseEntity;

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

  @Column(name = "executionOrder", nullable = false)
  private Long executionOrder;

  @Column(name = "testCaseStepName", nullable = false)
  private String testCaseStepName;

  @Column(name = "isInverseResult")
  private boolean isInverseResult;

}
