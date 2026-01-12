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

package wam.automationtool.domain.entity.user.type;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;
import wam.automationtool.domain.entity.user.WAMUser;
import wam.automationtool.domain.entity.permission.Permission;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "user_type",
        indexes = {
                @Index(name = "user_type_pkey", columnList = "ID", unique = true),
        })
public class UserType {

  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
  @Column(
          name = "ID",
          unique = true,
          nullable = false,
          updatable = false,
          columnDefinition = "VARCHAR(36)")
  private String id;

  @Column(name = "userTypeName", nullable = false, unique = true)
  private String userTypeName;

  @Column(name = "description", columnDefinition = "LONGTEXT")
  private String description;

  @NotNull
  @Column(name = "IsDeleted")
  private Boolean isDeleted = false;

  @CreationTimestamp
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "CreateDate")
  private Date createDate;

  @UpdateTimestamp
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "ModifyDate")
  private Date modifyDate;

  // One-to-many relationship with WAMUser
  @OneToMany(mappedBy = "userType", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<WAMUser> wamUsers;

  // Many-to-many relationship with PermissionList
  @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinTable(
      name = "user_type_permission",
      joinColumns =
          @JoinColumn(
              name = "user_type_id",
              referencedColumnName = "ID",
              columnDefinition = "VARCHAR(36)"),
      inverseJoinColumns =
          @JoinColumn(
              name = "permission_id",
              referencedColumnName = "ID",
              columnDefinition = "VARCHAR(36)"))
  private Set<Permission> permissions = new HashSet<>();
}
