package game.adventurer.model;

import static game.adventurer.util.MiscUtil.sanitizeString;

import game.adventurer.model.enums.DifficultyLevel;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import lombok.Getter;

@Getter
public class Score implements Comparable<Score> {

  private final String adventurerName;
  private final int scoreValue;
  private final LocalDateTime date;
  private final int movesCount;
  private final DifficultyLevel difficultyLevel;

  public Score(String adventurerName, int scoreValue, LocalDateTime date, int movesCount, DifficultyLevel difficultyLevel) {
    this.adventurerName = adventurerName;
    this.scoreValue = scoreValue;
    this.date = date;
    this.movesCount = movesCount;
    this.difficultyLevel = difficultyLevel;
  }


  @Override
  public int compareTo(Score other) {
    int scoreComparison = Integer.compare(other.scoreValue, this.scoreValue);
    if (scoreComparison != 0) {
      return scoreComparison;
    }
    // if scores are equals, fewer moves is considered better
    return Integer.compare(this.movesCount, other.movesCount);
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Score score1 = (Score) o;
    return scoreValue == score1.scoreValue &&
        movesCount == score1.movesCount &&
        Objects.equals(adventurerName, score1.adventurerName) &&
        Objects.equals(date, score1.date) &&
        difficultyLevel == score1.difficultyLevel;
  }

  @Override
  public int hashCode() {
    return Objects.hash(adventurerName, scoreValue, date, movesCount, difficultyLevel);
  }

  @Override
  public String toString() {
    return String.format("%s,%d,%s,%d,%s",
        adventurerName,
        scoreValue,
        date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
        movesCount,
        difficultyLevel);
  }

  public static Score sanitizedFromString(String string) {
    String[] parts = string.split(",");
    if (parts.length != 5) {
      throw new IllegalArgumentException("Invalid number of parts in scoreValue entry");
    }
    String adventurerName = sanitizeString(parts[0], 20);
    int score = Integer.parseInt(parts[1]);
    LocalDateTime date = LocalDateTime.parse(parts[2], DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    int movesCount = Integer.parseInt(parts[3]);
    DifficultyLevel difficultyLevel = DifficultyLevel.valueOf(parts[4]);
    return new Score(
        adventurerName,
        score,
        date,
        movesCount,
        difficultyLevel
    );
  }
}
