package com.crimereport.servlet;

import com.crimereport.dao.ChiefDAO;
import com.crimereport.model.Complaint;
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
        res.setHeader("Access-Control-Allow-Origin", "*");
        PrintWriter out = res.getWriter();
        String path = req.getPathInfo();

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
            int caseId = Integer.parseInt(req.getParameter("case_id"));
            String[] details = chiefDAO.getCaseDetailsForChief(caseId);
            out.print(gson.toJson(details));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("application/json");
        res.setHeader("Access-Control-Allow-Origin", "*");
        PrintWriter out = res.getWriter();
        String path = req.getPathInfo();

        if (path.equals("/reject-complaint")) {
            int complaintId = Integer.parseInt(req.getParameter("complaint_id"));
            boolean success = chiefDAO.rejectComplaint(complaintId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            out.print(gson.toJson(response));

        } else if (path.equals("/convert-to-case")) {
            int complaintId = Integer.parseInt(req.getParameter("complaint_id"));
            int officerId = Integer.parseInt(req.getParameter("officer_id"));
            boolean success = chiefDAO.convertToCase(complaintId, officerId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            out.print(gson.toJson(response));
        }
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse res) {
        res.setHeader("Access-Control-Allow-Origin", "*");
        res.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        res.setHeader("Access-Control-Allow-Headers", "Content-Type");
        res.setStatus(200);
    }
}