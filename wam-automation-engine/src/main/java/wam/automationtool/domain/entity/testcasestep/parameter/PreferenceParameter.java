package wam.automationtool.domain.entity.testcasestep.parameter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import wam.automationtool.domain.entity.BaseEntity;
import wam.automationtool.domain.entity.testcasestep.TestCaseStep;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "preference_parameter",
        indexes = {
                @Index(name = "preference_parameter_pkey", columnList = "ID", unique = true),
        })
public class PreferenceParameter extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID", unique = true, nullable = false, updatable = false)
  private Long id;

  @Column(name = "parameterValue")
  private String parameterValue;

  // Many-to-One relationship with TestCaseStep
  @ManyToOne
  @JoinColumn(name = "test_case_step_id", nullable = false)
  private TestCaseStep testCaseStep;

  // Many-to-One relationship with PreferenceParameterType
  @ManyToOne
  @JoinColumn(name = "preference_parameter_type_id", nullable = false)
  private PreferenceParameterType preferenceParameterType;
}
