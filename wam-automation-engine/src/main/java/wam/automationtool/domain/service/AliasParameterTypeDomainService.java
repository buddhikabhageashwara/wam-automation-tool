package wam.automationtool.domain.service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wam.automationtool.domain.entity.testcasestep.alias.parameter.AliasParameterType;
import wam.automationtool.domain.repository.AliasParameterTypeRepository;

@Service
public class AliasParameterTypeDomainService {

  private final AliasParameterTypeRepository aliasParameterTypeRepository;

  @Autowired
  public AliasParameterTypeDomainService(
      final AliasParameterTypeRepository aliasParameterTypeRepository) {
    this.aliasParameterTypeRepository = aliasParameterTypeRepository;
  }

  public void add(final AliasParameterType aliasParameterType) {
    aliasParameterTypeRepository.save(aliasParameterType);
  }

  public void addAll(final List<AliasParameterType> aliasParameterTypeList) {
    aliasParameterTypeRepository.saveAll(aliasParameterTypeList);
  }

  public void update(final AliasParameterType aliasParameterType) {
    aliasParameterTypeRepository.save(aliasParameterType);
  }

  public void delete(final AliasParameterType aliasParameterType) {
    aliasParameterType.setIsDeleted(true);
    aliasParameterTypeRepository.save(aliasParameterType);
  }

  public Optional<AliasParameterType> findById(final Long id) {
    return aliasParameterTypeRepository.findByIdAndIsDeleted(id, false);
  }

  public List<AliasParameterType> findAll() {
    return aliasParameterTypeRepository.findByIsDeleted(false);
  }

  public Optional<AliasParameterType> findByParameterName(final String parameterName) {
    return aliasParameterTypeRepository.findByParameterNameAndIsDeleted(parameterName, false);
  }

  public List<AliasParameterType> findByParameterNameIn(final Collection<String> parameterNames) {
    return aliasParameterTypeRepository.findByParameterNameInAndIsDeleted(parameterNames, false);
  }
}
