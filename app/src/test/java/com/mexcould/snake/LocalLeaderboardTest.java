package com.mexcould.snake;

import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LocalLeaderboardTest {
    @Test
    public void rowsIncludeEveryDifficultyAndWallModeInStableOrder() {
        Map<String, Integer> scores = new HashMap<>();
        scores.put("high_score_EASY_SOLID", 3);
        scores.put("high_score_EASY_WRAP", 4);
        scores.put("high_score_NORMAL_SOLID", 5);
        scores.put("high_score_NORMAL_WRAP", 6);
        scores.put("high_score_HARD_SOLID", 7);
        scores.put("high_score_HARD_WRAP", 8);

        List<LocalLeaderboard.Row> rows = LocalLeaderboard.rows(scores);

        assertEquals(6, rows.size());
        assertEquals("Easy / Solid walls", rows.get(0).label);
        assertEquals(3, rows.get(0).score);
        assertEquals("Easy / Wrap", rows.get(1).label);
        assertEquals("Normal / Solid walls", rows.get(2).label);
        assertEquals("Normal / Wrap", rows.get(3).label);
        assertEquals("Hard / Solid walls", rows.get(4).label);
        assertEquals("Hard / Wrap", rows.get(5).label);
        assertEquals(8, rows.get(5).score);
    }

    @Test
    public void missingScoresDefaultToZero() {
        List<LocalLeaderboard.Row> rows = LocalLeaderboard.rows(new HashMap<String, Integer>());

        for (LocalLeaderboard.Row row : rows) {
            assertEquals(0, row.score);
        }
    }

    @Test
    public void shareTextIncludesPlayerNameFriendCodeAndAllScores() {
        Map<String, Integer> scores = new HashMap<>();
        scores.put("high_score_NORMAL_WRAP", 12);

        String text = LocalLeaderboard.shareText("Chris", "A7K92P", scores);

        assertTrue(text.contains("Chris's Snake Classic scores"));
        assertTrue(text.contains("Friend code: A7K92P"));
        assertTrue(text.contains("Normal / Wrap: 12"));
        assertTrue(text.contains("Hard / Solid walls: 0"));
    }
}
