package wam.automationtool.domain.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import wam.automationtool.domain.entity.testcasestep.alias.Alias;

@Repository
public interface AliasRepository extends JpaRepository<Alias, Long> {

  @EntityGraph(attributePaths = {"aliasParameters"})
  Optional<Alias> findByIdAndIsDeleted(Long id, boolean isDeleted);

  @EntityGraph(attributePaths = {"aliasParameters", "aliasParameters.aliasParameterType"})
  List<Alias> findByIsDeleted(boolean isDeleted);

  @EntityGraph(attributePaths = {"aliasParameters"})
  List<Alias> findByAliasTypeAndIsDeleted(String aliasType, boolean isDeleted);

  @EntityGraph(attributePaths = {"aliasParameters"})
  Optional<Alias> findByAliasNameAndIsDeleted(String aliasName, boolean isDeleted);

  @EntityGraph(attributePaths = {"aliasParameters"})
  List<Alias> findByIdInAndIsDeleted(List<Long> aliasIds, boolean isDeleted);
}
