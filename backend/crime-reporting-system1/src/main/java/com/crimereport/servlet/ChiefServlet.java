package com.crimereport.servlet;

import com.crimereport.dao.ChiefDAO;
import com.crimereport.model.Complaint;
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

@WebServlet("/api/chief/*")
public class ChiefServlet extends HttpServlet {

    private final ChiefDAO chiefDAO = new ChiefDAO();
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
        if (session == null || !"police".equals(session.type()) || !"Chief".equals(session.role())) {
            ServletUtil.writeError(res, gson, 401, "Unauthorized");
            return;
        }

        if (path.equals("/pending-complaints")) {
            List<Complaint> complaints = chiefDAO.getPendingComplaints();
            out.print(gson.toJson(complaints));

        } else if (path.equals("/rejected-complaints")) {
            List<Complaint> complaints = chiefDAO.getRejectedComplaints();
            out.print(gson.toJson(complaints));

        } else if (path.equals("/active-cases")) {
            List<String[]> cases = chiefDAO.getActiveCases();
            out.print(gson.toJson(cases));

        } else if (path.equals("/closed-cases")) {
            List<String[]> cases = chiefDAO.getClosedCases();
            out.print(gson.toJson(cases));

        } else if (path.equals("/officers")) {
            List<String[]> officers = chiefDAO.getAvailableOfficers();
            out.print(gson.toJson(officers));

        } else if (path.equals("/case-details")) {
            Integer caseId = ServletUtil.parseIntParam(req, "case_id");
            if (caseId == null) {
                ServletUtil.writeError(res, gson, 400, "Invalid case_id");
                return;
            }
            String[] details = chiefDAO.getCaseDetailsForChief(caseId);
            out.print(gson.toJson(details));
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
        if (session == null || !"police".equals(session.type()) || !"Chief".equals(session.role())) {
            ServletUtil.writeError(res, gson, 401, "Unauthorized");
            return;
        }

        if (path.equals("/reject-complaint")) {
            Integer complaintId = ServletUtil.parseIntParam(req, "complaint_id");
            if (complaintId == null) {
                ServletUtil.writeError(res, gson, 400, "Invalid complaint_id");
                return;
            }
            boolean success = chiefDAO.rejectComplaint(complaintId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            if (!success) {
                res.setStatus(400);
                response.put("message", "Failed to reject complaint");
            }
            out.print(gson.toJson(response));

        } else if (path.equals("/convert-to-case")) {
            Integer complaintId = ServletUtil.parseIntParam(req, "complaint_id");
            Integer officerId = ServletUtil.parseIntParam(req, "officer_id");
            if (complaintId == null || officerId == null) {
                ServletUtil.writeError(res, gson, 400, "Invalid complaint_id or officer_id");
                return;
            }
            boolean success = chiefDAO.convertToCase(complaintId, officerId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            if (!success) {
                res.setStatus(400);
                response.put("message", "Failed to convert complaint to case");
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
