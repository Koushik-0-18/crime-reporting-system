package com.crimereport.dao;

import com.crimereport.db.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CitizenDAO {

    public boolean registerCitizen(String fullName, String mobileNumber, String email, String address, String password) {
        String sql = "INSERT INTO citizens (full_name, mobile_number, email, address, password) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, fullName);
            stmt.setString(2, mobileNumber);
            stmt.setString(3, email);
            stmt.setString(4, address);
            stmt.setString(5, password);
            stmt.executeUpdate();
            return true;
        } catch (Exception e) {
            System.out.println("Registration failed: " + e.getMessage());
            return false;
        }
    }

    public int loginCitizen(String mobileNumber, String password) {
        String sql = "SELECT citizen_id FROM citizens WHERE mobile_number = ? AND password = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, mobileNumber);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("citizen_id");
            }
        } catch (Exception e) {
            System.out.println("Login failed: " + e.getMessage());
        }
        return -1;
    }
    public boolean uploadComplaintEvidence(int complaintId, String fileUrl) {
        String sql = "INSERT INTO complaint_evidence (complaint_id, file_url) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, complaintId);
            stmt.setString(2, fileUrl);
            stmt.executeUpdate();
            return true;
        } catch (Exception e) {
            System.out.println("Failed to upload evidence: " + e.getMessage());
            return false;
        }
    }

    public String[] getCaseDetailsForCitizen(int complaintId) {
        String sql = "SELECT c.case_id, c.current_status, c.case_summary, c.final_result, " +
                "p.name, p.phone, p.badge_id FROM cases c " +
                "JOIN police p ON c.assigned_officer_id = p.police_id " +
                "WHERE c.complaint_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, complaintId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new String[]{
                        String.valueOf(rs.getInt("case_id")),
                        rs.getString("current_status"),
                        rs.getString("case_summary"),
                        rs.getString("final_result"),
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getString("badge_id")
                };
            }
        } catch (Exception e) {
            System.out.println("Failed to fetch case details: " + e.getMessage());
        }
        return null;
    }
}