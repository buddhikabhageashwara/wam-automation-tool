package wam.automationtool.domain.repository;

import wam.automationtool.domain.entity.testcasestep.alias.Alias;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AliasRepository extends JpaRepository<Alias, Long> {

    Optional<Alias> findByIdAndIsDeleted(Long id, boolean isDeleted);

    List<Alias> findByIsDeleted(boolean isDeleted);

    List<Alias> findByAliasTypeAndIsDeleted(String aliasType, boolean isDeleted);

    Optional<Alias> findByAliasNameAndIsDeleted(String aliasName, boolean isDeleted);

    List<Alias> findByIdInAndIsDeleted(List<Long> aliasIds, boolean isDeleted);
}
