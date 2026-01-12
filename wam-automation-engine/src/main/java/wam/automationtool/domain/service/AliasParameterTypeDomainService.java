/*
 * MIT License
 *
 * Copyright (c) 2024 buddhika bhageashwara alwis
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
