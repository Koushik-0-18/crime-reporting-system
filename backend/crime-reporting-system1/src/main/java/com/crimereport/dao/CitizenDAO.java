package com.crimereport.dao;

import com.crimereport.db.DBConnection;
import org.mindrot.jbcrypt.BCrypt;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CitizenDAO {

    public boolean registerCitizen(String fullName, String mobileNumber, String email, String address, String password) {
        String sql = "INSERT INTO citizens (full_name, mobile_number, email, address, password) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
            stmt.setString(1, fullName);
            stmt.setString(2, mobileNumber);
            stmt.setString(3, email);
            stmt.setString(4, address);
            stmt.setString(5, hashedPassword);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Registration failed: " + e.getMessage());
            return false;
        }
    }

    public int loginCitizen(String mobileNumber, String password) {
        String sql = "SELECT citizen_id, password FROM citizens WHERE mobile_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, mobileNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    if (passwordMatches(password, storedPassword)) {
                        int citizenId = rs.getInt("citizen_id");
                        if (!isBcryptHash(storedPassword)) {
                            migrateToHashedPassword(conn, citizenId, password);
                        }
                        return citizenId;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Login failed: " + e.getMessage());
        }
        return -1;
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

    private void migrateToHashedPassword(Connection conn, int citizenId, String password) {
        String sql = "UPDATE citizens SET password = ? WHERE citizen_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, BCrypt.hashpw(password, BCrypt.gensalt()));
            stmt.setInt(2, citizenId);
            stmt.executeUpdate();
        } catch (Exception e) {
            System.out.println("Password hash migration failed: " + e.getMessage());
        }
    }
}
