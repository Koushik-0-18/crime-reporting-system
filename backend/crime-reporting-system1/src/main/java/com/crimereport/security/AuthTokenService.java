package com.crimereport.security;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AuthTokenService {
    private static final Map<String, SessionData> SESSIONS = new ConcurrentHashMap<>();
    private static final long TOKEN_TTL_MILLIS = resolveTokenTtlMillis();
    private static volatile long lastCleanupAt = System.currentTimeMillis();

    public static String createCitizenToken(int citizenId) {
        String token = UUID.randomUUID().toString();
        SESSIONS.put(token, new SessionData("citizen", citizenId, null, System.currentTimeMillis() + TOKEN_TTL_MILLIS));
        return token;
    }

    public static String createPoliceToken(int policeId, String role) {
        String token = UUID.randomUUID().toString();
        SESSIONS.put(token, new SessionData("police", policeId, role, System.currentTimeMillis() + TOKEN_TTL_MILLIS));
        return token;
    }

    public static SessionData validate(String token) {
        cleanupExpiredIfNeeded();
        if (token == null || token.isBlank()) {
            return null;
        }
        SessionData session = SESSIONS.get(token);
        if (session == null) {
            return null;
        }
        if (System.currentTimeMillis() > session.expiresAt()) {
            SESSIONS.remove(token);
            return null;
        }
        return session;
    }

    private static void cleanupExpiredIfNeeded() {
        long now = System.currentTimeMillis();
        if (now - lastCleanupAt < 5L * 60L * 1000L) {
            return;
        }
        SESSIONS.entrySet().removeIf(entry -> now > entry.getValue().expiresAt());
        lastCleanupAt = now;
    }

    private static long resolveTokenTtlMillis() {
        String ttlSeconds = System.getenv("AUTH_TOKEN_TTL_SECONDS");
        if (ttlSeconds == null || ttlSeconds.isBlank()) {
            return 24L * 60L * 60L * 1000L;
        }
        try {
            long seconds = Long.parseLong(ttlSeconds);
            if (seconds <= 0) {
                return 24L * 60L * 60L * 1000L;
            }
            return seconds * 1000L;
        } catch (NumberFormatException e) {
            return 24L * 60L * 60L * 1000L;
        }
    }

    public record SessionData(String type, int userId, String role, long expiresAt) {}
}
