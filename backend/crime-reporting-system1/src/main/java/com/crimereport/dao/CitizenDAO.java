package com.crimereport.dao;

import com.crimereport.db.DBConnection;
import org.mindrot.jbcrypt.BCrypt;
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
                        return rs.getInt("citizen_id");
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
        if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$")) {
            return BCrypt.checkpw(inputPassword, storedPassword);
        }
        return inputPassword.equals(storedPassword);
    }
}
