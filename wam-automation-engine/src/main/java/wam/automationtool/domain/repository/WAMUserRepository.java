package wam.automationtool.domain.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import wam.automationtool.domain.entity.user.WAMUser;

@Repository
public interface WAMUserRepository extends JpaRepository<WAMUser, Long> {

  Optional<WAMUser> findByUserEmailAndUserPasswordAndIsDeleted(
      String userEmail, String userPassword, boolean isDeleted);

  Optional<WAMUser> findByUserEmailAndIsDeleted(String userEmail, boolean isDeleted);

  long countByUserType_IdAndIsDeleted(String userTypeId, boolean isDeleted);

  boolean existsByIdAndUserType_IdAndIsDeleted(String id, String userTypeId, boolean isDeleted);
}
