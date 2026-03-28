package com.crimereport.dao;

import com.crimereport.db.DBConnection;
import com.crimereport.model.Complaint;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ComplaintDAO {

    public boolean fileComplaint(int citizenId, String description, String incidentDate, String incidentTime, String location) {
        String sql = "INSERT INTO complaints (citizen_id, description, incident_date, incident_time, location) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, citizenId);
            stmt.setString(2, description);
            stmt.setDate(3, java.sql.Date.valueOf(incidentDate));
            stmt.setTime(4, java.sql.Time.valueOf(incidentTime));
            stmt.setString(5, location);
            stmt.executeUpdate();
            return true;
        } catch (Exception e) {
            System.out.println("Failed to file complaint: " + e.getMessage());
            return false;
        }
    }

    public List<Complaint> getComplaintsByCitizen(int citizenId) {
        List<Complaint> complaints = new ArrayList<>();
        String sql = "SELECT * FROM complaints WHERE citizen_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, citizenId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                complaints.add(new Complaint(
                        rs.getInt("complaint_id"),
                        rs.getInt("citizen_id"),
                        rs.getString("description"),
                        rs.getString("incident_date"),
                        rs.getString("incident_time"),
                        rs.getString("location"),
                        rs.getString("status")
                ));
            }
        } catch (Exception e) {
            System.out.println("Failed to fetch complaints: " + e.getMessage());
        }
        return complaints;
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