package wam.automationtool.domain.service;

import wam.automationtool.domain.entity.user.type.UserType;
import wam.automationtool.domain.repository.UserTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserTypeDomainService {

  private final UserTypeRepository userTypeRepository;

  @Autowired
  public UserTypeDomainService(final UserTypeRepository userTypeRepository) {
    this.userTypeRepository = userTypeRepository;
  }

  /**
   * Add a new UserType
   *
   * @param userType the entity to add
   */
  public void add(final UserType userType) {
    userTypeRepository.save(userType);
  }

  /**
   * Update an existing UserType
   *
   * @param userType the entity to update
   */
  public void update(final UserType userType) {
    userTypeRepository.save(userType);
  }

  /**
   * Delete an existing UserType
   *
   * @param userType the entity to delete
   */
  public void delete(final UserType userType) {
    userTypeRepository.delete(userType);
  }

  /**
   * Find a UserType by its ID
   *
   * @param id the ID of the UserType
   * @return an Optional containing the found UserType or empty if not found
   */
  public Optional<UserType> findById(final String id) {
    return userTypeRepository.findByIdAndIsDeleted(id, false);
  }

  /**
   * Find a UserType by its userType
   *
   * @param userTypeName
   * @return an Optional containing the found UserType or empty if not found
   */
  public Optional<UserType> findByUserTypeName(final String userTypeName) {
    return userTypeRepository.findByUserTypeNameAndIsDeleted(userTypeName, false);
  }

  /**
   * Find all UserTypes
   *
   * @return a list of all UserTypes that are not deleted
   */
  public List<UserType> findAll() {
    return userTypeRepository.findByIsDeleted(false);
  }
}
