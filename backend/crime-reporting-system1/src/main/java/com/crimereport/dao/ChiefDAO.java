package com.crimereport.dao;

import com.crimereport.db.DBConnection;
import com.crimereport.model.Complaint;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ChiefDAO {

    public List<Complaint> getPendingComplaints() {
        List<Complaint> complaints = new ArrayList<>();
        String sql = "SELECT * FROM complaints WHERE status = 'Pending'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
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
            System.out.println("Failed to fetch pending complaints: " + e.getMessage());
        }
        return complaints;
    }

    public boolean rejectComplaint(int complaintId) {
        String sql = "UPDATE complaints SET status = 'Rejected' WHERE complaint_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, complaintId);
            stmt.executeUpdate();
            return true;
        } catch (Exception e) {
            System.out.println("Failed to reject complaint: " + e.getMessage());
            return false;
        }
    }

    public boolean convertToCase(int complaintId, int officerId) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // Update complaint status to Converted
            String updateComplaint = "UPDATE complaints SET status = 'Converted' WHERE complaint_id = ?";
            PreparedStatement stmt1 = conn.prepareStatement(updateComplaint);
            stmt1.setInt(1, complaintId);
            stmt1.executeUpdate();

            // Create a new case
            String insertCase = "INSERT INTO cases (complaint_id, assigned_officer_id) VALUES (?, ?)";
            PreparedStatement stmt2 = conn.prepareStatement(insertCase);
            stmt2.setInt(1, complaintId);
            stmt2.setInt(2, officerId);
            stmt2.executeUpdate();

            conn.commit();
            return true;
        } catch (Exception e) {
            System.out.println("Failed to convert complaint to case: " + e.getMessage());
            try { if (conn != null) conn.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
            return false;
        }
    }

    public List<String[]> getAvailableOfficers() {
        List<String[]> officers = new ArrayList<>();
        String sql = "SELECT police_id, name, badge_id FROM police WHERE role = 'IO'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                officers.add(new String[]{
                        String.valueOf(rs.getInt("police_id")),
                        rs.getString("name"),
                        rs.getString("badge_id")
                });
            }
        } catch (Exception e) {
            System.out.println("Failed to fetch officers: " + e.getMessage());
        }
        return officers;
    }
    public List<Complaint> getRejectedComplaints() {
        List<Complaint> complaints = new ArrayList<>();
        String sql = "SELECT * FROM complaints WHERE status = 'Rejected'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
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
            System.out.println("Failed to fetch rejected complaints: " + e.getMessage());
        }
        return complaints;
    }

    public List<String[]> getActiveCases() {
        List<String[]> cases = new ArrayList<>();
        String sql = "SELECT c.case_id, c.current_status, c.case_summary, " +
                "co.description, co.location, co.incident_date, " +
                "p.name, p.badge_id, ci.full_name, ci.mobile_number " +
                "FROM cases c " +
                "JOIN complaints co ON c.complaint_id = co.complaint_id " +
                "JOIN police p ON c.assigned_officer_id = p.police_id " +
                "JOIN citizens ci ON co.citizen_id = ci.citizen_id " +
                "WHERE c.current_status != 'Closed'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                cases.add(new String[]{
                        String.valueOf(rs.getInt("case_id")),
                        rs.getString("current_status"),
                        rs.getString("case_summary"),
                        rs.getString("description"),
                        rs.getString("location"),
                        rs.getString("incident_date"),
                        rs.getString("name"),
                        rs.getString("badge_id"),
                        rs.getString("full_name"),
                        rs.getString("mobile_number")
                });
            }
        } catch (Exception e) {
            System.out.println("Failed to fetch active cases: " + e.getMessage());
        }
        return cases;
    }

    public List<String[]> getClosedCases() {
        List<String[]> cases = new ArrayList<>();
        String sql = "SELECT c.case_id, c.current_status, c.case_summary, c.final_result, " +
                "c.closed_at, co.description, co.location, co.incident_date, " +
                "p.name, p.badge_id, ci.full_name, ci.mobile_number " +
                "FROM cases c " +
                "JOIN complaints co ON c.complaint_id = co.complaint_id " +
                "JOIN police p ON c.assigned_officer_id = p.police_id " +
                "JOIN citizens ci ON co.citizen_id = ci.citizen_id " +
                "WHERE c.current_status = 'Closed'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                cases.add(new String[]{
                        String.valueOf(rs.getInt("case_id")),
                        rs.getString("current_status"),
                        rs.getString("case_summary"),
                        rs.getString("final_result"),
                        rs.getString("closed_at"),
                        rs.getString("description"),
                        rs.getString("location"),
                        rs.getString("incident_date"),
                        rs.getString("name"),
                        rs.getString("badge_id"),
                        rs.getString("full_name"),
                        rs.getString("mobile_number")
                });
            }
        } catch (Exception e) {
            System.out.println("Failed to fetch closed cases: " + e.getMessage());
        }
        return cases;
    }

    public String[] getCaseDetailsForChief(int caseId) {
        String sql = "SELECT c.case_id, c.current_status, c.case_summary, c.final_result, " +
                "c.created_at, c.closed_at, co.description, co.location, " +
                "co.incident_date, co.incident_time, " +
                "p.name, p.badge_id, p.phone, " +
                "ci.full_name, ci.mobile_number, ci.email " +
                "FROM cases c " +
                "JOIN complaints co ON c.complaint_id = co.complaint_id " +
                "JOIN police p ON c.assigned_officer_id = p.police_id " +
                "JOIN citizens ci ON co.citizen_id = ci.citizen_id " +
                "WHERE c.case_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, caseId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new String[]{
                        String.valueOf(rs.getInt("case_id")),
                        rs.getString("current_status"),
                        rs.getString("case_summary"),
                        rs.getString("final_result"),
                        rs.getString("created_at"),
                        rs.getString("closed_at"),
                        rs.getString("description"),
                        rs.getString("location"),
                        rs.getString("incident_date"),
                        rs.getString("incident_time"),
                        rs.getString("name"),
                        rs.getString("badge_id"),
                        rs.getString("phone"),
                        rs.getString("full_name"),
                        rs.getString("mobile_number"),
                        rs.getString("email")
                };
            }
        } catch (Exception e) {
            System.out.println("Failed to fetch case details: " + e.getMessage());
        }
        return null;
    }
}