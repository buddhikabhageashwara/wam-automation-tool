package wam.automationtool.domain.entity;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.envers.Audited;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Audited
public class BaseEntity {

  @CreationTimestamp
  @Column(name = "CreatedDate", updatable = false)
  private LocalDateTime createdDate;

  @UpdateTimestamp
  @Column(name = "LastModifiedDate")
  private LocalDateTime lastModifiedDate;

  @CreatedBy
  @Column(name = "CreatedBy")
  private String createdBy;

  @LastModifiedBy
  @Column(name = "LastModifiedBy")
  private String lastModifiedBy;

  @NotNull
  @Column(name = "IsDeleted")
  private Boolean isDeleted = false;
}
