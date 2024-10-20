package wam.automationtool.domain.service;

import wam.automationtool.domain.entity.permission.Permission;
import wam.automationtool.domain.repository.PermissionRepository;
import java.util.List;
import java.util.Objects;import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PermissionDomainService {

  private final PermissionRepository permissionRepository;

  @Autowired
  public PermissionDomainService(final PermissionRepository permissionRepository) {
    this.permissionRepository = permissionRepository;
  }

  /**
   * Adds a new PermissionList to the repository.
   *
   * @param permission The PermissionList entity to add.
   */
  public void add(final Permission permission) {
    permissionRepository.save(permission);
  }

  /**
   * Updates an existing PermissionList in the repository.
   *
   * @param permission The PermissionList entity to update.
   */
  public void update(final Permission permission) {
    permissionRepository.save(permission);
  }

  /**
   * Deletes a PermissionList from the repository.
   *
   * @param permission The PermissionList entity to delete.
   */
  public void delete(final Permission permission) {
    permissionRepository.delete(permission);
  }

  /**
   * Finds a PermissionList by its ID.
   *
   * @param id The ID of the PermissionList to find.
   * @return An Optional containing the found PermissionList, or empty if not found.
   */
  public Optional<Permission> findById(final String id) {
    return permissionRepository.findByIdAndIsDeleted(id, false);
  }

  /**
   *
   * @param permissions
   * @return A List containing the found PermissionLists.
   */
  public List<Permission> findByPermission(final List<Permission> permissions) {
    final List<String> permissionIdList = permissions.stream()
            .map(Permission::getId)
            .filter(Objects::nonNull)
            .toList();
    return permissionRepository.findByIdInAndIsDeleted(permissionIdList, false);
  }

  /**
   * Finds all PermissionLists that are not marked as deleted.
   *
   * @return A list of all active PermissionLists.
   */
  public List<Permission> findAll() {
    return permissionRepository.findByIsDeleted(false);
  }

  public Optional<Permission> findByPermissionType(final String permissionType) {
    return permissionRepository.findByPermissionTypeAndIsDeleted(permissionType, false);
  }

}
