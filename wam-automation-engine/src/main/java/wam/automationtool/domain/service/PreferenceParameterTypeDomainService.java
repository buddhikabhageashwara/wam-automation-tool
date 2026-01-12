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

import wam.automationtool.domain.entity.testcasestep.parameter.PreferenceParameterType;
import wam.automationtool.domain.repository.PreferenceParameterTypeRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PreferenceParameterTypeDomainService {

    private final PreferenceParameterTypeRepository preferenceParameterTypeRepository;

    @Autowired
    public PreferenceParameterTypeDomainService(final PreferenceParameterTypeRepository preferenceParameterTypeRepository) {
        this.preferenceParameterTypeRepository = preferenceParameterTypeRepository;
    }

    public void add(final PreferenceParameterType preferenceParameterType) {
        preferenceParameterTypeRepository.save(preferenceParameterType);
    }

    public List<PreferenceParameterType> addAll(final List<PreferenceParameterType> preferenceParameterTypes) {
        return preferenceParameterTypeRepository.saveAll(preferenceParameterTypes);
    }

    public void update(final PreferenceParameterType preferenceParameterType) {
        preferenceParameterTypeRepository.save(preferenceParameterType);
    }

    public void delete(final PreferenceParameterType preferenceParameterType) {
        preferenceParameterType.setIsDeleted(true);
        preferenceParameterTypeRepository.save(preferenceParameterType);
    }

    public Optional<PreferenceParameterType> findById(final Long id) {
        return preferenceParameterTypeRepository.findByIdAndIsDeleted(id, false);
    }

    public List<PreferenceParameterType> findAll() {
        return preferenceParameterTypeRepository.findByIsDeleted(false);
    }

    public Optional<PreferenceParameterType> findByParameterName(final String parameterName) {
        return preferenceParameterTypeRepository.findByParameterNameAndIsDeleted(parameterName, false);
    }

    public List<PreferenceParameterType> findByParameterNameIn(final Collection<String> parameterNames) {
        return preferenceParameterTypeRepository.findByParameterNameInAndIsDeleted(parameterNames, false);
    }
}
