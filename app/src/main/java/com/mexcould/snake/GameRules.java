package com.mexcould.snake;

public final class GameRules {
    private GameRules() {}

    public static int wrapCoordinate(int value, int maxExclusive) {
        if (value < 0) return maxExclusive - 1;
        if (value >= maxExclusive) return 0;
        return value;
    }

    public static boolean isInsideBoard(int x, int y, int cols, int rows) {
        return x >= 0 && x < cols && y >= 0 && y < rows;
    }
}
