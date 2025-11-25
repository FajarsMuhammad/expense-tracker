package com.fajars.expensetracker.util;

import com.fajars.expensetracker.common.util.JwtUtil;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class JwtUtilTest {

    @Test
    void generateAndValidateToken() {
        // use a plain test secret (JwtUtil will pad or decode as needed)
        JwtUtil jwtUtil = new JwtUtil("test-secret-for-unit-test-which-is-long-enough");

        String token = jwtUtil.generateToken("test-user");
        assertNotNull(token, "Token should not be null");
        assertTrue(jwtUtil.isTokenValid(token), "Token should be valid");
        assertEquals("test-user", jwtUtil.extractUsername(token), "Username should match");
    }
}

