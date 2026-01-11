package wam.automationtool.domain.service;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wam.automationtool.domain.entity.user.WAMUser;
import wam.automationtool.domain.repository.WAMUserRepository;

@Service
public class WAMUserDomainService {

  private final WAMUserRepository wamUserRepository;

  @Autowired
  public WAMUserDomainService(final WAMUserRepository wamUserRepository) {
    this.wamUserRepository = wamUserRepository;
  }

  /**
   * Save a new WAMUser entity to the database.
   *
   * @param WAMUser The WAMUser entity to add.
   */
  public void add(final WAMUser WAMUser) {
    wamUserRepository.save(WAMUser);
  }

  /**
   * Finds a WAMUser by userEmail.
   *
   * @param userEmail The email of the user.
   * @return An Optional containing the found WAMUser, or empty if not found.
   */
  public Optional<WAMUser> findByUserEmail(final String userEmail) {
    return wamUserRepository.findByUserEmailAndIsDeleted(userEmail, false);
  }

  /**
   * Updates an existing WAMUser entity in the database.
   *
   * @param WAMUser The WAMUser entity to update.
   */
  public void update(final WAMUser WAMUser) {
    wamUserRepository.save(WAMUser);
  }

  public long countActiveUsersForUserTypeId(final String userTypeId) {
    return wamUserRepository.countByUserType_IdAndIsDeleted(userTypeId, false);
  }

  public boolean existsActiveUserByIdAndUserTypeId(
      final String wamUserId, final String userTypeId) {
    return wamUserRepository.existsByIdAndUserType_IdAndIsDeleted(wamUserId, userTypeId, false);
  }
}
