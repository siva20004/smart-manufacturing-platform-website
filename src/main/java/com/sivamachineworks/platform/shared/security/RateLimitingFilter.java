package com.sivamachineworks.platform.shared.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sivamachineworks.platform.shared.dto.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private int maxRequestsPerWindow = 1000;
    private int maxAuthRequestsPerWindow = 500;
    private long windowDurationMs = 60_000L;

    private final Map<String, ClientWindow> clientWindows = new ConcurrentHashMap<>();
    private final ObjectMapper mapper;

    public RateLimitingFilter() {
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String clientIp = getClientIp(request);
        String uri = request.getRequestURI();
        boolean isSensitive = uri.contains("/api/v1/auth/login") || uri.contains("/api/v1/ai/") || uri.contains("/api/v1/rag/");

        int limit = isSensitive ? maxAuthRequestsPerWindow : maxRequestsPerWindow;
        long now = System.currentTimeMillis();

        ClientWindow window = clientWindows.compute(clientIp + ":" + (isSensitive ? "SENSITIVE" : "GENERAL"), (k, v) -> {
            if (v == null || now - v.windowStartTime > windowDurationMs) {
                return new ClientWindow(now, new AtomicInteger(1));
            }
            v.counter.incrementAndGet();
            return v;
        });

        if (window.counter.get() > limit) {
            response.setStatus(429); // HTTP 429 Too Many Requests
            response.setContentType("application/json");
            response.setHeader("Retry-After", "60");

            ErrorResponse errorResponse = new ErrorResponse(
                    false,
                    Instant.now(),
                    new ErrorResponse.Error("RATE_LIMIT_EXCEEDED", "Rate limit exceeded. Please wait before retrying.", null),
                    UUID.randomUUID().toString()
            );
            response.getWriter().write(mapper.writeValueAsString(errorResponse));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty() || !xfHeader.contains(",")) {
            return request.getRemoteAddr() != null ? request.getRemoteAddr() : "127.0.0.1";
        }
        return xfHeader.split(",")[0].trim();
    }

    public void resetLimitsForTesting() {
        this.maxRequestsPerWindow = 1000;
        this.maxAuthRequestsPerWindow = 500;
        clientWindows.clear();
    }

    public void setLimitsForTesting(int authLimit, int generalLimit) {
        this.maxAuthRequestsPerWindow = authLimit;
        this.maxRequestsPerWindow = generalLimit;
        clientWindows.clear();
    }

    private static class ClientWindow {
        final long windowStartTime;
        final AtomicInteger counter;

        ClientWindow(long windowStartTime, AtomicInteger counter) {
            this.windowStartTime = windowStartTime;
            this.counter = counter;
        }
    }
}
