package wam.automationtool.domain.entity.testplan;

import com.fasterxml.jackson.annotation.JsonFormat;
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
import wam.automationtool.domain.entity.testcase.TestCase;

import java.time.LocalDateTime;
import java.util.List;

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

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS")
  @Column(name = "executionScheduledDate")
  private LocalDateTime executionScheduledDate;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS")
  @Column(name = "executionExpiryDate")
  private LocalDateTime executionExpiryDate;

  @Column(name = "executionFrequency")
  private String executionFrequency;

  // One-to-Many relationship with TestCase
  @OneToMany(mappedBy = "testPlan", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<TestCase> testCases;
}