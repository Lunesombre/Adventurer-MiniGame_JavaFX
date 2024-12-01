package game.adventurer.ui.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import game.adventurer.service.LocalizationService;
import game.adventurer.service.LocalizedMessageService;
import javafx.application.HostServices;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.testfx.framework.junit5.ApplicationTest;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class CreditsOverlayTest extends ApplicationTest {

  private CreditsOverlay creditsOverlay;

  @Mock
  private LocalizationService localizationService;

  public static final double INITIAL_WIDTH = 1440.0;
  public static final double INITIAL_HEIGHT = 900.0;
  public static final double NEW_WIDTH = 1920.0;
  public static final double NEW_HEIGHT = 1080.0;
  @Mock
  private ReloadableResourceBundleMessageSource messageSource;

  @Mock
  private ApplicationEventPublisher eventPublisher;

  @Override
  public void start(Stage stage) throws Exception {
    // Initializing JavaFX
  }

  @BeforeEach
  void setup() {
    LocalizedMessageService messageService = LocalizedMessageService.getInstance();
    messageService.initialize(messageSource, eventPublisher);
    HostServices hostServices = mock(HostServices.class);
    creditsOverlay = new CreditsOverlay(hostServices, localizationService, INITIAL_WIDTH, INITIAL_HEIGHT);
  }

  // TESTS ON SHOW()
  @Test
  @DisplayName("CreditsOverlay show() - displays the CreditsOverlay")
  void testShow() {
    // Given: CreditsOverlay is initially hidden and added to a parent
    StackPane parent = new StackPane(new Text("Test"), creditsOverlay, new Text("Test2"));
    creditsOverlay.setVisible(false);

    // When: show() is called
    creditsOverlay.show();

    // Then: CreditsOverlay should be visible and in the foreground
    assertTrue(creditsOverlay.isVisible(), "CreditsOverlay should be visible");
    int lastIndex = parent.getChildren().size() - 1;
    assertEquals(lastIndex, parent.getChildren().indexOf(creditsOverlay), "CreditsOverlay should be in the foreground");
  }

  // TESTS ON HIDE()
  @Test
  @DisplayName("CreditsOverlay hide() - hides the CreditsOverlay")
  void testHide() {
    // Given: CreditsOverlay is initially visible and added to a parent
    StackPane parent = new StackPane(new Text("Test"), creditsOverlay, new Text("Test2"));
    creditsOverlay.setVisible(true);

    // When: hide() is called
    creditsOverlay.hide();

    // Then: CreditsOverlay should be hidden and in the background
    assertFalse(creditsOverlay.isVisible(), "L'CreditsOverlay shouldn't be visible");
    assertEquals(0, parent.getChildren().indexOf(creditsOverlay), "CreditsOverlay should be at the back of its parent's children list");
  }

  // TESTS ON updateSize()

  @Test
  @DisplayName("CreditsOverlay updateSize() - updates the CreditsOverlay dimensions")
  void testUpdateSize() {
    // Given: a CreditsOverlay set on initial size.

    // When : updateSize() is called with new dimensions

    creditsOverlay.updateSize(NEW_WIDTH, NEW_HEIGHT);

    // Then : Les dimensions de l'overlay devraient être mises à jour
    assertEquals(NEW_WIDTH, creditsOverlay.getPrefWidth(), "La largeur préférée devrait être mise à jour");
    assertEquals(NEW_HEIGHT, creditsOverlay.getPrefHeight(), "La hauteur préférée devrait être mise à jour");
    assertEquals(NEW_WIDTH, creditsOverlay.getMinWidth(), "La largeur minimale devrait être mise à jour");
    assertEquals(NEW_HEIGHT, creditsOverlay.getMinHeight(), "La hauteur minimale devrait être mise à jour");
    assertEquals(NEW_WIDTH, creditsOverlay.getMaxWidth(), "La largeur maximale devrait être mise à jour");
    assertEquals(NEW_HEIGHT, creditsOverlay.getMaxHeight(), "La hauteur maximale devrait être mise à jour");
  }
}

