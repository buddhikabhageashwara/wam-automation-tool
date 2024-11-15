package wam.automationtool.domain.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import wam.automationtool.domain.entity.testcasestep.alias.parameter.AliasParameterType;

@Repository
public interface AliasParameterTypeRepository extends JpaRepository<AliasParameterType, Long> {

  Optional<AliasParameterType> findByIdAndIsDeleted(Long id, boolean isDeleted);

    List<AliasParameterType> findByIsDeleted(boolean isDeleted);

    Optional<AliasParameterType> findByParameterNameAndIsDeleted(String parameterName, boolean isDeleted);
}

