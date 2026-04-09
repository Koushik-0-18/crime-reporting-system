package com.crimereport.servlet;

import com.crimereport.dao.IODAO;
import com.crimereport.security.AuthTokenService;
import com.crimereport.security.CorsUtil;
import com.crimereport.security.ServletUtil;
import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/io/*")
public class IOServlet extends HttpServlet {

    private final IODAO ioDAO = new IODAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("application/json");
        CorsUtil.apply(req, res);
        PrintWriter out = res.getWriter();
        String path = req.getPathInfo();
        if (path == null || path.isBlank()) {
            ServletUtil.writeError(res, gson, 404, "Endpoint not found");
            return;
        }
        AuthTokenService.SessionData session = AuthTokenService.validate(ServletUtil.bearerToken(req));
        if (session == null || !"police".equals(session.type()) || !"IO".equals(session.role())) {
            ServletUtil.writeError(res, gson, 401, "Unauthorized");
            return;
        }
        int officerId = session.userId();

        if (path.equals("/assigned-cases")) {
            Integer requestedOfficerId = ServletUtil.parseIntParam(req, "officer_id");
            if (requestedOfficerId == null || requestedOfficerId != officerId) {
                ServletUtil.writeError(res, gson, 403, "Forbidden");
                return;
            }
            List<String[]> cases = ioDAO.getAssignedCases(officerId);
            out.print(gson.toJson(cases));

        } else if (path.equals("/closed-cases")) {
            Integer requestedOfficerId = ServletUtil.parseIntParam(req, "officer_id");
            if (requestedOfficerId == null || requestedOfficerId != officerId) {
                ServletUtil.writeError(res, gson, 403, "Forbidden");
                return;
            }
            List<String[]> cases = ioDAO.getClosedCasesForIO(officerId);
            out.print(gson.toJson(cases));

        } else if (path.equals("/citizen-info")) {
            Integer caseId = ServletUtil.parseIntParam(req, "case_id");
            if (caseId == null || !ioDAO.isCaseAssignedToOfficer(caseId, officerId)) {
                ServletUtil.writeError(res, gson, 403, "Forbidden");
                return;
            }
            String[] info = ioDAO.getCitizenInfoForCase(caseId);
            out.print(gson.toJson(info));

        } else if (path.equals("/complaint-evidence")) {
            Integer caseId = ServletUtil.parseIntParam(req, "case_id");
            if (caseId == null || !ioDAO.isCaseAssignedToOfficer(caseId, officerId)) {
                ServletUtil.writeError(res, gson, 403, "Forbidden");
                return;
            }
            List<String[]> evidence = ioDAO.getComplaintEvidenceForCase(caseId);
            out.print(gson.toJson(evidence));

        } else if (path.equals("/case-evidence")) {
            Integer caseId = ServletUtil.parseIntParam(req, "case_id");
            if (caseId == null || !ioDAO.isCaseAssignedToOfficer(caseId, officerId)) {
                ServletUtil.writeError(res, gson, 403, "Forbidden");
                return;
            }
            List<String[]> evidence = ioDAO.getCaseEvidenceForIO(caseId);
            out.print(gson.toJson(evidence));

        } else if (path.equals("/diary")) {
            Integer caseId = ServletUtil.parseIntParam(req, "case_id");
            if (caseId == null || !ioDAO.isCaseAssignedToOfficer(caseId, officerId)) {
                ServletUtil.writeError(res, gson, 403, "Forbidden");
                return;
            }
            List<String[]> diary = ioDAO.getCaseDiary(caseId);
            out.print(gson.toJson(diary));
        } else {
            ServletUtil.writeError(res, gson, 404, "Endpoint not found");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("application/json");
        CorsUtil.apply(req, res);
        PrintWriter out = res.getWriter();
        String path = req.getPathInfo();
        if (path == null || path.isBlank()) {
            ServletUtil.writeError(res, gson, 404, "Endpoint not found");
            return;
        }
        AuthTokenService.SessionData session = AuthTokenService.validate(ServletUtil.bearerToken(req));
        if (session == null || !"police".equals(session.type()) || !"IO".equals(session.role())) {
            ServletUtil.writeError(res, gson, 401, "Unauthorized");
            return;
        }
        int officerId = session.userId();

        if (path.equals("/update-status")) {
            Integer caseId = ServletUtil.parseIntParam(req, "case_id");
            if (caseId == null || !ioDAO.isCaseAssignedToOfficer(caseId, officerId)) {
                ServletUtil.writeError(res, gson, 403, "Forbidden");
                return;
            }
            String status = req.getParameter("status");
            if (status == null || status.isBlank()) {
                ServletUtil.writeError(res, gson, 400, "Invalid status");
                return;
            }
            boolean success = ioDAO.updateCaseStatus(caseId, status);
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            if (!success) {
                res.setStatus(400);
                response.put("message", "Failed to update status");
            }
            out.print(gson.toJson(response));

        } else if (path.equals("/add-diary")) {
            Integer caseId = ServletUtil.parseIntParam(req, "case_id");
            Integer requestedOfficerId = ServletUtil.parseIntParam(req, "officer_id");
            if (caseId == null || requestedOfficerId == null || requestedOfficerId != officerId
                    || !ioDAO.isCaseAssignedToOfficer(caseId, officerId)) {
                ServletUtil.writeError(res, gson, 403, "Forbidden");
                return;
            }
            String note = req.getParameter("note");
            if (note == null || note.isBlank()) {
                ServletUtil.writeError(res, gson, 400, "Invalid note");
                return;
            }
            boolean success = ioDAO.addCaseDiaryEntry(caseId, officerId, note);
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            if (!success) {
                res.setStatus(400);
                response.put("message", "Failed to add diary entry");
            }
            out.print(gson.toJson(response));

        } else if (path.equals("/upload-evidence")) {
            Integer caseId = ServletUtil.parseIntParam(req, "case_id");
            Integer requestedOfficerId = ServletUtil.parseIntParam(req, "officer_id");
            if (caseId == null || requestedOfficerId == null || requestedOfficerId != officerId
                    || !ioDAO.isCaseAssignedToOfficer(caseId, officerId)) {
                ServletUtil.writeError(res, gson, 403, "Forbidden");
                return;
            }
            String fileUrl = req.getParameter("file_url");
            if (fileUrl == null || fileUrl.isBlank()) {
                ServletUtil.writeError(res, gson, 400, "Invalid file_url");
                return;
            }
            boolean success = ioDAO.uploadCaseEvidence(caseId, officerId, fileUrl);
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            if (!success) {
                res.setStatus(400);
                response.put("message", "Failed to upload evidence");
            }
            out.print(gson.toJson(response));
        } else if (path.equals("/close-case")) {
            Integer caseId = ServletUtil.parseIntParam(req, "case_id");
            if (caseId == null || !ioDAO.isCaseAssignedToOfficer(caseId, officerId)) {
                ServletUtil.writeError(res, gson, 403, "Forbidden");
                return;
            }
            String summary = req.getParameter("summary");
            String result = req.getParameter("final_result");
            if (summary == null || result == null || summary.isBlank() || result.isBlank()) {
                ServletUtil.writeError(res, gson, 400, "Invalid close-case payload");
                return;
            }
            boolean success = ioDAO.closeCase(caseId, summary, result);
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            if (!success) {
                res.setStatus(400);
                response.put("message", "Failed to close case");
            }
            out.print(gson.toJson(response));
        } else {
            ServletUtil.writeError(res, gson, 404, "Endpoint not found");
        }
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse res) {
        CorsUtil.apply(req, res);
        res.setStatus(200);
    }
}
