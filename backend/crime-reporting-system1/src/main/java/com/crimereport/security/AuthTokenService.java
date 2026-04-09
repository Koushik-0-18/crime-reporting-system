package com.crimereport.security;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AuthTokenService {
    private static final Map<String, SessionData> SESSIONS = new ConcurrentHashMap<>();

    public static String createCitizenToken(int citizenId) {
        String token = UUID.randomUUID().toString();
        SESSIONS.put(token, new SessionData("citizen", citizenId, null));
        return token;
    }

    public static String createPoliceToken(int policeId, String role) {
        String token = UUID.randomUUID().toString();
        SESSIONS.put(token, new SessionData("police", policeId, role));
        return token;
    }

    public static SessionData validate(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        return SESSIONS.get(token);
    }

    public record SessionData(String type, int userId, String role) {}
}
