package game.adventurer.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Singleton class to handle the config for javaFX's scenes
 */
@Component
public final class SceneConfig {

  private static SceneConfig instance;

  @Getter
  @Value("${app.title.font.path}")
  private String titleFontPath;
  
  @Getter
  private final int width;
  @Getter
  private final int height;

  @SuppressWarnings("unused") // This constructor is used by Spring for dependency injection
  private SceneConfig(@Value("${scene.window.width}") int width,
      @Value("${scene.window.height}") int height) {
    if (width <= 0 || height <= 0) {
      throw new IllegalArgumentException("Width and height must be positive");
    }
    this.width = width;
    this.height = height;
  }

  @PostConstruct
  private void init() {
    setInstance(this);
  }

  /**
   * Sets the singleton instance of SceneConfig. This method is synchronized to ensure thread-safe initialization of the instance.
   *
   * @param config The SceneConfig instance to be set as the singleton instance.
   * @throws NullPointerException if the provided config is null.
   */
  private static synchronized void setInstance(SceneConfig config) {
    if (config == null) {
      throw new NullPointerException("Config instance cannot be null");
    }
    instance = config;
  }

  /**
   * Retrieves the singleton instance of SceneConfig. This method is synchronized to ensure thread-safe access to the instance.
   *
   * @return The singleton instance of SceneConfig.
   * @throws IllegalStateException if the SceneConfig instance has not been initialized.
   */
  public static synchronized SceneConfig getInstance() {
    if (instance == null) {
      throw new IllegalStateException("SceneConfig has not been initialized");
    }
    return instance;
  }
}


