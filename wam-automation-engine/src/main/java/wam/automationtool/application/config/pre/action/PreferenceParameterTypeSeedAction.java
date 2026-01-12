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

package wam.automationtool.application.config.pre.action;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import jakarta.transaction.Transactional;
import wam.automationtool.application.config.pre.action.seed.StartupPreAction;
import wam.automationtool.application.config.pre.action.seed.TestCaseStepPreferenceParameterType;
import wam.automationtool.domain.entity.testcasestep.parameter.PreferenceParameterType;
import wam.automationtool.domain.service.PreferenceParameterTypeDomainService;

@Slf4j
@Component
@Order(3)
@RequiredArgsConstructor
public class PreferenceParameterTypeSeedAction implements StartupPreAction {

    private final PreferenceParameterTypeDomainService preferenceParameterTypeDomainService;

    @Override
    @Transactional
    public void execute() {
        final Set<String> enumParameterNames =
                Arrays.stream(TestCaseStepPreferenceParameterType.values())
                        .map(TestCaseStepPreferenceParameterType::getParameterName)
                        .collect(Collectors.toSet());
        final Set<String> existingParameterNames =
                preferenceParameterTypeDomainService.findByParameterNameIn(enumParameterNames).stream()
                        .map(PreferenceParameterType::getParameterName)
                        .collect(Collectors.toSet());
        final List<PreferenceParameterType> preferenceParameterTypeList =
                Arrays.stream(TestCaseStepPreferenceParameterType.values())
                        .filter(type
                                -> !existingParameterNames.contains(type.getParameterName()))
                        .map(
                                type ->
                                        PreferenceParameterType.builder()
                                                .parameterDisplayName(type.getDisplayName())
                                                .parameterName(type.getParameterName())
                                                .build())
                        .collect(Collectors.toList());
        if (!preferenceParameterTypeList.isEmpty()) {
            preferenceParameterTypeDomainService.addAll(preferenceParameterTypeList);
        }
        log.info("PreferenceParameterType seeded. createdCount={}", preferenceParameterTypeList.size());
    }
}
