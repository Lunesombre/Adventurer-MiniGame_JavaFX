package game.adventurer.service;

import game.adventurer.events.LanguageChangeEvent;
import java.util.Locale;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class LocalizedMessageService {

  private static LocalizedMessageService instance;
  public static final String MESSAGE_SOURCE_NOT_INIT = "MessageSource not initialized";
  public static final String MESSAGE_KEY_NOT_FOUND = "Message key not found: {}";

  @Setter
  @Getter
  private Locale currentLocale;
  @Getter
  private ReloadableResourceBundleMessageSource messageSource; // this one allows for dynamically reload messages according to Locale changes.
  private ApplicationEventPublisher eventPublisher;

  // private Constructor to prevent direct instantiation
  private LocalizedMessageService() {
    this.currentLocale = Locale.getDefault();
  }

  // getter for the singleton
  public static synchronized LocalizedMessageService getInstance() {
    if (instance == null) {
      instance = new LocalizedMessageService();
    }
    return instance;
  }

  // initializing dependencies
  public void initialize(ReloadableResourceBundleMessageSource messageSource, ApplicationEventPublisher eventPublisher) {
    this.messageSource = messageSource;
    this.eventPublisher = eventPublisher;
  }


  public void setLanguage(String language) {
    Locale newLocale = Locale.forLanguageTag(language);
    if (!newLocale.equals(this.currentLocale)) {
      this.currentLocale = newLocale; // Update only if necessary
      this.messageSource.clearCache(); // Reload messages
      eventPublisher.publishEvent(new LanguageChangeEvent(this, currentLocale));

      log.debug("Language requested : {}", language);
      log.debug("CurrentLocale then : {}", currentLocale);
    } else {
      log.info("The requested Locale is already the current Locale : {}", currentLocale);
    }
  }


  public String getMessage(String key) {
    if (messageSource == null) {
      log.warn(MESSAGE_SOURCE_NOT_INIT);
      return key;
    }
    try {
      return messageSource.getMessage(key, null, currentLocale);
    } catch (NoSuchMessageException e) {
      log.debug(MESSAGE_KEY_NOT_FOUND, key, e);
      return key;
    }
  }

  public String getMessage(String key, Object... args) {
    if (messageSource == null) {
      log.warn(MESSAGE_SOURCE_NOT_INIT);
      return key;
    }
    try {
      return messageSource.getMessage(key, args, currentLocale);
    } catch (NoSuchMessageException e) {
      log.debug(MESSAGE_KEY_NOT_FOUND, key, e);

      return key;
    }
  }

  public String getMessage(String key, String... args) {
    if (messageSource == null) {
      log.warn(MESSAGE_SOURCE_NOT_INIT);
      return key;
    }
    try {
      return messageSource.getMessage(key, args, currentLocale);
    } catch (NoSuchMessageException e) {
      log.debug(MESSAGE_KEY_NOT_FOUND, key, e);
      return key;
    }
  }

}
