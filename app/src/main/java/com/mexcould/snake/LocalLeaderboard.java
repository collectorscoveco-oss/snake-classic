package com.mexcould.snake;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class LocalLeaderboard {
    private LocalLeaderboard() {}

    public static final class Row {
        public final String label;
        public final String key;
        public final int score;

        public Row(String label, String key, int score) {
            this.label = label;
            this.key = key;
            this.score = score;
        }
    }

    public static List<Row> rows(Map<String, Integer> scores) {
        List<Row> rows = new ArrayList<>();
        add(rows, scores, GameOptions.Difficulty.EASY, GameOptions.WallMode.SOLID);
        add(rows, scores, GameOptions.Difficulty.EASY, GameOptions.WallMode.WRAP);
        add(rows, scores, GameOptions.Difficulty.NORMAL, GameOptions.WallMode.SOLID);
        add(rows, scores, GameOptions.Difficulty.NORMAL, GameOptions.WallMode.WRAP);
        add(rows, scores, GameOptions.Difficulty.HARD, GameOptions.WallMode.SOLID);
        add(rows, scores, GameOptions.Difficulty.HARD, GameOptions.WallMode.WRAP);
        return rows;
    }

    private static void add(List<Row> rows, Map<String, Integer> scores, GameOptions.Difficulty difficulty, GameOptions.WallMode wallMode) {
        GameOptions options = new GameOptions();
        options.difficulty = difficulty;
        options.wallMode = wallMode;
        String key = options.highScoreKey();
        Integer score = scores.get(key);
        rows.add(new Row(label(difficulty, wallMode), key, score == null ? 0 : score));
    }

    private static String label(GameOptions.Difficulty difficulty, GameOptions.WallMode wallMode) {
        String diff;
        switch (difficulty) {
            case EASY: diff = "Easy"; break;
            case HARD: diff = "Hard"; break;
            case NORMAL:
            default: diff = "Normal"; break;
        }
        String mode = wallMode == GameOptions.WallMode.WRAP ? "Wrap" : "Solid walls";
        return diff + " / " + mode;
    }

    public static String shareText(String playerName, String friendCode, Map<String, Integer> scores) {
        String cleanName = playerName == null || playerName.trim().isEmpty() ? "Player" : playerName.trim();
        StringBuilder builder = new StringBuilder();
        builder.append(cleanName).append("'s Snake Classic scores:\n\n");
        for (Row row : rows(scores)) {
            builder.append(row.label).append(": ").append(row.score).append('\n');
        }
        if (friendCode != null && !friendCode.trim().isEmpty()) {
            builder.append("\nFriend code: ").append(friendCode.trim().toUpperCase());
        }
        return builder.toString();
    }
}
