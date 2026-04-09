package com.crimereport.dao;

import com.crimereport.db.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class IODAO {

    public List<String[]> getAssignedCases(int officerId) {
        List<String[]> cases = new ArrayList<>();
        String sql = "SELECT c.case_id, c.current_status, c.case_summary, co.description, co.location " +
                "FROM cases c JOIN complaints co ON c.complaint_id = co.complaint_id " +
                "WHERE c.assigned_officer_id = ? AND c.current_status != 'Closed'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, officerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    cases.add(new String[]{
                            String.valueOf(rs.getInt("case_id")),
                            rs.getString("current_status"),
                            rs.getString("case_summary"),
                            rs.getString("description"),
                            rs.getString("location")
                    });
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to fetch cases: " + e.getMessage());
        }
        return cases;
    }

    public boolean updateCaseStatus(int caseId, String newStatus) {
        String sql = "UPDATE cases SET current_status = ? WHERE case_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newStatus);
            stmt.setInt(2, caseId);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Failed to update status: " + e.getMessage());
            return false;
        }
    }

    public boolean addCaseDiaryEntry(int caseId, int officerId, String note) {
        String sql = "INSERT INTO case_diary (case_id, officer_id, note) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, caseId);
            stmt.setInt(2, officerId);
            stmt.setString(3, note);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Failed to add diary entry: " + e.getMessage());
            return false;
        }
    }

    public boolean uploadCaseEvidence(int caseId, int officerId, String fileUrl) {
        String sql = "INSERT INTO case_evidence (case_id, uploaded_by, uploaded_by_id, file_url) VALUES (?, 'police', ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, caseId);
            stmt.setInt(2, officerId);
            stmt.setString(3, fileUrl);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Failed to upload evidence: " + e.getMessage());
            return false;
        }
    }

    public boolean closeCase(int caseId, String summary, String finalResult) {
        String sql = "UPDATE cases SET current_status = 'Closed', case_summary = ?, final_result = ?, closed_at = now() WHERE case_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, summary);
            stmt.setString(2, finalResult);
            stmt.setInt(3, caseId);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Failed to close case: " + e.getMessage());
            return false;
        }
    }

    public List<String[]> getCaseDiary(int caseId) {
        List<String[]> entries = new ArrayList<>();
        String sql = "SELECT note, created_at FROM case_diary WHERE case_id = ? ORDER BY created_at ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, caseId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    entries.add(new String[]{
                            rs.getString("note"),
                            rs.getString("created_at")
                    });
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to fetch diary: " + e.getMessage());
        }
        return entries;
    }
    public String[] getCitizenInfoForCase(int caseId) {
        String sql = "SELECT ci.citizen_id, ci.full_name, ci.mobile_number, ci.email, ci.address " +
                "FROM citizens ci " +
                "JOIN complaints co ON ci.citizen_id = co.citizen_id " +
                "JOIN cases c ON co.complaint_id = c.complaint_id " +
                "WHERE c.case_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, caseId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new String[]{
                            String.valueOf(rs.getInt("citizen_id")),
                            rs.getString("full_name"),
                            rs.getString("mobile_number"),
                            rs.getString("email"),
                            rs.getString("address")
                    };
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to fetch citizen info: " + e.getMessage());
        }
        return null;
    }

    public List<String[]> getComplaintEvidenceForCase(int caseId) {
        List<String[]> evidence = new ArrayList<>();
        String sql = "SELECT ce.evidence_id, ce.file_url, ce.uploaded_at " +
                "FROM complaint_evidence ce " +
                "JOIN cases c ON ce.complaint_id = c.complaint_id " +
                "WHERE c.case_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, caseId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    evidence.add(new String[]{
                            String.valueOf(rs.getInt("evidence_id")),
                            rs.getString("file_url"),
                            rs.getString("uploaded_at")
                    });
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to fetch complaint evidence: " + e.getMessage());
        }
        return evidence;
    }

    public List<String[]> getCaseEvidenceForIO(int caseId) {
        List<String[]> evidence = new ArrayList<>();
        String sql = "SELECT evidence_id, uploaded_by, file_url, uploaded_at " +
                "FROM case_evidence WHERE case_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, caseId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    evidence.add(new String[]{
                            String.valueOf(rs.getInt("evidence_id")),
                            rs.getString("uploaded_by"),
                            rs.getString("file_url"),
                            rs.getString("uploaded_at")
                    });
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to fetch case evidence: " + e.getMessage());
        }
        return evidence;
    }

    public List<String[]> getClosedCasesForIO(int officerId) {
        List<String[]> cases = new ArrayList<>();
        String sql = "SELECT c.case_id, c.current_status, c.case_summary, c.final_result, " +
                "c.closed_at, co.description, co.location, co.incident_date, " +
                "ci.full_name, ci.mobile_number " +
                "FROM cases c " +
                "JOIN complaints co ON c.complaint_id = co.complaint_id " +
                "JOIN citizens ci ON co.citizen_id = ci.citizen_id " +
                "WHERE c.assigned_officer_id = ? AND c.current_status = 'Closed'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, officerId);
            try (ResultSet rs = stmt.executeQuery()) {
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
                            rs.getString("full_name"),
                            rs.getString("mobile_number")
                    });
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to fetch closed cases: " + e.getMessage());
        }
        return cases;
    }

    public boolean isCaseAssignedToOfficer(int caseId, int officerId) {
        String sql = "SELECT 1 FROM cases WHERE case_id = ? AND assigned_officer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, caseId);
            stmt.setInt(2, officerId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            System.out.println("Failed to validate case assignment: " + e.getMessage());
            return false;
        }
    }
}
