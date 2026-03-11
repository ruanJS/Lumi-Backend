package com.lumi.ai.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimitFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> loginBuckets         = new ConcurrentHashMap<>();
    private final Map<String, Bucket> registerBuckets      = new ConcurrentHashMap<>();
    private final Map<String, Bucket> forgotPasswordBuckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path  = request.getServletPath();
        String method = request.getMethod();

        if ("POST".equalsIgnoreCase(method)) {
            Bucket bucket = resolveBucket(path, resolveClientIp(request));

            if (bucket != null && !bucket.tryConsume(1)) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write(
                    "{\"status\":429,\"error\":\"Too Many Requests\"," +
                    "\"message\":\"Muitas tentativas. Tente novamente em alguns instantes.\"}"
                );
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private Bucket resolveBucket(String path, String ip) {
        return switch (path) {
            case "/api/auth/login"           -> loginBuckets.computeIfAbsent(ip,
                    k -> buildBucket(5, Duration.ofMinutes(1)));
            case "/api/auth/register"        -> registerBuckets.computeIfAbsent(ip,
                    k -> buildBucket(3, Duration.ofMinutes(1)));
            case "/api/auth/forgot-password" -> forgotPasswordBuckets.computeIfAbsent(ip,
                    k -> buildBucket(3, Duration.ofMinutes(1)));
            default                          -> null;
        };
    }

    private Bucket buildBucket(int capacity, Duration refillPeriod) {
        Bandwidth limit = Bandwidth.classic(capacity,
                Refill.intervally(capacity, refillPeriod));
        return Bucket.builder().addLimit(limit).build();
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
