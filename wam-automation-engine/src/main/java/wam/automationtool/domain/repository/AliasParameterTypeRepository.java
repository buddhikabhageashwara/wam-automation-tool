package wam.automationtool.domain.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import wam.automationtool.domain.entity.testcasestep.alias.parameter.AliasParameterType;

@Repository
public interface AliasParameterTypeRepository extends JpaRepository<AliasParameterType, Long> {

  @EntityGraph(attributePaths = {"aliasParameters"})
  Optional<AliasParameterType> findByIdAndIsDeleted(Long id, boolean isDeleted);

  @EntityGraph(attributePaths = {"aliasParameters"})
  List<AliasParameterType> findByIsDeleted(boolean isDeleted);

  @EntityGraph(attributePaths = {"aliasParameters"})
  Optional<AliasParameterType> findByParameterNameAndIsDeleted(
      String parameterName, boolean isDeleted);

  List<AliasParameterType> findByParameterNameInAndIsDeleted(
      Collection<String> parameterNames, boolean isDeleted);
}
