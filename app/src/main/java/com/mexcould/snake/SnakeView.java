package com.mexcould.snake;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SnakeView extends View {
    private static final int START_LENGTH = 4;

    private enum Direction { UP, DOWN, LEFT, RIGHT }
    private enum GameState { MENU, READY, RUNNING, GAME_OVER, PAUSED, OPTIONS, FRIENDS }

    private static final class Cell {
        int x;
        int y;
        Cell(int x, int y) { this.x = x; this.y = y; }
        boolean same(Cell other) { return other != null && x == other.x && y == other.y; }
    }

    private static final class Palette {
        int bg, board, grid, snake, head, food, text, muted, panel;
        Palette(int bg, int board, int grid, int snake, int head, int food, int text, int muted, int panel) {
            this.bg = bg; this.board = board; this.grid = grid; this.snake = snake; this.head = head;
            this.food = food; this.text = text; this.muted = muted; this.panel = panel;
        }
    }

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();
    private final List<Cell> snake = new ArrayList<>();
    private final Context appContext;
    private final SharedPreferences prefs;
    private final SharedPreferences friendsPrefs;
    private final SharedPreferences optionPrefs;
    private final ToneGenerator tones;
    private final RectF startButton = new RectF();
    private final RectF optionsButton = new RectF();
    private final RectF friendsButton = new RectF();
    private final RectF playAgainButton = new RectF();
    private final RectF menuButton = new RectF();
    private final RectF shareCodeButton = new RectF();
    private final RectF newCodeButton = new RectF();
    private final RectF joinPlaceholderButton = new RectF();
    private final RectF scanQrButton = new RectF();
    private final RectF pauseButton = new RectF();
    private final RectF resumeButton = new RectF();
    private final RectF pauseOptionsButton = new RectF();
    private final RectF[] optionRows = new RectF[]{new RectF(), new RectF(), new RectF(), new RectF(), new RectF(), new RectF(), new RectF()};
    private final RectF backButton = new RectF();
    private final RectF upButton = new RectF();
    private final RectF downButton = new RectF();
    private final RectF leftButton = new RectF();
    private final RectF rightButton = new RectF();

    private final Runnable tick = new Runnable() {
        @Override public void run() {
            if (state == GameState.RUNNING) {
                stepGame();
                invalidate();
                handler.postDelayed(this, currentDelay());
            }
        }
    };

    private GameOptions options;
    private Direction direction = Direction.RIGHT;
    private Direction pendingDirection = Direction.RIGHT;
    private GameState state = GameState.MENU;
    private Cell food;
    private int score = 0;
    private int highScore = 0;
    private String friendCode;
    private boolean optionsOpenedFromGame = false;
    private boolean gameResetByOptionChange = false;
    private float downX;
    private float downY;

    public SnakeView(Context context) {
        super(context);
        appContext = context;
        setFocusable(true);
        prefs = context.getSharedPreferences("snake_scores", Context.MODE_PRIVATE);
        friendsPrefs = context.getSharedPreferences("snake_friends", Context.MODE_PRIVATE);
        optionPrefs = context.getSharedPreferences("snake_options", Context.MODE_PRIVATE);
        highScore = 0;
        friendCode = friendsPrefs.getString("friend_code", "");
        if (!FriendCode.isValid(friendCode)) {
            friendCode = FriendCode.generate(random);
            friendsPrefs.edit().putString("friend_code", friendCode).apply();
        }
        options = GameOptions.load(optionPrefs);
        loadHighScore();
        tones = new ToneGenerator(AudioManager.STREAM_MUSIC, 45);
        resetGame(false);
    }

    public void pause() {
        if (state == GameState.RUNNING) {
            state = GameState.PAUSED;
            handler.removeCallbacks(tick);
            invalidate();
        }
    }

    public void resume() { invalidate(); }

    private int cols() { return options.cols(); }
    private int rows() { return options.rows(); }

    private void resetGame(boolean ready) {
        handler.removeCallbacks(tick);
        snake.clear();
        int startX = cols() / 2;
        int startY = rows() / 2;
        for (int i = 0; i < START_LENGTH; i++) snake.add(new Cell(startX - i, startY));
        direction = Direction.RIGHT;
        pendingDirection = Direction.RIGHT;
        score = 0;
        food = randomFood();
        state = ready ? GameState.READY : GameState.MENU;
        invalidate();
    }

    private void loadHighScore() {
        highScore = prefs.getInt(options.highScoreKey(), 0);
    }

    private long currentDelay() {
        return Math.max(options.minDelayMs(), options.baseDelayMs() - (score * 4L));
    }

    private void startGame() {
        if (state == GameState.GAME_OVER) resetGame(true);
        if (state == GameState.MENU) resetGame(true);
        if (state == GameState.READY || state == GameState.PAUSED) {
            state = GameState.RUNNING;
            handler.removeCallbacks(tick);
            handler.postDelayed(tick, currentDelay());
            invalidate();
        }
    }

    private void stepGame() {
        direction = pendingDirection;
        Cell head = snake.get(0);
        Cell next = new Cell(head.x, head.y);
        switch (direction) {
            case UP: next.y--; break;
            case DOWN: next.y++; break;
            case LEFT: next.x--; break;
            case RIGHT: next.x++; break;
        }
        if (options.wallMode == GameOptions.WallMode.WRAP) {
            next.x = GameRules.wrapCoordinate(next.x, cols());
            next.y = GameRules.wrapCoordinate(next.y, rows());
        } else if (!GameRules.isInsideBoard(next.x, next.y, cols(), rows())) {
            playTone(ToneGenerator.TONE_PROP_NACK, 180);
            gameOver();
            return;
        }
        if (hitsSnake(next)) {
            playTone(ToneGenerator.TONE_PROP_NACK, 180);
            gameOver();
            return;
        }
        snake.add(0, next);
        if (next.same(food)) {
            score++;
            playTone(ToneGenerator.TONE_PROP_BEEP, 70);
            if (score > highScore) {
                highScore = score;
                prefs.edit().putInt(options.highScoreKey(), highScore).apply();
            }
            food = randomFood();
        } else {
            snake.remove(snake.size() - 1);
        }
    }

    private void playTone(int tone, int durationMs) {
        if (options.soundEnabled) tones.startTone(tone, durationMs);
    }

    private boolean hitsSnake(Cell cell) {
        for (Cell part : snake) if (cell.same(part)) return true;
        return false;
    }

    private Cell randomFood() {
        Cell candidate;
        do candidate = new Cell(random.nextInt(cols()), random.nextInt(rows())); while (hitsSnake(candidate));
        return candidate;
    }

    private void gameOver() {
        state = GameState.GAME_OVER;
        handler.removeCallbacks(tick);
        invalidate();
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Palette p = palette();
        canvas.drawColor(p.bg);
        if (state == GameState.MENU) { drawMenu(canvas, p); return; }
        if (state == GameState.OPTIONS) { drawOptions(canvas, p); return; }
        if (state == GameState.FRIENDS) { drawFriends(canvas, p); return; }

        float topPadding = getHeight() * 0.12f;
        float bottomReserve = options.controls == GameOptions.Controls.BUTTONS ? dp(155) : dp(80);
        float adReserve = options.adsEnabled ? dp(42) : 0;
        float boardWidth = getWidth() * 0.94f;
        float boardHeight = getHeight() - topPadding - bottomReserve - adReserve;
        float cellSize = Math.min(boardWidth / cols(), boardHeight / rows());
        float boardLeft = (getWidth() - cellSize * cols()) / 2f;
        float boardTop = topPadding;
        float boardRight = boardLeft + cellSize * cols();
        float boardBottom = boardTop + cellSize * rows();

        drawHeader(canvas, p, boardTop);
        drawBoard(canvas, p, boardLeft, boardTop, boardRight, boardBottom, cellSize);
        drawFood(canvas, p, boardLeft, boardTop, cellSize);
        drawSnake(canvas, p, boardLeft, boardTop, cellSize);
        drawFooter(canvas, p, boardBottom);
        if (options.controls == GameOptions.Controls.BUTTONS) drawControls(canvas, p, boardBottom + dp(28));
        if (options.adsEnabled) drawAdPlaceholder(canvas, p);

        if (state == GameState.READY) drawCenterMessage(canvas, p, "SNAKE", "Swipe/tap to start");
        if (state == GameState.PAUSED) drawPauseMenu(canvas, p);
        if (state == GameState.GAME_OVER) drawGameOver(canvas, p);
    }

    private void drawMenu(Canvas canvas, Palette p) {
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setFakeBoldText(true);
        paint.setColor(p.text);
        paint.setTextSize(sp(44));
        canvas.drawText("SNAKE", getWidth() / 2f, getHeight() * 0.22f, paint);
        paint.setFakeBoldText(false);
        paint.setTextSize(sp(16));
        paint.setColor(p.muted);
        canvas.drawText("Classic arcade snake", getWidth() / 2f, getHeight() * 0.27f, paint);
        float w = getWidth() * 0.72f;
        startButton.set((getWidth()-w)/2f, getHeight()*0.35f, (getWidth()+w)/2f, getHeight()*0.43f);
        friendsButton.set(startButton.left, startButton.bottom + dp(16), startButton.right, startButton.bottom + dp(16) + startButton.height());
        optionsButton.set(startButton.left, friendsButton.bottom + dp(16), startButton.right, friendsButton.bottom + dp(16) + startButton.height());
        drawButton(canvas, p, startButton, "PLAY");
        drawButton(canvas, p, friendsButton, "FRIENDS");
        drawButton(canvas, p, optionsButton, "OPTIONS");
        paint.setColor(p.muted);
        paint.setTextSize(sp(14));
        canvas.drawText("High score: " + highScore, getWidth()/2f, optionsButton.bottom + dp(42), paint);
        if (options.adsEnabled) drawAdPlaceholder(canvas, p);
    }

    private void drawOptions(Canvas canvas, Palette p) {
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setFakeBoldText(true);
        paint.setColor(p.text);
        paint.setTextSize(sp(30));
        canvas.drawText("OPTIONS", getWidth()/2f, dp(70), paint);
        paint.setFakeBoldText(false);
        String[] labels = new String[]{
                "Difficulty: " + options.difficultyLabel(),
                "Controls: " + options.controlsLabel(),
                "Theme: " + options.themeLabel(),
                "Layout: " + options.layoutLabel(),
                "Mode: " + options.wallModeLabel(),
                "Sound: " + (options.soundEnabled ? "On" : "Off"),
                "Ads: " + (options.adsEnabled ? "Placeholder On" : "Off")
        };
        if (optionsOpenedFromGame) {
            paint.setTextSize(sp(12));
            paint.setColor(p.food);
            canvas.drawText("Warning: difficulty/layout changes reset the current game", getWidth()/2f, dp(98), paint);
        }
        float top = optionsOpenedFromGame ? dp(122) : dp(110);
        float h = dp(52);
        for (int i = 0; i < labels.length; i++) {
            optionRows[i].set(dp(24), top + i * (h + dp(10)), getWidth() - dp(24), top + i * (h + dp(10)) + h);
            drawButton(canvas, p, optionRows[i], labels[i]);
        }
        backButton.set(dp(24), getHeight() - dp(90), getWidth() - dp(24), getHeight() - dp(35));
        drawButton(canvas, p, backButton, "BACK TO MENU");
    }

    private void drawFriends(Canvas canvas, Palette p) {
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setFakeBoldText(true);
        paint.setColor(p.text);
        paint.setTextSize(sp(30));
        canvas.drawText("FRIENDS", getWidth()/2f, dp(70), paint);
        paint.setFakeBoldText(false);
        paint.setTextSize(sp(15));
        paint.setColor(p.muted);
        canvas.drawText("Simple Android-only sharing for now", getWidth()/2f, dp(100), paint);

        RectF card = new RectF(dp(24), dp(128), getWidth()-dp(24), dp(382));
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(p.panel);
        canvas.drawRoundRect(card, dp(16), dp(16), paint);
        paint.setTextSize(sp(14));
        paint.setColor(p.muted);
        canvas.drawText("Your leaderboard code", getWidth()/2f, card.top + dp(36), paint);
        paint.setFakeBoldText(true);
        paint.setTextSize(sp(42));
        paint.setColor(Color.WHITE);
        canvas.drawText(friendCode, getWidth()/2f, card.top + dp(92), paint);
        paint.setFakeBoldText(false);

        drawQrStylePlaceholder(canvas, getWidth()/2f, card.top + dp(128), dp(112));
        paint.setTextSize(sp(12));
        paint.setColor(p.muted);
        canvas.drawText("QR placeholder — scanner/deep links later", getWidth()/2f, card.bottom - dp(18), paint);

        float w = getWidth() * 0.78f;
        float buttonTop = card.bottom + dp(22);
        shareCodeButton.set((getWidth()-w)/2f, buttonTop, (getWidth()+w)/2f, buttonTop + dp(48));
        scanQrButton.set(shareCodeButton.left, shareCodeButton.bottom + dp(12), shareCodeButton.right, shareCodeButton.bottom + dp(60));
        joinPlaceholderButton.set(shareCodeButton.left, scanQrButton.bottom + dp(12), shareCodeButton.right, scanQrButton.bottom + dp(60));
        newCodeButton.set(shareCodeButton.left, joinPlaceholderButton.bottom + dp(12), shareCodeButton.right, joinPlaceholderButton.bottom + dp(60));
        backButton.set(shareCodeButton.left, getHeight() - dp(90), shareCodeButton.right, getHeight() - dp(35));
        drawButton(canvas, p, shareCodeButton, "SHARE INVITE");
        drawButton(canvas, p, scanQrButton, "SCAN QR CODE");
        drawButton(canvas, p, joinPlaceholderButton, "JOIN CODE: COMING SOON");
        drawButton(canvas, p, newCodeButton, "GENERATE NEW CODE");
        drawButton(canvas, p, backButton, "BACK TO MENU");
    }

    private void drawQrStylePlaceholder(Canvas canvas, float cx, float top, float size) {
        int cells = 9;
        float cell = size / cells;
        float left = cx - size / 2f;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        canvas.drawRect(left - dp(4), top - dp(4), left + size + dp(4), top + size + dp(4), paint);
        paint.setColor(Color.BLACK);
        int hash = friendCode.hashCode();
        for (int y = 0; y < cells; y++) {
            for (int x = 0; x < cells; x++) {
                boolean finder = (x < 3 && y < 3) || (x > 5 && y < 3) || (x < 3 && y > 5);
                boolean bit = ((hash >> ((x + y * cells) % 24)) & 1) == 1;
                if (finder || bit) canvas.drawRect(left + x * cell, top + y * cell, left + (x+1) * cell, top + (y+1) * cell, paint);
            }
        }
    }


    private void drawHeader(Canvas canvas, Palette p, float boardTop) {
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(p.text);
        paint.setTextSize(sp(22));
        paint.setFakeBoldText(true);
        canvas.drawText("SNAKE", dp(18), boardTop - dp(45), paint);
        paint.setFakeBoldText(false);
        paint.setTextSize(sp(16));
        canvas.drawText("Score: " + score, dp(18), boardTop - dp(18), paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("High: " + highScore, getWidth() - dp(18), boardTop - dp(18), paint);
        pauseButton.set(getWidth() - dp(78), boardTop - dp(66), getWidth() - dp(18), boardTop - dp(36));
        drawSmallButton(canvas, p, pauseButton, "PAUSE");
    }

    private void drawBoard(Canvas canvas, Palette p, float left, float top, float right, float bottom, float cellSize) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(p.board);
        canvas.drawRoundRect(new RectF(left - dp(4), top - dp(4), right + dp(4), bottom + dp(4)), dp(12), dp(12), paint);
        paint.setColor(p.grid);
        paint.setStrokeWidth(1f);
        for (int x = 0; x <= cols(); x++) canvas.drawLine(left + x * cellSize, top, left + x * cellSize, bottom, paint);
        for (int y = 0; y <= rows(); y++) canvas.drawLine(left, top + y * cellSize, right, top + y * cellSize, paint);
    }

    private void drawSnake(Canvas canvas, Palette p, float left, float top, float cellSize) {
        paint.setStyle(Paint.Style.FILL);
        for (int i = snake.size() - 1; i >= 0; i--) {
            Cell c = snake.get(i);
            paint.setColor(i == 0 ? p.head : p.snake);
            float pad = dp(1.5f);
            canvas.drawRoundRect(new RectF(left + c.x * cellSize + pad, top + c.y * cellSize + pad,
                    left + (c.x + 1) * cellSize - pad, top + (c.y + 1) * cellSize - pad), dp(5), dp(5), paint);
        }
    }

    private void drawFood(Canvas canvas, Palette p, float left, float top, float cellSize) {
        if (food == null || !GameRules.isInsideBoard(food.x, food.y, cols(), rows())) food = randomFood();
        paint.setColor(p.food);
        canvas.drawCircle(left + food.x * cellSize + cellSize/2f, top + food.y * cellSize + cellSize/2f, cellSize * 0.36f, paint);
    }

    private void drawFooter(Canvas canvas, Palette p, float boardBottom) {
        paint.setColor(p.muted);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(sp(13));
        canvas.drawText(options.difficultyLabel() + "  •  " + options.wallModeLabel() + "  •  " + options.controlsLabel(), getWidth()/2f, boardBottom + dp(24), paint);
    }

    private void drawControls(Canvas canvas, Palette p, float top) {
        float size = dp(48);
        float cx = getWidth()/2f;
        upButton.set(cx-size/2, top, cx+size/2, top+size);
        leftButton.set(cx-size*1.6f, top+size*0.9f, cx-size*0.6f, top+size*1.9f);
        downButton.set(cx-size/2, top+size*0.9f, cx+size/2, top+size*1.9f);
        rightButton.set(cx+size*0.6f, top+size*0.9f, cx+size*1.6f, top+size*1.9f);
        drawButton(canvas, p, upButton, "▲"); drawButton(canvas, p, leftButton, "◀");
        drawButton(canvas, p, downButton, "▼"); drawButton(canvas, p, rightButton, "▶");
    }

    private void drawGameOver(Canvas canvas, Palette p) {
        drawCenterMessage(canvas, p, "GAME OVER", "Score " + score);
        float w = getWidth() * 0.64f;
        playAgainButton.set((getWidth()-w)/2f, getHeight()/2f + dp(55), (getWidth()+w)/2f, getHeight()/2f + dp(105));
        menuButton.set(playAgainButton.left, playAgainButton.bottom + dp(12), playAgainButton.right, playAgainButton.bottom + dp(62));
        drawButton(canvas, p, playAgainButton, "PLAY AGAIN");
        drawButton(canvas, p, menuButton, "MENU");
    }

    private void drawPauseMenu(Canvas canvas, Palette p) {
        drawCenterMessage(canvas, p, "PAUSED", gameResetByOptionChange ? "Board setting changed — game reset" : "Settings or resume");
        float w = getWidth() * 0.64f;
        resumeButton.set((getWidth()-w)/2f, getHeight()/2f + dp(55), (getWidth()+w)/2f, getHeight()/2f + dp(105));
        pauseOptionsButton.set(resumeButton.left, resumeButton.bottom + dp(12), resumeButton.right, resumeButton.bottom + dp(62));
        menuButton.set(resumeButton.left, pauseOptionsButton.bottom + dp(12), resumeButton.right, pauseOptionsButton.bottom + dp(62));
        drawButton(canvas, p, resumeButton, "RESUME");
        drawButton(canvas, p, pauseOptionsButton, "SETTINGS");
        drawButton(canvas, p, menuButton, "MENU");
    }


    private void drawCenterMessage(Canvas canvas, Palette p, String title, String subtitle) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(218, 0, 0, 0));
        canvas.drawRoundRect(new RectF(dp(28), getHeight()/2f - dp(90), getWidth()-dp(28), getHeight()/2f + dp(50)), dp(18), dp(18), paint);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setFakeBoldText(true);
        paint.setTextSize(sp(33));
        paint.setColor(Color.WHITE);
        canvas.drawText(title, getWidth()/2f, getHeight()/2f - dp(26), paint);
        paint.setFakeBoldText(false);
        paint.setTextSize(sp(17));
        paint.setColor(p.muted);
        canvas.drawText(subtitle, getWidth()/2f, getHeight()/2f + dp(12), paint);
    }

    private void drawAdPlaceholder(Canvas canvas, Palette p) {
        RectF ad = new RectF(dp(18), getHeight() - dp(36), getWidth() - dp(18), getHeight() - dp(8));
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(120, 255, 255, 255));
        canvas.drawRoundRect(ad, dp(8), dp(8), paint);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(sp(12));
        paint.setColor(Color.rgb(35,35,35));
        canvas.drawText("Ad placeholder — real ads later with Google AdMob", getWidth()/2f, ad.centerY()+dp(4), paint);
    }

    private void drawButton(Canvas canvas, Palette p, RectF r, String label) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(p.panel);
        canvas.drawRoundRect(r, dp(12), dp(12), paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(2));
        paint.setColor(p.text);
        canvas.drawRoundRect(r, dp(12), dp(12), paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setFakeBoldText(true);
        paint.setTextSize(sp(17));
        canvas.drawText(label, r.centerX(), r.centerY() + dp(6), paint);
        paint.setFakeBoldText(false);
    }

    private void drawSmallButton(Canvas canvas, Palette p, RectF r, String label) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(p.panel);
        canvas.drawRoundRect(r, dp(8), dp(8), paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1.5f));
        paint.setColor(p.text);
        canvas.drawRoundRect(r, dp(8), dp(8), paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setFakeBoldText(true);
        paint.setTextSize(sp(10));
        canvas.drawText(label, r.centerX(), r.centerY() + dp(4), paint);
        paint.setFakeBoldText(false);
    }


    @Override public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) { downX = event.getX(); downY = event.getY(); return true; }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            float x = event.getX(), y = event.getY();
            if (state == GameState.MENU) {
                if (startButton.contains(x, y)) { playTone(ToneGenerator.TONE_PROP_ACK, 80); resetGame(true); startGame(); return true; }
                if (friendsButton.contains(x, y)) { playTone(ToneGenerator.TONE_PROP_ACK, 80); state = GameState.FRIENDS; invalidate(); return true; }
                if (optionsButton.contains(x, y)) { playTone(ToneGenerator.TONE_PROP_ACK, 80); state = GameState.OPTIONS; invalidate(); return true; }
                return true;
            }
            if (state == GameState.OPTIONS) { handleOptionsTap(x, y); return true; }
            if (state == GameState.FRIENDS) { handleFriendsTap(x, y); return true; }
            if (state == GameState.PAUSED) { handlePauseTap(x, y); return true; }
            if ((state == GameState.RUNNING || state == GameState.READY) && pauseButton.contains(x, y)) { pauseGame(); return true; }
            if (state == GameState.GAME_OVER) {
                if (playAgainButton.contains(x, y)) { resetGame(true); startGame(); return true; }
                if (menuButton.contains(x, y)) { resetGame(false); return true; }
            }
            if (state == GameState.RUNNING && options.controls == GameOptions.Controls.BUTTONS && handleButtonControls(x, y)) return true;
            if (options.controls == GameOptions.Controls.TAP_TURN) {
                rotateDirection(x < getWidth()/2f ? -1 : 1);
                startGame(); return true;
            }
            float dx = x - downX, dy = y - downY;
            if (Math.abs(dx) < dp(24) && Math.abs(dy) < dp(24)) { startGame(); return true; }
            if (Math.abs(dx) > Math.abs(dy)) setDirection(dx > 0 ? Direction.RIGHT : Direction.LEFT);
            else setDirection(dy > 0 ? Direction.DOWN : Direction.UP);
            startGame(); return true;
        }
        return true;
    }

    private void pauseGame() {
        if (state == GameState.RUNNING || state == GameState.READY) {
            state = GameState.PAUSED;
            handler.removeCallbacks(tick);
            playTone(ToneGenerator.TONE_PROP_ACK, 60);
            invalidate();
        }
    }

    private void handlePauseTap(float x, float y) {
        if (resumeButton.contains(x, y)) { startGame(); return; }
        if (pauseOptionsButton.contains(x, y)) {
            optionsOpenedFromGame = true;
            gameResetByOptionChange = false;
            state = GameState.OPTIONS;
            playTone(ToneGenerator.TONE_PROP_ACK, 80);
            invalidate();
            return;
        }
        if (menuButton.contains(x, y)) { optionsOpenedFromGame = false; resetGame(false); }
    }

    private void handleFriendsTap(float x, float y) {
        if (shareCodeButton.contains(x, y)) { shareFriendCode(); return; }
        if (newCodeButton.contains(x, y)) {
            friendCode = FriendCode.generate(random);
            friendsPrefs.edit().putString("friend_code", friendCode).apply();
            playTone(ToneGenerator.TONE_PROP_ACK, 80);
            invalidate();
            return;
        }
        if (scanQrButton.contains(x, y)) { playTone(ToneGenerator.TONE_PROP_BEEP, 90); return; }
        if (joinPlaceholderButton.contains(x, y)) { playTone(ToneGenerator.TONE_PROP_BEEP, 90); return; }
        if (backButton.contains(x, y)) { resetGame(false); }
    }

    private void shareFriendCode() {
        playTone(ToneGenerator.TONE_PROP_ACK, 80);
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_TEXT, FriendCode.inviteMessage(friendCode));
        Intent chooser = Intent.createChooser(sendIntent, "Share Snake Classic invite");
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        appContext.startActivity(chooser);
    }

    private void handleOptionsTap(float x, float y) {
        if (backButton.contains(x, y)) {
            options.save(optionPrefs);
            if (optionsOpenedFromGame) {
                optionsOpenedFromGame = false;
                if (gameResetByOptionChange) resetGame(true); else state = GameState.PAUSED;
                invalidate();
                return;
            }
            resetGame(false);
            return;
        }
        for (int i = 0; i < optionRows.length; i++) {
            if (!optionRows[i].contains(x, y)) continue;
            boolean changedBoardRules = i == 0 || i == 3 || i == 4;
            if (i == 0) options.nextDifficulty();
            if (i == 1) options.nextControls();
            if (i == 2) options.nextTheme();
            if (i == 3) options.nextLayout();
            if (i == 4) options.nextWallMode();
            if (i == 5) options.toggleSound();
            if (i == 6) options.toggleAds();
            options.save(optionPrefs);
            loadHighScore();
            if (optionsOpenedFromGame && changedBoardRules) {
                gameResetByOptionChange = true;
                resetGame(true);
                state = GameState.OPTIONS;
            }
            playTone(ToneGenerator.TONE_PROP_ACK, 65);
            invalidate();
            return;
        }
    }

    private boolean handleButtonControls(float x, float y) {
        if (upButton.contains(x, y)) setDirection(Direction.UP);
        else if (downButton.contains(x, y)) setDirection(Direction.DOWN);
        else if (leftButton.contains(x, y)) setDirection(Direction.LEFT);
        else if (rightButton.contains(x, y)) setDirection(Direction.RIGHT);
        else return false;
        playTone(ToneGenerator.TONE_PROP_ACK, 35);
        return true;
    }

    private void rotateDirection(int amount) {
        Direction[] clockwise = new Direction[]{Direction.UP, Direction.RIGHT, Direction.DOWN, Direction.LEFT};
        int index = 0;
        for (int i = 0; i < clockwise.length; i++) if (clockwise[i] == direction) index = i;
        int next = (index + amount + clockwise.length) % clockwise.length;
        setDirection(clockwise[next]);
    }

    private void setDirection(Direction next) {
        if ((direction == Direction.UP && next == Direction.DOWN) || (direction == Direction.DOWN && next == Direction.UP)
                || (direction == Direction.LEFT && next == Direction.RIGHT) || (direction == Direction.RIGHT && next == Direction.LEFT)) return;
        pendingDirection = next;
    }

    private Palette palette() {
        switch (options.theme) {
            case CLASSIC:
                return new Palette(Color.rgb(20, 35, 16), Color.rgb(115, 160, 65), Color.rgb(85, 125, 50), Color.rgb(35, 65, 25), Color.rgb(15, 35, 15), Color.rgb(200, 20, 20), Color.rgb(235, 250, 210), Color.rgb(220, 235, 180), Color.rgb(55, 90, 35));
            case OCEAN:
                return new Palette(Color.rgb(4, 17, 28), Color.rgb(8, 47, 73), Color.rgb(16, 73, 105), Color.rgb(45, 212, 191), Color.rgb(165, 243, 252), Color.rgb(251, 146, 60), Color.rgb(224, 242, 254), Color.rgb(186, 230, 253), Color.rgb(7, 89, 133));
            case NEON:
            default:
                return new Palette(Color.rgb(5, 7, 5), Color.rgb(12, 22, 12), Color.rgb(16, 35, 16), Color.rgb(101, 245, 108), Color.rgb(175, 255, 175), Color.rgb(255, 80, 95), Color.rgb(101, 245, 108), Color.rgb(165, 190, 165), Color.rgb(18, 35, 18));
        }
    }

    private float dp(float value) { return value * getResources().getDisplayMetrics().density; }
    private float sp(float value) { return value * getResources().getDisplayMetrics().scaledDensity; }
}





