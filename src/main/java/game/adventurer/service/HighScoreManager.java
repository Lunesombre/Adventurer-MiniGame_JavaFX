package game.adventurer.service;

import game.adventurer.model.Score;
import game.adventurer.model.enums.DifficultyLevel;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class HighScoreManager {


  private static final String FILE_NAME = "adventurer_highScores.log";

  private static final int MAX_SCORES = 30;
  private final String filePath;
  private List<Score> highScores;

  public HighScoreManager() {
    String userHome = System.getProperty("user.home");
    this.filePath = Paths.get(userHome, ".adventurer", FILE_NAME).toString();
    initializeDirectory();
    highScores = new ArrayList<>();
    loadScores();
    if (highScores.isEmpty()) {
      initializeWithDefaultScores();
    }
  }

  private void initializeDirectory() {
    File directory = new File(Paths.get(System.getProperty("user.home"), ".adventurer").toString());
    if (!directory.exists()) {
      log.info("Création du répertoire des HighScores");
      directory.mkdirs();
    }
  }

  public void addScore(Score score) {
    highScores.add(score);
    Collections.sort(highScores);
    if (highScores.size() > MAX_SCORES) {
      highScores = highScores.subList(0, MAX_SCORES);
    }
    saveScores();
  }

  private void saveScores() {
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
      for (Score score : highScores) {
        writer.write(score.toString());
        writer.newLine();
      }
    } catch (IOException e) {
      log.error("Erreur lors de la sauvegarde des scores : {}", e.getMessage());
    }
  }

  private void loadScores() {
    File file = new File(filePath);
    if (!file.exists()) {
      initializeWithDefaultScores();
      return;
    }
    try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
      String line;
      while ((line = reader.readLine()) != null) {
        highScores.add(Score.sanitizedFromString(line));
      }
      Collections.sort(highScores);

    } catch (IOException e) {
      log.warn("Erreur lors du chargement des scores : {}", e.getMessage());
      initializeWithDefaultScores();
    }
  }

  public List<Score> getTopScores(int n) {
    return highScores.subList(0, Math.min(n, highScores.size()));
  }

  private void initializeWithDefaultScores() {
    highScores.addAll(Arrays.asList(defaultScores));
    Collections.sort(highScores);
    saveScores();
  }

  public void resetHighScores() {
    try {
      // 1. Delete the existing highScores' file
      Files.delete(Path.of(filePath));
      // 2. Reinit high scores with default values and save it
      highScores.clear();
      highScores.addAll(Arrays.asList(defaultScores));
      saveScores();
      log.info("High scores successfully reinitialized");
    } catch (IOException e) {
      log.error("HighScore file not deleted : {}", e.getMessage(), e);
    }
  }

  private static final Score[] defaultScores = {
      new Score("Michel", 1000,
          LocalDateTime.of(1957, 10, 4, 0, 0), 3, DifficultyLevel.HARD), // Sputnik 1
      new Score("Maxine", 950,
          LocalDateTime.of(1965, 3, 18, 0, 0), 27, DifficultyLevel.EASY), // First EVA (Leonov)
      new Score("Laura", 900,
          LocalDateTime.of(1963, 6, 16, 0, 0), 42, DifficultyLevel.NORMAL), // 1st woman in space
      new Score("Romain", 850,
          LocalDateTime.of(2021, 2, 18, 0, 0), 7, DifficultyLevel.NORMAL), // Perseverance on Mars
      new Score("Jeb", 700,
          LocalDateTime.of(1961, 4, 12, 0, 0), 70, DifficultyLevel.NORMAL), // 1st man in space
      new Score("Ishtar", 500,
          LocalDateTime.of(1970, 12, 15, 0, 0), 80, DifficultyLevel.NORMAL), // Venera 7
      new Score("Enki", 450,
          LocalDateTime.of(1966, 3, 16, 0, 0), 90, DifficultyLevel.NORMAL), // Gemini8 - Agena RdV
      new Score("Nanna", 400,
          LocalDateTime.of(1969, 7, 20, 0, 0), 100, DifficultyLevel.NORMAL), // Apollo 11 on the Moon
      new Score("Ninurta", 300,
          LocalDateTime.of(2004, 7, 1, 0, 0), 110, DifficultyLevel.NORMAL), // Cassini-Huygens orbiting Saturn
      new Score("Marduk", 200,
          LocalDateTime.of(2023, 4, 14, 0, 0), 1337, DifficultyLevel.NORMAL) // JUICE launch
  };

}

