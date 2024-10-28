package wam.automationtool.domain.entity.permission;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;
import wam.automationtool.domain.entity.BaseEntity;
import wam.automationtool.domain.entity.user.type.UserType;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
    name = "permission",
    indexes = {
      @Index(name = "permission_pkey", columnList = "ID", unique = true),
    })
public class Permission extends BaseEntity {

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

  @Column(name = "permissionType", nullable = false, columnDefinition = "LONGTEXT")
  private String permissionType;

  @Column(name = "description", columnDefinition = "LONGTEXT")
  private String description;

  // Many-to-many relationship with UserType
  @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  private Set<UserType> userTypes = new HashSet<>();
}