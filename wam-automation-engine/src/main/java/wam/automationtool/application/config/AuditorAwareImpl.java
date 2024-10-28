package wam.automationtool.application.config;

import static wam.automationtool.application.config.AppConstant.CREATED_MODIFIED_USER_ID;

import java.util.Optional;
import org.slf4j.MDC;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

@Component("auditorAware")
public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
    String user = MDC.get(CREATED_MODIFIED_USER_ID);
        return Optional.ofNullable(user);
    }
}

