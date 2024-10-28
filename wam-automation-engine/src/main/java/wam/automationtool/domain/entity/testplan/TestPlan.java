package wam.automationtool.domain.entity.testplan;

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

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "test_plan",
        indexes = {
                @Index(name = "test_plan_pkey", columnList = "ID", unique = true),
        })
public class TestPlan extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID", unique = true, nullable = false, updatable = false)
  private Long id;

  @Column(name = "testPlanName", nullable = false, columnDefinition = "LONGTEXT")
  private String testPlanName;

  @Column(name = "description", columnDefinition = "LONGTEXT")
  private String description;

  @Column(name = "executionScheduledDate")
  private LocalDateTime executionScheduledDate;

  @Column(name = "executionExpiryDate")
  private LocalDateTime executionExpiryDate;

  @Column(name = "executionFrequency")
  private String executionFrequency;
}
