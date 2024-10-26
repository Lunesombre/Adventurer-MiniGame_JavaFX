package game.adventurer.ui.common.option;

import static game.adventurer.util.MiscUtil.copyNode;

import game.adventurer.common.Localizable;
import game.adventurer.service.LocalizationService;
import game.adventurer.service.LocalizedMessageService;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import javafx.scene.Node;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LanguageOption implements Option<String>, Localizable {

  private final LocalizedMessageService localizedMessageService;

  @Setter
  private String nameKey;
  private final MenuButton languageButton;
  private final Map<String, MenuItem> languageItems;
  @Getter
  private String currentLanguage;
  @Setter
  private int imageViewHeight;


  public LanguageOption(LocalizedMessageService localizedMessageService, LocalizationService localizationService, int imageViewHeight) {
    this.localizedMessageService = localizedMessageService;
    this.nameKey = "option.language.label";
    this.languageButton = new MenuButton();
    languageButton.setStyle("-fx-background-color: transparent;");
    this.languageItems = new HashMap<>();
    this.imageViewHeight = imageViewHeight;

    // Available languages
    addLanguage("fr", localizedMessageService.getMessage("language.french"), "/assets/icons/flags/france.png", localizedMessageService);
    addLanguage("en", localizedMessageService.getMessage("language.english"), "/assets/icons/flags/uk.png", localizedMessageService);

    // Define current language
    setCurrentLanguage(localizedMessageService.getCurrentLocale().getLanguage());

    // Register this class as localizable
    localizationService.registerLocalizable(this);
  }

  @Override
  public String getNameKey() {
    return nameKey;
  }

  @Override
  public Node getNode() {
    return languageButton;
  }

  @Override
  public void onValueChange(Consumer<String> listener) {
    // nothing to do here, already handled in the LanguageService
  }


  private void addLanguage(String code, String name, String flagPath, LocalizedMessageService localizedMessageService) {
    Image flagImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream(flagPath)));
    ImageView flagView = new ImageView(flagImage);
    setImageViewHeightKeepingRatio(flagView, imageViewHeight);

    MenuItem item = new MenuItem(name, flagView);
    item.setOnAction(e -> {
      log.info("Click sur {}", item.getText());
      localizedMessageService.setLanguage(code);
      setCurrentLanguage(code);
    });

    languageItems.put(code, item);
    languageButton.getItems().add(item);
  }

  private void setCurrentLanguage(String languageCode) {
    currentLanguage = languageCode;
    MenuItem currentItem = languageItems.get(languageCode);

    if (currentItem != null) {
      ImageView graphicCopy = (ImageView) copyNode(currentItem.getGraphic());
      setImageViewHeightKeepingRatio(graphicCopy, imageViewHeight);
      languageButton.setGraphic(graphicCopy);
    }
  }

  @Override
  public void updateLanguage(Locale newLocale) {
    // Update languages' names in MenuItems
    languageItems.get("fr").setText(localizedMessageService.getMessage("language.french"));
    languageItems.get("en").setText(localizedMessageService.getMessage("language.english"));
  }

  public void setImageViewHeightKeepingRatio(ImageView imageView, int height) {
    imageView.setPreserveRatio(true);
    imageView.setFitHeight(height);
  }
}
