package game.adventurer.listeners;

import game.adventurer.events.LanguageChangeEvent;
import game.adventurer.service.LocalizationService;
import game.adventurer.service.LocalizedMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LanguageChangeListener implements ApplicationListener<LanguageChangeEvent> {

  private final LocalizedMessageService localizedMessageService;
  private final LocalizationService localizationService;

  public LanguageChangeListener(LocalizedMessageService localizedMessageService, LocalizationService localizationService) {
    this.localizedMessageService = localizedMessageService;
    this.localizationService = localizationService;
  }

  @Override
  public void onApplicationEvent(LanguageChangeEvent event) {
    log.debug("J'ai capté un language change event");
    log.debug("Langue changée pour : {}", event.getNewLocale());
    log.debug("languageService.getCurrentLocale() : {}", localizedMessageService.getCurrentLocale());

    // Update all components using the LocalizationService
    localizationService.updateAllComponents(event.getNewLocale());

  }

}
