package wam.automationtool.domain.repository;

import wam.automationtool.domain.entity.testcasestep.alias.parameter.AliasParameter;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AliasParameterRepository extends JpaRepository<AliasParameter, Long> {

    Optional<AliasParameter> findByIdAndIsDeleted(Long id, boolean isDeleted);

    List<AliasParameter> findByIsDeleted(boolean isDeleted);

    List<AliasParameter> findByAlias_IdAndIsDeleted(Long aliasId, boolean isDeleted);

    List<AliasParameter> findByIdInAndIsDeleted(List<Long> aliasParameterIds, boolean isDeleted);
}
