package wam.automationtool.application.config.pre.action;

import jakarta.transaction.Transactional;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import wam.automationtool.application.config.pre.action.seed.StartupPreAction;
import wam.automationtool.application.config.pre.action.seed.TestCaseStepAliasParameterType;
import wam.automationtool.domain.entity.testcasestep.alias.parameter.AliasParameterType;
import wam.automationtool.domain.service.AliasParameterTypeDomainService;

@Slf4j
@Component
@Order(3)
@RequiredArgsConstructor
public class AliasParameterTypeSeedAction implements StartupPreAction {

  private final AliasParameterTypeDomainService aliasParameterTypeDomainService;

  @Override
  @Transactional
  public void execute() {
    final Set<String> enumParameterNames =
        Arrays.stream(TestCaseStepAliasParameterType.values())
            .map(TestCaseStepAliasParameterType::getParameterName)
            .collect(Collectors.toSet());
    final Set<String> existingParameterNames =
        aliasParameterTypeDomainService.findByParameterNameIn(enumParameterNames).stream()
            .map(AliasParameterType::getParameterName)
            .collect(Collectors.toSet());
    final List<AliasParameterType> preferenceParameterTypeList =
        Arrays.stream(TestCaseStepAliasParameterType.values())
            .filter(type -> !existingParameterNames.contains(type.getParameterName()))
            .map(
                type ->
                    AliasParameterType.builder()
                        .parameterDisplayName(type.getDisplayName())
                        .parameterName(type.getParameterName())
                        .build())
            .collect(Collectors.toList());
    if (!preferenceParameterTypeList.isEmpty()) {
      aliasParameterTypeDomainService.addAll(preferenceParameterTypeList);
    }
    log.info("AliasParameterType seeded. createdCount={}", preferenceParameterTypeList.size());
  }
}
