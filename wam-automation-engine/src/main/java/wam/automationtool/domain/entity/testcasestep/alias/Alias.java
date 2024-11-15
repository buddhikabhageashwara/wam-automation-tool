package wam.automationtool.domain.entity.testcasestep.alias;

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
import wam.automationtool.domain.entity.testcasestep.alias.parameter.AliasParameter;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "alias",
        indexes = {
                @Index(name = "alias_pkey", columnList = "ID", unique = true),
        })
public class Alias extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID", unique = true, nullable = false, updatable = false)
  private Long id;

  @Column(name = "aliasType", nullable = false)
  private String aliasType;

  @Column(name = "aliasName", nullable = false)
  private String aliasName;

  // One-to-Many relationship with AliasParameter
  @OneToMany(mappedBy = "alias", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<AliasParameter> aliasParameters;
}
