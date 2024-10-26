package game.adventurer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import game.adventurer.events.LanguageChangeEvent;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

@ExtendWith(MockitoExtension.class)
class LocalizedMessageServiceTest {

  private LocalizedMessageService localizedMessageService;

  @Mock
  private ReloadableResourceBundleMessageSource messageSource;

  @Mock
  private ApplicationEventPublisher eventPublisher;

  @BeforeEach
  void setUp() {
    localizedMessageService = LocalizedMessageService.getInstance();
    localizedMessageService.initialize(messageSource, eventPublisher);
  }

  @Test
  void testSetLanguage() {
    // GIVEN
    String newLanguage = "fr";
    Locale expectedLocale = Locale.forLanguageTag(newLanguage);

    // WHEN
    localizedMessageService.setLanguage(newLanguage);

    // THEN
    assertEquals(expectedLocale, localizedMessageService.getCurrentLocale());
    verify(messageSource).clearCache();
    verify(eventPublisher).publishEvent(any(LanguageChangeEvent.class));
  }

  @Test
  void testGetMessage_WithoutArgs() {
    // GIVEN
    String key = "test.key";
    String expectedMessage = "Test Message";
    when(messageSource.getMessage(eq(key), isNull(), any(Locale.class))).thenReturn(expectedMessage);

    // WHEN
    String result = localizedMessageService.getMessage(key);

    // THEN
    assertEquals(expectedMessage, result);
  }

  @Test
  void testGetMessage_WithObjectArgs() {
    // GIVEN
    String key = "test.key";
    Object[] args = {1, "test"};
    String expectedMessage = "Test Message 1 test";
    when(messageSource.getMessage(eq(key), eq(args), any(Locale.class))).thenReturn(expectedMessage);

    // WHEN
    String result = localizedMessageService.getMessage(key, args);

    // THEN
    assertEquals(expectedMessage, result);
  }

  @Test
  void testGetMessage_WithStringArgs() {
    // GIVEN
    String key = "test.key";
    String[] args = {"arg1", "arg2"};
    String expectedMessage = "Test Message arg1 arg2";
    when(messageSource.getMessage(eq(key), eq(args), any(Locale.class))).thenReturn(expectedMessage);

    // WHEN
    String result = localizedMessageService.getMessage(key, args);

    // THEN
    assertEquals(expectedMessage, result);
  }

  @Test
  void testGetMessage_NoSuchMessageException() {
    // GIVEN
    String key = "nonexistent.key";
    when(messageSource.getMessage(eq(key), isNull(), any(Locale.class)))
        .thenThrow(new NoSuchMessageException(""));

    // WHEN
    String result = localizedMessageService.getMessage(key);

    // THEN
    assertEquals(key, result);
  }
}
