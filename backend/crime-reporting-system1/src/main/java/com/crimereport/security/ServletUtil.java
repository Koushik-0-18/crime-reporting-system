package com.crimereport.security;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ServletUtil {
    public static Integer parseIntParam(HttpServletRequest req, String name) {
        String value = req.getParameter(name);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static String bearerToken(HttpServletRequest req) {
        String header = req.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }
        return header.substring(7).trim();
    }

    public static void writeError(HttpServletResponse res, Gson gson, int status, String message) throws IOException {
        res.setStatus(status);
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        res.getWriter().print(gson.toJson(response));
    }
}
