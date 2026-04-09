package com.crimereport.dao;

import com.crimereport.db.DBConnection;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PoliceAuthDAO {

    public String[] loginPolice(String badgeId, String password) {
        String sql = "SELECT police_id, name, role, password FROM police WHERE badge_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, badgeId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    if (!passwordMatches(password, storedPassword)) {
                        return null;
                    }
                    return new String[]{
                            String.valueOf(rs.getInt("police_id")),
                            rs.getString("name"),
                            rs.getString("role")
                    };
                }
            }
        } catch (Exception e) {
            System.out.println("Police login failed: " + e.getMessage());
        }
        return null;
    }

    private boolean passwordMatches(String inputPassword, String storedPassword) {
        if (storedPassword == null || storedPassword.isBlank()) {
            return false;
        }
        if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$")) {
            return BCrypt.checkpw(inputPassword, storedPassword);
        }
        return inputPassword.equals(storedPassword);
    }
}
