package wam.automationtool.application.config.pre.action;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import wam.automationtool.application.config.pre.action.seed.StartupPreAction;
import wam.automationtool.application.impl.permission.PermissionService;

@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class PermissionSeedAction implements StartupPreAction {

  private final PermissionService permissionService;

  @Override
  @Transactional
  public void execute() {
    try {
      permissionService.addPermission();
    } catch (final Exception exception) {
      log.error("Permission seeding failed. Application startup will fail.", exception);
      throw exception;
    }
  }
}
