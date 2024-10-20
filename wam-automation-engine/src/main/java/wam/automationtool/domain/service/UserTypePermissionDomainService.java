package wam.automationtool.domain.service;

import wam.automationtool.domain.entity.user.type.UserType;
import wam.automationtool.domain.entity.usertype.permission.UserTypePermission;
import wam.automationtool.domain.repository.UserTypePermissionRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserTypePermissionDomainService {

  private final UserTypePermissionRepository repository;

  @Autowired
  public UserTypePermissionDomainService(final UserTypePermissionRepository repository) {
    this.repository = repository;
  }

  public void add(final UserTypePermission userTypePermission) {
    repository.save(userTypePermission);
  }

  public void update(final UserTypePermission userTypePermission) {
    repository.save(userTypePermission);
  }

  public void delete(final UserTypePermission userTypePermission) {
    repository.delete(userTypePermission);
  }

  public Optional<UserTypePermission> findById(final String id) {
    return repository.findById(id);
  }

  public Optional<UserTypePermission> findByPermissionIdAndUserTypeId(
      final String permissionListId, final String userTypeId) {
    return repository.findByPermissionIdAndUserTypeIdAndIsDeleted(
        permissionListId, userTypeId, false);
  }

  public List<UserTypePermission> findByUserType(final UserType userType) {
    return repository.findByUserTypeAndIsDeleted(userType, false);
  }

  public List<UserTypePermission> findAll() {
    return repository.findAll();
  }
}
