package wam.automationtool.application.config;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ResourceLoader implements ResourceMessages {

  private final ResourceBundle resourceBundle;

  @Autowired
  public ResourceLoader() {

    this.resourceBundle = ResourceBundle.getBundle("messages", Locale.getDefault());
  }

  @Override
  public final String getErrorMessage(final String messageKey) {
    try {
      return resourceBundle.getString(messageKey);
    } catch (MissingResourceException e) {
      return messageKey;
    }
  }

  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder builder) {
    return builder.build();
  }
}
