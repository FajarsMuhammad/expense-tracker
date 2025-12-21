package com.fajars.expensetracker.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fajars.expensetracker.common.util.JwtUtil;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class JwtUtilTest {

    @Test
    void generateAndValidateToken() {
        // use a plain test secret (JwtUtil will pad or decode as needed)
        JwtUtil jwtUtil = new JwtUtil("test-secret-for-unit-test-which-is-long-enough");

        String token = jwtUtil.generateToken(UUID.randomUUID(), "test-user");
        assertNotNull(token, "Token should not be null");
        assertTrue(jwtUtil.isTokenValid(token), "Token should be valid");
        assertEquals("test-user", jwtUtil.extractEmail(token), "Username should match");
    }
}

