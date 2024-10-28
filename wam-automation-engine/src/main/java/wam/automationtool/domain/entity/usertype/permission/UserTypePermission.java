package wam.automationtool.domain.entity.usertype.permission;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;
import wam.automationtool.domain.entity.BaseEntity;
import wam.automationtool.domain.entity.permission.Permission;
import wam.automationtool.domain.entity.user.type.UserType;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
    name = "user_type_permission",
    indexes = {
      @Index(name = "user_type_permission_pkey", columnList = "ID", unique = true),
    })
public class UserTypePermission extends BaseEntity {

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

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_type_id", referencedColumnName = "ID", nullable = false, columnDefinition = "VARCHAR(36)")
  private UserType userType;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "permission_id",
      referencedColumnName = "ID",
      nullable = false,
      columnDefinition = "VARCHAR(36)")
  private Permission permission;
}