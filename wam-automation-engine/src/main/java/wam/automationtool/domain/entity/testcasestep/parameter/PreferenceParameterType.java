package wam.automationtool.domain.entity.testcasestep.parameter;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import wam.automationtool.domain.entity.BaseEntity;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "preference_parameter_type",
        indexes = {
                @Index(name = "preference_parameter_type_pkey", columnList = "ID", unique = true),
        })
public class PreferenceParameterType extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID", unique = true, nullable = false, updatable = false)
  private Long id;

  @Column(name = "parameterDisplayName", nullable = false)
  private String parameterDisplayName;

  @Column(name = "parameterName", nullable = false)
  private String parameterName;

  // One-to-Many relationship with PreferenceParameter
  @OneToMany(mappedBy = "preferenceParameterType", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<PreferenceParameter> preferenceParameters;
}