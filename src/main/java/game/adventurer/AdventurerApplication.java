package game.adventurer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class AdventurerApplication {


  public static void main(String[] args) {

    ConfigurableApplicationContext springContext = SpringApplication.run(AdventurerApplication.class, args);

    // Lancer l'application JavaFX avec le contexte Spring
    AdventurerGameApp.launchWithSpringContext(args, springContext);
  }

}
