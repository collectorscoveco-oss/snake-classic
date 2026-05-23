package com.mexcould.snake;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GameRulesTest {
    @Test public void wrapCoordinateMovesPastRightEdgeToLeftEdge() {
        assertEquals(0, GameRules.wrapCoordinate(18, 18));
    }

    @Test public void wrapCoordinateMovesPastLeftEdgeToRightEdge() {
        assertEquals(17, GameRules.wrapCoordinate(-1, 18));
    }

    @Test public void wallModeHighScoreKeyIncludesDifficultyAndMode() {
        GameOptions options = new GameOptions();
        options.difficulty = GameOptions.Difficulty.HARD;
        options.wallMode = GameOptions.WallMode.SOLID;
        assertEquals("high_score_HARD_SOLID", options.highScoreKey());
    }

    @Test public void wrapModeHighScoreKeyIsSeparateFromWallMode() {
        GameOptions options = new GameOptions();
        options.difficulty = GameOptions.Difficulty.HARD;
        options.wallMode = GameOptions.WallMode.WRAP;
        assertEquals("high_score_HARD_WRAP", options.highScoreKey());
    }

    @Test public void compactBoundsRejectsTallOnlyCoordinates() {
        GameOptions options = new GameOptions();
        options.layout = GameOptions.Layout.COMPACT;
        assertFalse(GameRules.isInsideBoard(19, 27, options.cols(), options.rows()));
    }

    @Test public void compactBoundsAcceptsCompactLastCell() {
        GameOptions options = new GameOptions();
        options.layout = GameOptions.Layout.COMPACT;
        assertTrue(GameRules.isInsideBoard(17, 21, options.cols(), options.rows()));
    }
}
