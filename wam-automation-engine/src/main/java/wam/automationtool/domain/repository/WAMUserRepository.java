package wam.automationtool.domain.repository;

import wam.automationtool.domain.entity.user.WAMUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface WAMUserRepository extends JpaRepository<WAMUser, Long> {

  Optional<WAMUser> findByUserEmailAndUserPasswordAndIsDeleted(
      String userEmail, String userPassword, boolean isDeleted);

  // Method to find the very first WAMUser record based on createDate
  Optional<WAMUser> findFirstByOrderByCreateDateAsc();
  Optional<WAMUser> findByUserEmailAndIsDeleted(String userEmail, boolean isDeleted);}
