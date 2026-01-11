package wam.automationtool.application.config.pre.action.seed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StartupPreActionRunner implements ApplicationRunner {

    private final List<StartupPreAction> preActions;

    @Override
    public void run(final ApplicationArguments args) {
        preActions.sort(AnnotationAwareOrderComparator.INSTANCE);
        log.info("Startup pre-actions started. count={}", preActions.size());
        for (final StartupPreAction action : preActions) {
            final String actionName = action.getClass().getSimpleName();
            try {
                log.info("Startup pre-action executing: {}", actionName);
                action.execute();
                log.info("Startup pre-action completed: {}", actionName);
            } catch (final Exception exception) {
                log.error("Startup pre-action failed: {}", actionName, exception);
                throw exception;
            }
        }
        log.info("Startup pre-actions finished.");
    }
}
