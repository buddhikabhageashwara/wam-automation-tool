package wam.automationtool.domain.entity.testcasestep.alias.parameter;

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
import wam.automationtool.domain.entity.testcasestep.alias.Alias;

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

  @Column(name = "parameterValue")
  private String parameterValue;

  // Many-to-One relationship with Alias
  @ManyToOne
  @JoinColumn(name = "alias_id", nullable = false)
  private Alias alias;

  // Many-to-One relationship with AliasParameterType
  @ManyToOne
  @JoinColumn(name = "alias_parameter_type_id", nullable = false)
  private AliasParameterType aliasParameterType;
}
