package wam.automationtool.domain.entity.testcasestep.alias.parameter;

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
    name = "alias_parameter",
    indexes = {
      @Index(name = "alias_parameter_pkey", columnList = "ID", unique = true),
    })
public class AliasParameter extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID", unique = true, nullable = false, updatable = false)
  private Long id;

  @Column(name = "parameterDisplayName", nullable = false)
  private String parameterDisplayName;

  @Column(name = "parameterName", nullable = false)
  private String parameterName;

  @Column(name = "parameterValue")
  private String parameterValue;

}
