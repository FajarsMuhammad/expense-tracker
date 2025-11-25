package com.fajars.expensetracker.auth;

import com.fajars.expensetracker.auth.AuthResponse;
import com.fajars.expensetracker.auth.LoginRequest;
import com.fajars.expensetracker.auth.RegisterRequest;
import com.fajars.expensetracker.common.logging.BusinessEventLogger;
import com.fajars.expensetracker.common.metrics.MetricsService;
import com.fajars.expensetracker.user.User;
import com.fajars.expensetracker.user.UserRepository;
import com.fajars.expensetracker.common.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Date;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final MetricsService metricsService;
    private final BusinessEventLogger businessEventLogger;

    public AuthService(
        UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil,
        AuthenticationManager authenticationManager, MetricsService metricsService,
        BusinessEventLogger businessEventLogger
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.metricsService = metricsService;
        this.businessEventLogger = businessEventLogger;
    }

    public AuthResponse register(RegisterRequest req) {
        if (userRepository.findByEmail(req.email()).isPresent()) {
            throw new IllegalArgumentException("Email already in use");
        }

        User user = User.builder()
            .id(UUID.randomUUID())
            .email(req.email())
            .passwordHash(passwordEncoder.encode(req.password()))
            .name(req.name())
            .createdAt(new Date())
            .build();

        userRepository.save(user);

        // Log business event and metrics
        String ipAddress = getClientIpAddress();
        businessEventLogger.logUserRegistration(user.getEmail(), ipAddress);
        metricsService.recordUserRegistration();

        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token, user.getId(), user.getEmail(), user.getName());
    }

    public AuthResponse login(LoginRequest req) {
        String ipAddress = getClientIpAddress();
        String userAgent = getUserAgent();

        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password())
            );
        } catch (org.springframework.security.core.AuthenticationException ex) {
            // Log failed login attempt
            businessEventLogger.logLoginFailure(req.email(), ipAddress, "Invalid credentials");
            metricsService.recordLoginFailure();
            throw new IllegalArgumentException("Invalid credentials");
        }

        // if authentication successful
        User user = userRepository.findByEmail(req.email()).orElseThrow();
        String token = jwtUtil.generateToken(user.getEmail());

        // Log successful login
        businessEventLogger.logLoginSuccess(user.getEmail(), ipAddress, userAgent);
        metricsService.recordLoginSuccess();

        return new AuthResponse(token, user.getId(), user.getEmail(), user.getName());
    }

    public AuthResponse refresh(String token) {
        if (!jwtUtil.isTokenValid(token)) {
            throw new IllegalArgumentException("Invalid token");
        }
        String username = jwtUtil.extractUsername(token);
        User user = userRepository.findByEmail(username).orElseThrow();
        String newToken = jwtUtil.generateToken(username);
        return new AuthResponse(newToken, user.getId(), user.getEmail(), user.getName());
    }

    private String getClientIpAddress() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String ip = request.getHeader("X-Forwarded-For");
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getRemoteAddr();
                }
                return ip;
            }
        } catch (Exception e) {
            // Ignore
        }
        return "unknown";
    }

    private String getUserAgent() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return request.getHeader("User-Agent");
            }
        } catch (Exception e) {
            // Ignore
        }
        return "unknown";
    }
}
