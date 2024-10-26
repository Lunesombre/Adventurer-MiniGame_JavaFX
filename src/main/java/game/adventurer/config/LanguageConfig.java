package game.adventurer.config;

import game.adventurer.service.LocalizedMessageService;
import jakarta.annotation.PostConstruct;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

@Configuration
public class LanguageConfig {

  private final ReloadableResourceBundleMessageSource messageSource;

  private final ApplicationEventPublisher eventPublisher;

  public LanguageConfig(ReloadableResourceBundleMessageSource messageSource, ApplicationEventPublisher eventPublisher) {
    this.messageSource = messageSource;
    this.eventPublisher = eventPublisher;
  }

  @PostConstruct
  public void initializeLanguageService() {
    LocalizedMessageService.getInstance().initialize(messageSource, eventPublisher);
  }
}
