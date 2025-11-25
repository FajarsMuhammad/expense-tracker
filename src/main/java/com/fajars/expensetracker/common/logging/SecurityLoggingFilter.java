package com.fajars.expensetracker.common.logging;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Enumeration;

/**
 * Filter to log security-related events (authentication and authorization)
 */
@Component
@Order(2) // Run after CorrelationIdFilter
@Slf4j
public class SecurityLoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String method = httpRequest.getMethod();
        String uri = httpRequest.getRequestURI();
        String ipAddress = getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        // Log the request
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = (authentication != null && authentication.isAuthenticated())
                ? authentication.getName()
                : "anonymous";

        log.debug("Security check: {} {} by user: {} from IP: {}", method, uri, username, ipAddress);

        long startTime = System.currentTimeMillis();

        try {
            chain.doFilter(request, response);

            long duration = System.currentTimeMillis() - startTime;
            int status = httpResponse.getStatus();

            // Log authentication/authorization results
            if (status == 401) {
                log.warn("Authentication failed: {} {} from IP: {} - User-Agent: {}",
                        method, uri, ipAddress, userAgent);
            } else if (status == 403) {
                log.warn("Authorization denied: {} {} for user: {} from IP: {} - User-Agent: {}",
                        method, uri, username, ipAddress, userAgent);
            } else if (uri.contains("/login") || uri.contains("/register") || uri.contains("/auth")) {
                if (status >= 200 && status < 300) {
                    log.info("Authentication successful: {} {} for user: {} from IP: {} in {}ms",
                            method, uri, username, ipAddress, duration);
                } else {
                    log.warn("Authentication attempt failed: {} {} from IP: {} - Status: {} in {}ms",
                            method, uri, ipAddress, status, duration);
                }
            }

            // Detect suspicious activities
            detectSuspiciousActivity(httpRequest, httpResponse, duration);

        } catch (Exception e) {
            log.error("Security filter error for {} {} from IP: {}: {}",
                    method, uri, ipAddress, e.getMessage());
            throw e;
        }
    }

    private void detectSuspiciousActivity(HttpServletRequest request, HttpServletResponse response, long duration) {
        String uri = request.getRequestURI();
        String method = request.getMethod();
        String ipAddress = getClientIpAddress(request);
        int status = response.getStatus();

        // Detect potential SQL injection attempts
        String queryString = request.getQueryString();
        if (queryString != null && containsSqlInjectionPattern(queryString)) {
            log.warn("Potential SQL injection attempt detected: {} {} from IP: {} - Query: {}",
                    method, uri, ipAddress, sanitizeQueryString(queryString));
        }

        // Detect access to non-existent resources (potential scanning)
        if (status == 404 && !uri.contains("/api/")) {
            log.debug("Access to non-existent resource: {} from IP: {}", uri, ipAddress);
        }

        // Detect unusually slow requests (potential DoS)
        if (duration > 10000) { // 10 seconds
            log.warn("Slow request detected: {} {} took {}ms from IP: {}",
                    method, uri, duration, ipAddress);
        }

        // Detect large response bodies (potential data exfiltration)
        String contentLength = response.getHeader("Content-Length");
        if (contentLength != null) {
            long size = Long.parseLong(contentLength);
            if (size > 10_000_000) { // 10MB
                log.warn("Large response detected: {} {} returned {} bytes to IP: {}",
                        method, uri, size, ipAddress);
            }
        }
    }

    private boolean containsSqlInjectionPattern(String input) {
        String lowerInput = input.toLowerCase();
        String[] sqlPatterns = {
                "union select", "drop table", "insert into", "delete from",
                "update set", "exec(", "execute(", "script>", "javascript:",
                "or 1=1", "' or '1'='1", "admin'--", "' or 1=1--"
        };

        for (String pattern : sqlPatterns) {
            if (lowerInput.contains(pattern)) {
                return true;
            }
        }
        return false;
    }

    private String sanitizeQueryString(String queryString) {
        if (queryString == null) {
            return "";
        }
        // Limit length and remove sensitive patterns
        String sanitized = queryString.length() > 100
                ? queryString.substring(0, 100) + "..."
                : queryString;
        return sanitized.replaceAll("(password|token|secret|key)=[^&]*", "$1=***");
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String[] headerNames = {
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "HTTP_X_FORWARDED",
                "HTTP_X_CLUSTER_CLIENT_IP",
                "HTTP_CLIENT_IP",
                "HTTP_FORWARDED_FOR",
                "HTTP_FORWARDED",
                "HTTP_VIA",
                "REMOTE_ADDR"
        };

        for (String header : headerNames) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For can contain multiple IPs, take the first one
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }

        return request.getRemoteAddr();
    }
}
