package com.crimereport.security;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AuthTokenService {
    private static final Map<String, SessionData> SESSIONS = new ConcurrentHashMap<>();
    private static final long TOKEN_TTL_MILLIS = 24L * 60L * 60L * 1000L;

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

    public record SessionData(String type, int userId, String role, long expiresAt) {}
}
