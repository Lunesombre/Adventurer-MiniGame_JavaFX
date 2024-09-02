package game.adventurer.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Singleton class to handle some scene related config for the app.
 */
@Component
public final class AppConfig {

  private static AppConfig instance;

  @Getter
  @Value("${app.title.font.path}")
  private String titleFontPath;


  private AppConfig() {
  }

  @PostConstruct
  private void init() {
    setInstance(this);
  }

  /**
   * Sets the singleton instance of AppConfig. This method is synchronized to ensure thread-safe initialization of the instance.
   *
   * @param config The AppConfig instance to be set as the singleton instance.
   * @throws NullPointerException if the provided config is null.
   */
  private static synchronized void setInstance(AppConfig config) {
    if (config == null) {
      throw new NullPointerException("Config instance cannot be null");
    }
    instance = config;
  }

  /**
   * Retrieves the singleton instance of AppConfig. This method is synchronized to ensure thread-safe access to the instance.
   *
   * @return The singleton instance of AppConfig.
   * @throws IllegalStateException if the AppConfig instance has not been initialized.
   */
  public static synchronized AppConfig getInstance() {
    if (instance == null) {
      throw new IllegalStateException("AppConfig has not been initialized");
    }
    return instance;
  }
}


