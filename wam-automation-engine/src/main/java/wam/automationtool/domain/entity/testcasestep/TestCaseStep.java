/*
 * MIT License
 *
 * Copyright (c) 2024 buddhika bhageashwara alwis
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import wam.automationtool.domain.entity.BaseEntity;
import wam.automationtool.domain.entity.testcase.TestCase;
import wam.automationtool.domain.entity.testcasestep.parameter.AssertParameter;
import wam.automationtool.domain.entity.testcasestep.parameter.PreferenceParameter;

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

  @Column(name = "description", columnDefinition = "LONGTEXT")
  private String description;

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
