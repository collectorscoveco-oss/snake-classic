package com.mexcould.snake;

import java.util.Random;

public final class FriendCode {
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int LENGTH = 6;

    private FriendCode() {}

    public static String generate(Random random) {
        StringBuilder code = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            code.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return code.toString();
    }

    public static String normalize(String raw) {
        if (raw == null) return "";
        return raw.toUpperCase().replaceAll("[^A-Z0-9]", "");
    }

    public static boolean isValid(String raw) {
        String code = normalize(raw);
        if (code.length() != LENGTH) return false;
        for (int i = 0; i < code.length(); i++) {
            if (ALPHABET.indexOf(code.charAt(i)) < 0) return false;
        }
        return true;
    }

    public static String inviteMessage(String code) {
        String normalized = normalize(code);
        return "Join my Snake Classic leaderboard!\n\n"
                + "Friend code: " + normalized + "\n\n"
                + "Future link: https://snake.mexcould.com/join/" + normalized;
    }
}
