package com.crimereport.dao;

import com.crimereport.db.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PoliceAuthDAO {

    public String[] loginPolice(String badgeId, String password) {
        String sql = "SELECT police_id, name, role FROM police WHERE badge_id = ? AND password = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, badgeId);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new String[]{
                        String.valueOf(rs.getInt("police_id")),
                        rs.getString("name"),
                        rs.getString("role")
                };
            }
        } catch (Exception e) {
            System.out.println("Police login failed: " + e.getMessage());
        }
        return null;
    }
}