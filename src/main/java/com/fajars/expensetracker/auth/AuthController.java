package com.fajars.expensetracker.auth;

import com.fajars.expensetracker.auth.*;
import com.fajars.expensetracker.auth.AuthService;
import com.fajars.expensetracker.auth.AuthResponse;
import com.fajars.expensetracker.auth.LoginRequest;
import com.fajars.expensetracker.auth.RefreshRequest;
import com.fajars.expensetracker.auth.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        AuthResponse res = authService.register(req);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        AuthResponse res = authService.login(req);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest req) {
        AuthResponse res = authService.refresh(req.token());
        return ResponseEntity.ok(res);
    }
}
