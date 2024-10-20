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

  @Column(name = "userTypeName", nullable = false)
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
