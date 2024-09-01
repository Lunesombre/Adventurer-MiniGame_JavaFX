package game.adventurer;

import javafx.application.Application;
import lombok.Getter;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class AdventurerApplication {


  @Getter
  private static ConfigurableApplicationContext context;

  public static void main(String[] args) {
    SpringApplicationBuilder builder = new SpringApplicationBuilder(AdventurerApplication.class);
    builder.headless(false);
    context = builder.run(args);

    Application.launch(AdventurerGameApp.class);
  }

}
