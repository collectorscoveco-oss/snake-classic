package com.mexcould.snake;

import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FriendCodeTest {
    @Test
    public void generatedCodesAreSixCharactersAndAvoidConfusingLetters() {
        String code = FriendCode.generate(new Random(7));
        assertEquals(6, code.length());
        assertTrue(code.matches("[A-HJ-NP-Z2-9]{6}"));
        assertFalse(code.contains("0"));
        assertFalse(code.contains("O"));
        assertFalse(code.contains("1"));
        assertFalse(code.contains("I"));
        assertFalse(code.contains("L"));
    }

    @Test
    public void normalizesTypedCodesForFutureBackendLookups() {
        assertEquals("ABCD29", FriendCode.normalize(" abcd-29 "));
        assertEquals("MEX777", FriendCode.normalize("mex 777"));
    }

    @Test
    public void validatesJoinCodesBeforeSubmitting() {
        assertTrue(FriendCode.isValid("A7K92P"));
        assertFalse(FriendCode.isValid("A7K92"));
        assertFalse(FriendCode.isValid("A7K920"));
        assertFalse(FriendCode.isValid("A7K92O"));
    }

    @Test
    public void inviteMessageIsShareableNowAndDeepLinkReadyLater() {
        String message = FriendCode.inviteMessage("A7K92P");
        assertTrue(message.contains("A7K92P"));
        assertTrue(message.contains("Snake Classic"));
        assertTrue(message.contains("snake.mexcould.com/join/A7K92P"));
    }
}
