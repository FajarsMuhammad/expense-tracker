package com.fajars.expensetracker.auth;

import com.fajars.expensetracker.auth.AuthResponse;
import com.fajars.expensetracker.auth.LoginRequest;
import com.fajars.expensetracker.auth.RegisterRequest;
import com.fajars.expensetracker.common.logging.BusinessEventLogger;
import com.fajars.expensetracker.common.metrics.MetricsService;
import com.fajars.expensetracker.subscription.Subscription;
import com.fajars.expensetracker.subscription.usecase.CreateFreeSubscription;
import com.fajars.expensetracker.user.User;
import com.fajars.expensetracker.user.UserRepository;
import com.fajars.expensetracker.common.util.JwtUtil;
import com.fajars.expensetracker.wallet.Wallet;
import com.fajars.expensetracker.wallet.usecase.CreateWalletUseCase;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Date;
import java.util.UUID;

@Service
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final MetricsService metricsService;
    private final BusinessEventLogger businessEventLogger;
    private final CreateFreeSubscription createFreeSubscription;
    private final CreateWalletUseCase createWalletUseCase;

    public AuthService(
        UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil,
        AuthenticationManager authenticationManager, MetricsService metricsService,
        BusinessEventLogger businessEventLogger, CreateFreeSubscription createFreeSubscription,
        CreateWalletUseCase createWalletUseCase
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.metricsService = metricsService;
        this.businessEventLogger = businessEventLogger;
        this.createFreeSubscription = createFreeSubscription;
        this.createWalletUseCase = createWalletUseCase;
    }

    /**
     * Register a new user with FREE subscription and default wallet.
     * This operation is atomic - if any step fails, entire registration rolls back.
     *
     * @param req registration request
     * @return authentication response with subscription and wallet info
     */
    @Transactional
    public AuthResponse register(RegisterRequest req) {
        long startTime = System.currentTimeMillis();
        log.info("Starting registration for email: {}", req.email());

        // Step 1: Validate email uniqueness
        if (userRepository.findByEmail(req.email()).isPresent()) {
            throw new IllegalArgumentException("Email already in use");
        }

        // Step 2: Create and save user
        User user = User.builder()
            .id(UUID.randomUUID())
            .email(req.email())
            .passwordHash(passwordEncoder.encode(req.password()))
            .name(req.name())
            .createdAt(new Date())
            .build();

        user = userRepository.save(user);
        log.debug("User created: userId={}", user.getId());

        // Step 3: Create FREE subscription
        Subscription subscription = createFreeSubscription.createFree(user.getId());
        log.debug("FREE subscription created: subscriptionId={}", subscription.getId());

        // Step 4: Create default wallet
        Wallet defaultWallet = createWalletUseCase.createDefaultForNewUser(user.getId());
        log.debug("Default wallet created: walletId={}", defaultWallet.getId());

        // Step 5: Generate JWT token (AFTER all database operations)
        String token = jwtUtil.generateToken(user.getEmail());

        // Step 6: Log business events and metrics
        String ipAddress = getClientIpAddress();
        businessEventLogger.logUserRegistration(user.getEmail(), ipAddress);
        metricsService.recordUserRegistration();
        metricsService.incrementCounter("user.registered.with_subscription");

        long duration = System.currentTimeMillis() - startTime;
        log.info("Registration completed successfully: userId={}, email={}, duration={}ms",
                user.getId(), user.getEmail(), duration);

        // Step 7: Build enhanced response with subscription and wallet info
        SubscriptionInfo subscriptionInfo = new SubscriptionInfo(
                subscription.getId(),
                subscription.getPlan(),
                subscription.getStatus()
        );

        WalletInfo walletInfo = new WalletInfo(
                defaultWallet.getId(),
                defaultWallet.getName(),
                defaultWallet.getCurrency(),
                defaultWallet.getInitialBalance()
        );

        return new AuthResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getName(),
                subscriptionInfo,
                walletInfo
        );
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
