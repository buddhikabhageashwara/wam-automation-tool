package wam.automationtool.domain.entity.testcase;

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
}
