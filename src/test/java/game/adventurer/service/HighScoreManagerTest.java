package game.adventurer.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import game.adventurer.model.Score;
import game.adventurer.model.enums.DifficultyLevel;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;


@Slf4j
@ExtendWith(MockitoExtension.class)
class HighScoreManagerTest {

  private static final int MAX_SCORES = 30;

  private HighScoreManager highScoreManager;

  @TempDir
  Path tempDir;

  @BeforeEach
  void setUp() {
    // Temp path for tests
    System.setProperty("user.home", tempDir.toString());
    highScoreManager = new HighScoreManager();
  }

  @Test
  void givenNewHighScoreManager_whenInitialized_thenHighScoresAreNeitherNullNorEmpty() {
    // GIVEN a new HighScoreManager is created
    // WHEN getting HighScores THEN
    assertNotNull(highScoreManager.getHighScores());
    assertFalse(highScoreManager.getHighScores().isEmpty());
  }

  @Test
  @DisplayName("Should create directory successfully when it doesn't exist")
  void testDirectoryInitializer() throws Exception {
    // GIVEN: Case 1 - the directory doesn't exist
    try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
      // Configures the mock of the static Files
      mockedFiles.when(() -> Files.notExists(tempDir)).thenReturn(true);
      mockedFiles.when(() -> Files.createDirectories(tempDir)).thenReturn(tempDir);

      // Uses Reflection to access the private Method
      Method directoryInitializerMethod = HighScoreManager.class.getDeclaredMethod("directoryInitializer", Path.class);
      directoryInitializerMethod.setAccessible(true);

      // WHEN
      boolean result = (boolean) directoryInitializerMethod.invoke(highScoreManager, tempDir);
      // THEN
      assertTrue(result, "The directory should be created successfully");
      try {
        mockedFiles.verify(() -> Files.createDirectories(tempDir), times(1));
      } catch (AssertionError e) {
        throw new AssertionError("Files.createDirectories should be called once when the directory does not already exists");
      }
    }
  }

  @Test
  @DisplayName("Should return true without creating directory when it already exists")
  void testDirectoryInitializerWhenDirectoryExists() throws Exception {
    // GIVEN: Case 2 - directory already exists
    try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
      // Configures the mock of the static Files
      mockedFiles.when(() -> Files.notExists(tempDir)).thenReturn(false);

      // Uses Reflection to access the private Method
      Method directoryInitializerMethod = HighScoreManager.class.getDeclaredMethod("directoryInitializer", Path.class);
      directoryInitializerMethod.setAccessible(true);
      // WHEN
      boolean result = (boolean) directoryInitializerMethod.invoke(highScoreManager, tempDir);

      // THEN
      assertTrue(result, "The directory exists, the method thus should return true");
      // The Files.createDirectories shouldn't be called
      try {
        mockedFiles.verify(() -> Files.createDirectories(tempDir), never());
      } catch (AssertionError e) {
        throw new AssertionError("Files.createDirectories should not be called when the directory already exists");
      }
    }
  }

  @Test
  @DisplayName("Should return false when directory creation fails")
  void testDirectoryInitializerFailure() throws Exception {
    // GIVEN for any reason, directory creation isn't possible
    try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
      // Configures the mock of the static Files
      mockedFiles.when(() -> Files.notExists(tempDir)).thenReturn(true);
      // Simulate an exception on directory creation
      mockedFiles.when(() -> Files.createDirectories(tempDir)).thenThrow(new IOException("Failed to create directory"));

      // Uses Reflection to access the private Method
      Method directoryInitializerMethod = HighScoreManager.class.getDeclaredMethod("directoryInitializer", Path.class);
      directoryInitializerMethod.setAccessible(true);

      // WHEN
      boolean result = (boolean) directoryInitializerMethod.invoke(highScoreManager, tempDir);

      // THEN
      // createDirectories should be called once
      try {
        mockedFiles.verify(() -> Files.createDirectories(tempDir), times(1));
      } catch (AssertionError e) {
        throw new AssertionError("Files.createDirectories should be called once when the directory does not already exists");
      }
      // but directoryInitializerMethod should return false in this case
      assertFalse(result, "directoryInitializer should return false on directory creation failure");
    }
  }

  @Test
  void givenHighScoreManager_whenNewScoreAdded_thenScoreIsInHighScores() {
    // GIVEN: HighScoreManager initialized
    int initialSize = highScoreManager.getHighScores().size();

    // WHEN: new Score added
    Score newScore = new Score("TestPlayer", 1500, LocalDateTime.now(), 5, DifficultyLevel.HARD);
    highScoreManager.addScore(newScore);

    // THEN: The new Score is in the list and the list's size has increased
    assertEquals(initialSize + 1, highScoreManager.getHighScores().size());
    assertTrue(highScoreManager.getHighScores().contains(newScore));
  }

  @Test
  @DisplayName("When a new high score is added to a full list, it should replace the lowest score")
  void givenHighScoreManagerAndHighScoresListIsFull_whenNewScoreAdded_thenScoreIsInHighScores() {
    // GIVEN: HighScore already contains the max allowed number of Scores
    int minScoreValue = 100;
    for (int i = 0; i < MAX_SCORES; i++) {
      highScoreManager.addScore(new Score("Player" + i, minScoreValue + i, LocalDateTime.now(), i, DifficultyLevel.NORMAL));
    }
    int initialSize = highScoreManager.getHighScores().size();

    // WHEN: new Score added
    Score newHighScore = new Score("TestPlayer", 150, LocalDateTime.now(), 5, DifficultyLevel.HARD);
    highScoreManager.addScore(newHighScore);

    // THEN: list size shouldn't have increased, the newHighScore is in the List, the smallest highScore has been removed
    List<Score> updatedScores = highScoreManager.getHighScores();
    assertEquals(initialSize, updatedScores.size(), "List size should not have increased");
    assertTrue(updatedScores.contains(newHighScore), "The newHighScore should be in the list");
    assertFalse(updatedScores.stream().anyMatch(s -> s.getScoreValue() == minScoreValue),
        "The smallest highScore should have been removed from the list");
  }

  @Test
  @DisplayName("When a low score is added to a full list, it should not be added and the list should remain unchanged")
  void givenHighScoreManagerAndHighScoresListIsFull_whenNewScoreAdded_thenScoreIsNotInHighScores() {
    // GIVEN: HighScore already contains the max allowed number of Scores
    int minScoreValue = 100;
    for (int i = 0; i < MAX_SCORES; i++) {
      highScoreManager.addScore(new Score("Player" + i, minScoreValue + i, LocalDateTime.now(), i, DifficultyLevel.NORMAL));
    }
    List<Score> initialScores = new ArrayList<>(highScoreManager.getHighScores());

    // WHEN: new Score added
    Score newLowScore = new Score("TestPlayer", minScoreValue - 1, LocalDateTime.now(), 50, DifficultyLevel.EASY);
    highScoreManager.addScore(newLowScore);

    // THEN: list size shouldn't have increased
    List<Score> updatedScores = highScoreManager.getHighScores();
    assertEquals(initialScores, updatedScores, "the HighScore list shouldn't have changed");
    assertFalse(updatedScores.contains(newLowScore), "The new low Score shouldn't have been added to the list");
  }

  @Test
  @DisplayName("When a score with the same value but fewer moves is added to a full list, it should replace the existing score")
  void givenHighScoreManagerAndHighScoresListIsFull_whenNewScoreWithSameValueButFewerMovesAdded_thenNewScoreReplaceExisting() {
    // GIVEN: HighScore already contains the max allowed number of Scores
    int minScoreValue = 100;
    LocalDateTime olderTime = LocalDateTime.now().minusDays(MAX_SCORES);
    for (int i = 0; i < MAX_SCORES; i++) {
      highScoreManager.addScore(new Score("Player" + i, minScoreValue + i, olderTime, 10, DifficultyLevel.NORMAL));
    }
    List<Score> initialScores = new ArrayList<>(highScoreManager.getHighScores());
    Score lowestScore = initialScores.stream()
        .min(Comparator.comparingInt(Score::getScoreValue))
        .orElseThrow();
    final int effectiveMinScoreValue = lowestScore.getScoreValue(); // in case the lowest score in the loop hasn't been kept (should be the case as there's already 10 default scores)

    // WHEN: new Score with the same value but fewer moves is added
    Score newScore = new Score("TestPlayer", effectiveMinScoreValue, LocalDateTime.now(), 5, DifficultyLevel.NORMAL);
    highScoreManager.addScore(newScore);

    // THEN: new score should replace the existing one with the same score value
    List<Score> updatedScores = highScoreManager.getHighScores();
    assertEquals(MAX_SCORES, updatedScores.size(), "The HighScore list size should remain the same");
    assertFalse(updatedScores.contains(lowestScore), "The original lowest score should no longer be in the list");
    assertTrue(updatedScores.contains(newScore), "The new score with fewer moves should be in the list");

    Score updatedLowestScore = updatedScores.stream()
        .min(Comparator.comparingInt(Score::getScoreValue))
        .orElseThrow();
    assertEquals(newScore, updatedLowestScore, "The new score should now be the lowest score in the list");
  }

  @Test
  @DisplayName("When a score with the same value and more moves is added to a full list, it should not replace the existing score")
  void givenHighScoreManagerAndHighScoresListIsFull_whenNewScoreWithSameValueButMoreMovesAdded_thenExistingScoreIsKept() {
    // GIVEN: HighScore already contains the max allowed number of Scores
    int minScoreValue = 100;
    LocalDateTime oldestTime = LocalDateTime.now().minusDays(MAX_SCORES);
    for (int i = 0; i < MAX_SCORES; i++) {
      highScoreManager.addScore(new Score("Player" + i, minScoreValue + i, oldestTime.plusDays(i), 10, DifficultyLevel.NORMAL));
    }
    List<Score> initialScores = new ArrayList<>(highScoreManager.getHighScores());
    Score lowestScore = initialScores.stream()
        .min(Comparator.comparingInt(Score::getScoreValue))
        .orElseThrow();

    final int effectiveMinScoreValue = lowestScore.getScoreValue(); // in case the lowest score in the loop hasn't been kept (should be the case as there's already 10 default scores)

    // WHEN: new Score with the same value but more moves is added
    Score newScore = new Score("TestPlayer", effectiveMinScoreValue, LocalDateTime.now(), 15, DifficultyLevel.NORMAL);
    highScoreManager.addScore(newScore);

    // THEN: The List should remain unchanged, keeping the existing score
    List<Score> updatedScores = highScoreManager.getHighScores();
    assertEquals(initialScores, updatedScores, "The HighScore list shouldn't have changed");
    assertTrue(updatedScores.contains(lowestScore), "The original lowest score should still be in the list");
    assertFalse(updatedScores.contains(newScore), "The new score with more moves shouldn't have been added to the list");
  }

  @Test
  @DisplayName("When a score with the same value and movesCount as the lowest is added to a full list, it should not replace the existing score - keeping the older one")
  void givenHighScoreManagerAndHighScoresListIsFull_whenNewScoreWithSameValueAdded_thenExistingScoreIsKept() {
    // GIVEN: HighScore already contains the max allowed number of Scores
    int minScoreValue = 100;
    LocalDateTime olderTime = LocalDateTime.now().minusDays(MAX_SCORES);
    for (int i = 0; i < MAX_SCORES; i++) {
      highScoreManager.addScore(new Score("Player" + i, minScoreValue + i, olderTime, 10, DifficultyLevel.NORMAL));
    }
    List<Score> initialScores = new ArrayList<>(highScoreManager.getHighScores());
    Score lowestScore = initialScores.stream()
        .min(Comparator.comparingInt(Score::getScoreValue))
        .orElseThrow();

    final int effectiveMinScoreValue = lowestScore.getScoreValue(); // in case the lowest score in the loop hasn't been kept (should be the case as there's already 10 default scores)

    // WHEN: new Score with the same value as the lowest is added
    Score newScore = new Score("TestPlayer", effectiveMinScoreValue, LocalDateTime.now(), 10, DifficultyLevel.NORMAL);
    highScoreManager.addScore(newScore);

    // THEN: the list should remain unchanged, keeping the older score
    List<Score> updatedScores = highScoreManager.getHighScores();
    assertEquals(initialScores, updatedScores, "The HighScore list shouldn't have changed");
    assertTrue(updatedScores.contains(lowestScore), "The original lowest score should still be in the list");
    assertFalse(updatedScores.contains(newScore), "The new score with the same value shouldn't have been added to the list");
    assertEquals(lowestScore.getDate(), updatedScores.stream()
        .filter(s -> s.getScoreValue() == effectiveMinScoreValue && s.getMovesCount() == 10)
        .findFirst()
        .orElseThrow()
        .getDate(), "The date of the lowest score should be the older one");
  }


  @Test
  @DisplayName("When getTopScores is called, it should return the correct number of sorted scores")
  void givenHighScoreManager_whenGetTopScoresCalled_thenReturnsCorrectNumberOfScores() {
    // Given: An initialized HighScoreManager
    // When: top 5 scores requested
    int numberOfTopScoreRequested = 5;
    List<Score> topScores = highScoreManager.getTopScores(numberOfTopScoreRequested);

    // Then: 5 scores are returned and they are sorted
    assertEquals(numberOfTopScoreRequested, topScores.size(),
        "Should return exactly the number of top scores requested (" + numberOfTopScoreRequested + ")");
    for (int i = 0; i < topScores.size() - 1; i++) {
      assertTrue(topScores.get(i).getScoreValue() >= topScores.get(i + 1).getScoreValue(),
          "Score at position " + i + " should be greater than or equal to score at position " + (i + 1));
    }
  }

  @Test
  @DisplayName("When resetHighScores is called, scores should be reset to default values")
  void resetHighScoresResetsToDefaultValues() {
    // Given: An initialized HighScoreManager

    // When: High scores are reset
    highScoreManager.resetHighScores();

    // Then: The scores are reset to default values
    List<Score> resetScores = highScoreManager.getHighScores();
    assertEquals(10, resetScores.size(), "Should have 10 default scores after reset");
    assertEquals("Michel", resetScores.getFirst().getAdventurerName(), "First default score should be for Michel");
    assertEquals(1000, resetScores.getFirst().getScoreValue(), "Michel's score should be 1000");
  }

  @Test
  @DisplayName("When a new high score is added, scores should be sorted correctly")
  void addingHighScoreSortsScoresCorrectly() {
    // Given: An initialized HighScoreManager

    // When: A new high score top high Score is added
    Score highScore = new Score("HighScorer", 2000, LocalDateTime.now(), 1, DifficultyLevel.HARD);
    highScoreManager.addScore(highScore);

    // Then: The new score is in the first position
    assertEquals(highScore, highScoreManager.getHighScores().getFirst(), "New high score should be at the top of the list");
  }

  @Test
  @DisplayName("When many scores are added, the maximum score limit should be respected")
  void addingManyScoresRespectsMaxScoreLimit() {
    // Given: An initialized HighScoreManager

    // When: We add many scores
    for (int i = 0; i < 50; i++) {
      highScoreManager.addScore(new Score("Player" + i, i, LocalDateTime.now(), i, DifficultyLevel.NORMAL));
    }

    // Then: The maximum number of scores is respected
    assertEquals(MAX_SCORES, highScoreManager.getHighScores().size(), "Should not exceed the maximum number of scores (" + MAX_SCORES + ")");
  }

  @Test
  @DisplayName("When scores are saved, they should be able to be loaded again")
  void savedScoresCanBeLoadedAgain() {
    // Given: An initialized HighScoreManager
    Score newScore = new Score("SaveTest", 1750, LocalDateTime.now(), 10, DifficultyLevel.HARD);

    // When: We add a new score and reinitialize the HighScoreManager
    highScoreManager.addScore(newScore);
    highScoreManager = new HighScoreManager();

    // Then: The saved score can be found in the loaded high scores
    assertTrue(highScoreManager.getHighScores().stream()
            .anyMatch(score -> "SaveTest".equals(score.getAdventurerName()) && score.getScoreValue() == 1750),
        "Saved score should be present in the loaded high scores");
  }


  @Test
  @DisplayName("When resetHighScores encounters an IOException, it should log an error")
  void resetHighScoresHandlesIOException() {
    // Given: A HighScoreManager with a mocked file system that throws an IOException

    try (MockedStatic<Files> files = mockStatic(Files.class)) {
      files.when(() -> Files.delete(any(Path.class))).thenThrow(new IOException("Mocked IO Exception"));

      // When & Then: resetHighScores is called, it should not throw an exception
      assertDoesNotThrow(() -> highScoreManager.resetHighScores(),
          "resetHighScores should handle IOException internally");

      // An error should be logged if the test passed as expected
    }
  }


  @Test
  @DisplayName("When loadScores encounters an IOException, it should initialize with default scores")
  void loadScoresHandlesIOException() {
    // Given: A mocked BufferedReader that throws an IOException on readLine
    try (MockedConstruction<FileReader> mockedFileReader = mockConstruction(FileReader.class);
        MockedConstruction<BufferedReader> mockedBufferedReader = mockConstruction(BufferedReader.class,
            (mock, context) -> {
              when(mock.readLine())
                  .thenThrow(new IOException("Mocked IO Exception"));
            })) {

      // When: A new HighScoreManager is created (which calls loadScores internally)
      HighScoreManager manager = new HighScoreManager();

      // Then: The high scores should be initialized with default values
      List<Score> scores = manager.getHighScores();
      assertEquals(10, scores.size(), "Should have 10 scores after IOException");
      assertEquals("Michel", scores.getFirst().getAdventurerName(), "First default score should be for Michel");
      assertEquals(1000, scores.getFirst().getScoreValue(), "Michel's score should be 1000");

      // Verify that FileReader was attempted to be constructed
      assertEquals(1, mockedFileReader.constructed().size(), "FileReader should have been constructed once");
      // Verify that BufferedReader was attempted to be constructed
      assertEquals(1, mockedBufferedReader.constructed().size(), "BufferedReader should have been constructed once");

    }
  }

  @Test
  @DisplayName("When loadScores reads from a file successfully, it should load the scores")
  void loadScoresReadsFileSuccessfully() {
    // Given: A mocked BufferedReader that returns some predefined scores
    try (MockedConstruction<FileReader> mockedFileReader = mockConstruction(FileReader.class);
        MockedConstruction<BufferedReader> mockedBufferedReader = mockConstruction(BufferedReader.class,
            (mock, context) -> {
              when(mock.readLine())
                  .thenReturn("TestPlayer1,500,2023-01-01T12:00:00,10,NORMAL")
                  .thenReturn("TestPlayer2,400,2023-01-02T12:00:00,15,EASY")
                  .thenReturn(null); // Simulate end of file
            })) {

      // When: A new HighScoreManager is created (which calls loadScores internally)
      HighScoreManager manager = new HighScoreManager();

      // Then: The high scores should be loaded from the "file"
      List<Score> scores = manager.getHighScores();
      assertEquals(2, scores.size(), "Should have 2 scores loaded from file");
      assertEquals("TestPlayer1", scores.get(0).getAdventurerName(), "First score should be for TestPlayer1");
      assertEquals(500, scores.get(0).getScoreValue(), "TestPlayer1's score should be 500");
      assertEquals("TestPlayer2", scores.get(1).getAdventurerName(), "Second score should be for TestPlayer2");
      assertEquals(400, scores.get(1).getScoreValue(), "TestPlayer2's score should be 400");

      // Verify that FileReader and BufferedReader were constructed
      assertEquals(1, mockedFileReader.constructed().size(), "FileReader should have been constructed once");
      assertEquals(1, mockedBufferedReader.constructed().size(), "BufferedReader should have been constructed once");
    }
  }

  @Test
  @DisplayName("When saveScores encounters an IOException, it should log an error")
  void saveScoresHandlesIOException() {
    try (MockedConstruction<FileWriter> mockedFileWriter = mockConstruction(FileWriter.class);
        MockedConstruction<BufferedWriter> mockedBufferedWriter = mockConstruction(BufferedWriter.class,
            (mock, context) -> {
              doThrow(new IOException("Mocked IO Exception"))
                  .when(mock).write(anyString());
              doThrow(new IOException("Mocked IO Exception"))
                  .when(mock).newLine();
            })) {

      // GIVEN: A HighScoreManager with some scores
      HighScoreManager manager = new HighScoreManager();
      // WHEN: Trying to add a score (which calls saveScores internally)
      manager.addScore(new Score("TestPlayer", 100, LocalDateTime.now(), 10, DifficultyLevel.NORMAL));

      // THEN: An error should be logged
      // Verifies that the BufferedWriter was constructed and its methods were called
      assertEquals(1, mockedFileWriter.constructed().size(), "FileWriter should have been constructed once");
      assertEquals(1, mockedBufferedWriter.constructed().size(), "BufferedWriter should have been constructed once");
      // Verifies that BufferedWriter.write was called once, and that newLine() was never reached
      verify(mockedBufferedWriter.constructed().getFirst(), times(1)).write(anyString());
      verify(mockedBufferedWriter.constructed().getFirst(), never()).newLine();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }


  }
}


