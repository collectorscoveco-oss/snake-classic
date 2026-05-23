package com.mexcould.snake;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GameOptionsTest {
    @Test
    public void difficultyCycleWrapsAroundAndChangesSpeed() {
        GameOptions options = new GameOptions();
        assertEquals(GameOptions.Difficulty.NORMAL, options.difficulty);
        assertEquals(145L, options.baseDelayMs());

        options.nextDifficulty();
        assertEquals(GameOptions.Difficulty.HARD, options.difficulty);
        assertEquals(105L, options.baseDelayMs());

        options.nextDifficulty();
        assertEquals(GameOptions.Difficulty.EASY, options.difficulty);
        assertEquals(185L, options.baseDelayMs());
    }

    @Test
    public void controlCycleIncludesSwipeButtonsAndTapTurn() {
        GameOptions options = new GameOptions();
        assertEquals(GameOptions.Controls.SWIPE, options.controls);
        options.nextControls();
        assertEquals(GameOptions.Controls.BUTTONS, options.controls);
        options.nextControls();
        assertEquals(GameOptions.Controls.TAP_TURN, options.controls);
        options.nextControls();
        assertEquals(GameOptions.Controls.SWIPE, options.controls);
    }

    @Test
    public void togglesSoundAndAdPlaceholder() {
        GameOptions options = new GameOptions();
        assertTrue(options.soundEnabled);
        assertTrue(options.adsEnabled);
        options.toggleSound();
        options.toggleAds();
        assertFalse(options.soundEnabled);
        assertFalse(options.adsEnabled);
    }
}
