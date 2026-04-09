package com.crimereport.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Set;

public class CorsUtil {
    private static final Set<String> DEFAULT_ALLOWED_ORIGINS = Set.of(
            "http://localhost:3000",
            "http://localhost:5173",
            "http://localhost:5500",
            "https://crime-reporting-system-production.up.railway.app"
    );

    public static void apply(HttpServletRequest req, HttpServletResponse res) {
        String origin = req.getHeader("Origin");
        String allowedOrigin = getAllowedOrigin(origin);
        if (allowedOrigin != null) {
            res.setHeader("Access-Control-Allow-Origin", allowedOrigin);
            res.setHeader("Vary", "Origin");
        }
        res.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        res.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    private static String getAllowedOrigin(String origin) {
        if (origin == null || origin.isBlank()) {
            return null;
        }
        String configured = System.getenv("CORS_ALLOWED_ORIGIN");
        if (configured != null && !configured.isBlank()) {
            return configured.equals(origin) ? origin : null;
        }
        return DEFAULT_ALLOWED_ORIGINS.contains(origin) ? origin : null;
    }
}
