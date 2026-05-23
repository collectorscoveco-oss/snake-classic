package com.mexcould.snake;

import android.content.SharedPreferences;

public class GameOptions {
    public enum Difficulty { EASY, NORMAL, HARD }
    public enum Controls { SWIPE, BUTTONS, TAP_TURN }
    public enum Theme { NEON, CLASSIC, OCEAN }
    public enum Layout { COMPACT, TALL }
    public enum WallMode { SOLID, WRAP }

    public Difficulty difficulty = Difficulty.NORMAL;
    public Controls controls = Controls.SWIPE;
    public Theme theme = Theme.NEON;
    public Layout layout = Layout.TALL;
    public WallMode wallMode = WallMode.SOLID;
    public boolean soundEnabled = true;
    public boolean adsEnabled = true;

    public static GameOptions load(SharedPreferences prefs) {
        GameOptions options = new GameOptions();
        options.difficulty = Difficulty.valueOf(prefs.getString("difficulty", Difficulty.NORMAL.name()));
        options.controls = Controls.valueOf(prefs.getString("controls", Controls.SWIPE.name()));
        options.theme = Theme.valueOf(prefs.getString("theme", Theme.NEON.name()));
        options.layout = Layout.valueOf(prefs.getString("layout", Layout.TALL.name()));
        options.wallMode = WallMode.valueOf(prefs.getString("wall_mode", WallMode.SOLID.name()));
        options.soundEnabled = prefs.getBoolean("sound", true);
        options.adsEnabled = prefs.getBoolean("ads", true);
        return options;
    }

    public void save(SharedPreferences prefs) {
        prefs.edit()
                .putString("difficulty", difficulty.name())
                .putString("controls", controls.name())
                .putString("theme", theme.name())
                .putString("layout", layout.name())
                .putString("wall_mode", wallMode.name())
                .putBoolean("sound", soundEnabled)
                .putBoolean("ads", adsEnabled)
                .apply();
    }

    public void nextDifficulty() {
        Difficulty[] values = Difficulty.values();
        difficulty = values[(difficulty.ordinal() + 1) % values.length];
    }

    public void nextControls() {
        Controls[] values = Controls.values();
        controls = values[(controls.ordinal() + 1) % values.length];
    }

    public void nextTheme() {
        Theme[] values = Theme.values();
        theme = values[(theme.ordinal() + 1) % values.length];
    }

    public void nextLayout() {
        Layout[] values = Layout.values();
        layout = values[(layout.ordinal() + 1) % values.length];
    }

    public void nextWallMode() {
        WallMode[] values = WallMode.values();
        wallMode = values[(wallMode.ordinal() + 1) % values.length];
    }

    public void toggleSound() { soundEnabled = !soundEnabled; }
    public void toggleAds() { adsEnabled = !adsEnabled; }

    public long baseDelayMs() {
        switch (difficulty) {
            case EASY: return 185L;
            case HARD: return 105L;
            case NORMAL:
            default: return 145L;
        }
    }

    public long minDelayMs() {
        switch (difficulty) {
            case EASY: return 95L;
            case HARD: return 55L;
            case NORMAL:
            default: return 70L;
        }
    }

    public int cols() {
        return layout == Layout.COMPACT ? 18 : 20;
    }

    public int rows() {
        return layout == Layout.COMPACT ? 22 : 28;
    }

    public String difficultyLabel() { return difficulty.name().charAt(0) + difficulty.name().substring(1).toLowerCase(); }
    public String controlsLabel() { return controls == Controls.TAP_TURN ? "Tap turn" : controls.name().charAt(0) + controls.name().substring(1).toLowerCase(); }
    public String themeLabel() { return theme.name().charAt(0) + theme.name().substring(1).toLowerCase(); }
    public String layoutLabel() { return layout.name().charAt(0) + layout.name().substring(1).toLowerCase(); }
    public String wallModeLabel() { return wallMode == WallMode.WRAP ? "Wrap" : "Solid walls"; }
    public String highScoreKey() { return "high_score_" + difficulty.name() + "_" + wallMode.name(); }
}

