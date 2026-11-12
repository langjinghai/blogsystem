package com.example.springbootproject.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordUtilsTest {

    @Test
    void bcryptRoundTrip() {
        String hash = PasswordUtils.encode("abc123");
        assertTrue(PasswordUtils.isBcrypt(hash));
        assertTrue(PasswordUtils.matches("abc123", hash));
        assertFalse(PasswordUtils.matches("wrong", hash));
    }

    @Test
    void legacyPlaintextStillMatchesForMigration() {
        assertTrue(PasswordUtils.matches("old-password", "old-password"));
        assertFalse(PasswordUtils.matches("other", "old-password"));
    }
}
