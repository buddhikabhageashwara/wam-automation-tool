package wam.automationtool.domain.repository;

import wam.automationtool.domain.entity.permission.Permission;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

  Optional<Permission> findByIdAndIsDeleted(String id, boolean isDeleted);
  List<Permission> findByIsDeleted(boolean isDeleted);
  Optional<Permission> findByPermissionTypeAndIsDeleted(String permissionType, boolean isDeleted);

  List<Permission> findByIdInAndIsDeleted(
          List<String> permissionListIds, boolean isDeleted);
}
