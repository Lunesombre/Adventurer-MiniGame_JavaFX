package game.adventurer.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import game.adventurer.exceptions.InvalidGameStateException;
import game.adventurer.model.Tile.Type;
import game.adventurer.model.enums.Move;
import game.adventurer.model.enums.MoveResult;
import game.adventurer.service.LocalizedMessageService;
import game.adventurer.util.MiscUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@Slf4j
@ExtendWith(MockitoExtension.class)
class GameMapTest {

  private GameMap gameMap;

  private Tile[][] grid;

  @Mock
  private Adventurer adventurer;

  @Mock
  private Treasure treasure;

  private static final int MAP_WIDTH = 10;
  private static final int MAP_HEIGHT = 10;
  private static final int X = 5;
  private static final int Y = 5;
  private static final int OOB_X = MAP_WIDTH + 3;
  private static final int OOB_Y = -3;
  private static final Integer NULL_COORDINATE = null;


  @BeforeEach
  void setUp() {
    grid = new Tile[MAP_HEIGHT][MAP_WIDTH];
    for (int y = 0; y < MAP_HEIGHT; y++) {
      for (int x = 0; x < MAP_WIDTH; x++) {
        grid[y][x] = mock(Tile.class);
      }
    }
    gameMap = new GameMap(grid, MAP_WIDTH, MAP_HEIGHT, adventurer, treasure);
  }

  // Test of getTileTypeAt
  // 1 - normal behavior
  @Test
  @DisplayName("getTileTypeAt should return the correct Type")
  void testGetTileTypeAt_normalBehavior_shouldReturnTheTileType() {
    // GIVEN a GameMap
    // WHEN
    when(grid[Y][X].getType()).thenReturn(Type.PATH);
    Type result = gameMap.getTileTypeAt(X, Y);
    // THEN
    assertEquals(Type.PATH, result, "getTileTypeAt should return Type.PATH in this case");

  }

  //   2a - x and/or y aren't in grid
  @Test
  @DisplayName("getTileTypeAt should throw IndexOutOfBoundsException on out of bounds coordinates")
  void testGetTileTypeAt_outOfBoundsCoordinates_shouldThrowException() {
    // GIVEN a GameMap
    // WHEN & THEN
    assertThrows(IndexOutOfBoundsException.class, () -> gameMap.getTileTypeAt(OOB_X, 0),
        "Should throw IndexOutOfBoundsException for out of bounds X");

    assertThrows(IndexOutOfBoundsException.class, () -> gameMap.getTileTypeAt(0, OOB_Y),
        "Should throw IndexOutOfBoundsException for out of bounds Y");

    assertThrows(IndexOutOfBoundsException.class, () -> gameMap.getTileTypeAt(OOB_X, OOB_Y),
        "Should throw IndexOutOfBoundsException for out of bounds X and Y");

  }

  // 2b - x and/or y are null
  @Test
  @DisplayName("getTileTypeAt should throw NullPointerException when coordinates are null")
  void testGetTileTypeAt_nullCoordinates_shouldThrowException() {
    // Note: If the method signature were to be changed to accept Integer parameters,
    // ensure that null inputs still result in a NullPointerException to maintain
    // consistent behavior and backward compatibility.
    // GIVEN a GameMap
    // WHEN & THEN
    assertThrows(NullPointerException.class, () -> {
      gameMap.getTileTypeAt(NULL_COORDINATE, 0); // here the unboxing of the null Integer produces a NPE.
    }, "Should throw NullPointerException for null x coordinate");

    assertThrows(NullPointerException.class, () -> gameMap.getTileTypeAt(0, NULL_COORDINATE),
        "Should throw NullPointerException for null y coordinate");

    assertThrows(NullPointerException.class, () -> gameMap.getTileTypeAt(NULL_COORDINATE, NULL_COORDINATE),
        "Should throw NullPointerException for both null x and y coordinates");
  }

  // 3 - grid location .getType is null
  @Test
  @DisplayName("getTileTypeAt should handle unexpected null return")
  void testGetTileTypeAt_UnexpectedReturnType_ShouldHandleGracefully() {
    // Test with null as TileType
    Tile mockTile = mock(Tile.class);
    when(mockTile.getType()).thenReturn(null);
    // Replace the tile with the Mock
    grid[Y][X] = mockTile;

    assertThrows(NullPointerException.class, () -> gameMap.getTileTypeAt(X, Y), "Should throw NullPointerException when getType() returns null");
  }


  // Test of moveAdventurer
  // 1 - normal move behavior
  @Test
  @DisplayName("Normal move behavior")
  void testMoveAdventurer_NormalMove() {
    // GIVEN
    when(adventurer.getTileX()).thenReturn(5);
    when(adventurer.getTileY()).thenReturn(5);
    when(adventurer.move(Move.RIGHT)).thenReturn(true);
    // simulates that the Tile where the Adventurer's try to move is of Type.PATH
    when(grid[Y][X + 1].getType()).thenReturn(Type.PATH);
    // WHEN
    MoveResult result = gameMap.moveAdventurer(Move.RIGHT);
    // THEN
    assertEquals(MoveResult.MOVED, result, "The MoveResult should be MOVED");

    // Verifies there's only one call to move(1,0)
    verify(adventurer, times(1)).move(Move.RIGHT);

    // Verifies there's no call of move on other values
    for (Move move : Move.values()) {
      if (move != Move.RIGHT) {
        verify(adventurer, never()).move(move);
      }
    }
  }

  // 1b - out of bound move
  @Test
  @DisplayName("Out of bounds move")
  void testMoveAdventurer_OutOfBounds() {
    when(adventurer.getTileX()).thenReturn(MAP_WIDTH - 1);
    when(adventurer.getTileY()).thenReturn(MAP_HEIGHT - 1);

    MoveResult result = gameMap.moveAdventurer(Move.RIGHT);

    assertEquals(MoveResult.OUT_OF_BOUNDS, result, "The MoveResult should be OUT_OF_BOUNDS");
    for (Move move : Move.values()) {
      verify(adventurer, never()).move(move);
    }

  }

  // 1c - !isValid move if cause is wood
  @Test
  @DisplayName("Invalid move due to WOOD tile")
  void testMoveAdventurer_InvalidMoveWood() {
    // GIVEN
    // Using a real Adventurer object to better
    Adventurer realAdventurer = new Adventurer("TestAdventurer", X, Y, 5, 1);
    gameMap.setAdventurer(realAdventurer);
    log.debug("Adventurer's health: {}", gameMap.getAdventurer().getHealth());
    when(grid[Y][X + 1].getType()).thenReturn(Type.WOOD);
    // WHEN
    MoveResult result = gameMap.moveAdventurer(Move.RIGHT);
    // THEN
    log.debug("Adventurer's health after move: {}", realAdventurer.getHealth());
    assertEquals(MoveResult.WOUNDED, result, "The MoveResult should be WOUNDED");
    assertEquals(4, realAdventurer.getHealth(), "The Adventurer's health should be reduced by one");
  }

  // 1d - !isValid move if cause is else
  @Test
  @DisplayName("Invalid move for other reasons")
  void testMoveAdventurer_InvalidMoveOther() {
    // GIVEN
    when(adventurer.getTileX()).thenReturn(X);
    when(adventurer.getTileY()).thenReturn(Y);
    // small trick: first time it's called it return the wood type thus failing the isValidMove evaluation,
    // then return PATH on checking while it isn't valid thus not triggering the WoodWound mechanic and entering the else
    when(grid[Y][X + 1].getType()).thenReturn(Type.WOOD, Type.PATH);

    // WHEN
    MoveResult result = gameMap.moveAdventurer(Move.RIGHT);
    // THEN
    assertEquals(MoveResult.BLOCKED, result, "The MoveResult should be BLOCKED");
    for (Move move : Move.values()) {
      if (move != Move.RIGHT) {
        verify(adventurer, never()).move(move);
      }
    }
  }

  // 2 - Move is null
  @Test
  @DisplayName("Move with null coordinates")
  void testMoveAdventurer_NullMove() {
    assertThrows(NullPointerException.class, () -> gameMap.moveAdventurer(null),
        "Should throw a NullPointerException on attempting to move with a null Move");
  }

  // 3 - initial position of the Adventurer is impossible
  @Test
  @DisplayName("Should throw InvalidGameStateException and call handleInvalidGameState when Adventurer's initial position before a Move is an invalid position")
  void testMoveAdventurer_InvalidPosition_ThrowsException() {
    // GIVEN
    try (MockedStatic<MiscUtil> mockedMiscUtils = mockStatic(MiscUtil.class);
        MockedStatic<LocalizedMessageService> ignored = mockStatic(LocalizedMessageService.class)
    ) {
      when(adventurer.getTileX()).thenReturn(-1); // Invalid position
      when(adventurer.getTileY()).thenReturn(-1);
      when(grid[0][0].getType()).thenReturn(Type.PATH);
      // Mock LocalizedMessageService
      LocalizedMessageService mockService = mock(LocalizedMessageService.class);
      when(LocalizedMessageService.getInstance()).thenReturn(mockService);
      // this needs a mock to be allowed, thus the MockedStatic<LocalizedMessageService> ignored
      when(mockService.getMessage("error.impossibleAdventurerLocation"))
          .thenReturn("Invalid adventurer location");

      // WHEN
      gameMap.moveAdventurer(Move.RIGHT);
      // THEN
      // Verifying that the method handleInvalidGameState was called
      try {
        mockedMiscUtils.verify(() ->
            MiscUtil.handleInvalidGameState(
                eq(GameMap.class),
                any(InvalidGameStateException.class)
            ), times(1)
        );
      } catch (AssertionError e) {
        throw new AssertionError(
            "MiscUtil.handleInvalidGameState should be called (only once) when initial Adventurer's position on a move is invalid.");
      }
    }
  }

}
