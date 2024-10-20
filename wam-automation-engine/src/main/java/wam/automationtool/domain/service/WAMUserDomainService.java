package wam.automationtool.domain.service;

import wam.automationtool.domain.entity.user.WAMUser;
import wam.automationtool.domain.repository.WAMUserRepository;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WAMUserDomainService {

  private final WAMUserRepository WAMUserRepository;

  @Autowired
  public WAMUserDomainService(final WAMUserRepository WAMUserRepository) {
    this.WAMUserRepository = WAMUserRepository;
  }

  /**
   * Save a new WAMUser entity to the database.
   *
   * @param WAMUser The WAMUser entity to add.
   */
  public void add(final WAMUser WAMUser) {
    WAMUserRepository.save(WAMUser);
  }

  /**
   * Finds a WAMUser by userEmail and userPassword.
   *
   * @param userEmail The email of the user.
   * @param userPassword The password of the user.
   * @return An Optional containing the found WAMUser, or empty if not found.
   */
  public Optional<WAMUser> findByUserEmailAndUserPassword(
          final String userEmail, final String userPassword) {
    return WAMUserRepository.findByUserEmailAndUserPasswordAndIsDeleted(
            userEmail, userPassword, false);
  }

  /**
   * Finds a WAMUser by userEmail.
   *
   * @param userEmail The email of the user.
   * @return An Optional containing the found WAMUser, or empty if not found.
   */
  public Optional<WAMUser> findByUserEmail(final String userEmail) {
    return WAMUserRepository.findByUserEmailAndIsDeleted(userEmail, false);
  }


  /**
   * Updates an existing WAMUser entity in the database.
   *
   * @param WAMUser The WAMUser entity to update.
   */
  public void update(final WAMUser WAMUser) {
    WAMUserRepository.save(WAMUser);
  }

  /**
   * Finds the very first WAMUser entity based on the createDate column.
   *
   * @return An Optional containing the very first WAMUser, or empty if not found.
   */
  public Optional<WAMUser> findFirstUser() {
    return WAMUserRepository.findFirstByOrderByCreateDateAsc();
  }
}
