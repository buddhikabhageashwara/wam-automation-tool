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
