package wam.automationtool.domain.repository;

import wam.automationtool.domain.entity.user.type.UserType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserTypeRepository extends JpaRepository<UserType, Long> {

  Optional<UserType> findByIdAndIsDeleted(String id, boolean isDeleted);

  List<UserType> findByIsDeleted(boolean isDeleted);

  Optional<UserType> findByUserTypeNameAndIsDeleted(String userTypeName, boolean isDeleted);

}
