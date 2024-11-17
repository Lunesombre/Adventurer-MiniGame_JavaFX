package game.adventurer.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import game.adventurer.common.Localizable;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LocalizationServiceTest {

  private Set<Localizable> localizableComponents;
  private LocalizationService localizationService;

  @Mock
  private Localizable mockComponent1;

  @Mock
  private Localizable mockComponent2;

  @BeforeEach
  void setup() {
    localizableComponents = new HashSet<>();
    localizationService = new LocalizationService(localizableComponents);
  }

  @Test
  @DisplayName("Test registerLocalizable()")
  void testRegisterLocalizable() {
    // GIVEN
    // LocalizationService is initialized

    // WHEN
    localizationService.registerLocalizable(mockComponent1);
    // THEN
    assertTrue(localizableComponents.contains(mockComponent1), "Component should be registered");
  }

  @Test
  @DisplayName("Test registerLocalizable() on a null value")
  void shouldDoSomething() {
    // GIVEN
    // LocalizationService is initialized

    // WHEN & THEN
    assertThrows(NullPointerException.class, () -> localizationService.registerLocalizable(null),
        "registerLocalizable should throw a NullPointerException when called on null");
  }

  @Test
  @DisplayName("Test updateAllComponents()")
  void testUpdateAllComponents() {
    // GIVEN
    localizationService.registerLocalizable(mockComponent1);
    localizationService.registerLocalizable(mockComponent2);
    Locale newLocale = Locale.FRENCH;

    // WHEN
    localizationService.updateAllComponents(newLocale);

    // THEN
    verify(mockComponent1, times(1)).updateLanguage(newLocale);
    verify(mockComponent2, times(1)).updateLanguage(newLocale);
  }

  @Test
  @DisplayName("Test updateAllComponents() on an empty set")
  void testUpdateAllComponentsWithEmptySet() {
    // GIVEN
    Locale newLocale = Locale.GERMAN;
    // WHEN & THEN
    assertDoesNotThrow(() -> localizationService.updateAllComponents(newLocale),
        "updateAllComponents shouldn't throw any exceptions when executed on an empty set");
  }

  @Test
  @DisplayName("registerLocalizable should avoid duplicates")
  void testRegisterLocalizableNoDuplicates() {
    // GIVEN
    localizationService.registerLocalizable(mockComponent1);
    // WHEN
    localizationService.registerLocalizable(mockComponent1);

    // THEN
    assertEquals(1, localizableComponents.size(), "There should be only one component registered");
  }
}

