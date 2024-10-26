package game.adventurer.events;

import java.util.Locale;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;

@Slf4j
@Getter
public class LanguageChangeEvent extends ApplicationEvent {

  private final Locale newLocale;

  public LanguageChangeEvent(Object source, Locale newLocale) {
    super(source);
    this.newLocale = newLocale;
    log.info("on est dans le LanguageChangEvent, newLocale : {}", newLocale);
  }
}
