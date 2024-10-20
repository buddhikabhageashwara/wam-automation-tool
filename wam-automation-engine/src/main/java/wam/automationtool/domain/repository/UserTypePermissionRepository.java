package wam.automationtool.domain.repository;

import wam.automationtool.domain.entity.user.type.UserType;
import wam.automationtool.domain.entity.usertype.permission.UserTypePermission;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserTypePermissionRepository extends JpaRepository<UserTypePermission, String> {

  Optional<UserTypePermission> findByPermissionIdAndUserTypeIdAndIsDeleted(
      String permissionListId, String userTypeId, boolean isDeleted);

    List<UserTypePermission> findByUserTypeAndIsDeleted(UserType userType, boolean isDeleted);
}
