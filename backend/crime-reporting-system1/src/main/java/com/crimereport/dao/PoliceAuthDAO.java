package com.crimereport.dao;

import com.crimereport.db.DBConnection;
import org.mindrot.jbcrypt.BCrypt;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
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
                    int policeId = rs.getInt("police_id");
                    if (!isBcryptHash(storedPassword)) {
                        migrateToHashedPassword(conn, policeId, password);
                    }
                    return new String[]{
                            String.valueOf(policeId),
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
        if (isBcryptHash(storedPassword)) {
            return BCrypt.checkpw(inputPassword, storedPassword);
        }
        return MessageDigest.isEqual(
                inputPassword.getBytes(StandardCharsets.UTF_8),
                storedPassword.getBytes(StandardCharsets.UTF_8)
        );
    }

    private boolean isBcryptHash(String value) {
        return value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$");
    }

    private void migrateToHashedPassword(Connection conn, int policeId, String password) {
        String sql = "UPDATE police SET password = ? WHERE police_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, BCrypt.hashpw(password, BCrypt.gensalt()));
            stmt.setInt(2, policeId);
            stmt.executeUpdate();
        } catch (Exception e) {
            System.out.println("Password hash migration failed: " + e.getMessage());
        }
    }
}
