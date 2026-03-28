package com.crimereport.servlet;

import com.crimereport.dao.IODAO;
import com.crimereport.dao.PoliceAuthDAO;
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
        res.setHeader("Access-Control-Allow-Origin", "*");
        PrintWriter out = res.getWriter();
        String path = req.getPathInfo();

        if (path.equals("/assigned-cases")) {
            int officerId = Integer.parseInt(req.getParameter("officer_id"));
            List<String[]> cases = ioDAO.getAssignedCases(officerId);
            out.print(gson.toJson(cases));

        } else if (path.equals("/closed-cases")) {
            int officerId = Integer.parseInt(req.getParameter("officer_id"));
            List<String[]> cases = ioDAO.getClosedCasesForIO(officerId);
            out.print(gson.toJson(cases));

        } else if (path.equals("/citizen-info")) {
            int caseId = Integer.parseInt(req.getParameter("case_id"));
            String[] info = ioDAO.getCitizenInfoForCase(caseId);
            out.print(gson.toJson(info));

        } else if (path.equals("/complaint-evidence")) {
            int caseId = Integer.parseInt(req.getParameter("case_id"));
            List<String[]> evidence = ioDAO.getComplaintEvidenceForCase(caseId);
            out.print(gson.toJson(evidence));

        } else if (path.equals("/case-evidence")) {
            int caseId = Integer.parseInt(req.getParameter("case_id"));
            List<String[]> evidence = ioDAO.getCaseEvidenceForIO(caseId);
            out.print(gson.toJson(evidence));

        } else if (path.equals("/diary")) {
            int caseId = Integer.parseInt(req.getParameter("case_id"));
            List<String[]> diary = ioDAO.getCaseDiary(caseId);
            out.print(gson.toJson(diary));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("application/json");
        res.setHeader("Access-Control-Allow-Origin", "*");
        PrintWriter out = res.getWriter();
        String path = req.getPathInfo();

        if (path.equals("/update-status")) {
            int caseId = Integer.parseInt(req.getParameter("case_id"));
            String status = req.getParameter("status");
            boolean success = ioDAO.updateCaseStatus(caseId, status);
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            out.print(gson.toJson(response));

        } else if (path.equals("/add-diary")) {
            int caseId = Integer.parseInt(req.getParameter("case_id"));
            int officerId = Integer.parseInt(req.getParameter("officer_id"));
            String note = req.getParameter("note");
            boolean success = ioDAO.addCaseDiaryEntry(caseId, officerId, note);
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            out.print(gson.toJson(response));

        } else if (path.equals("/upload-evidence")) {
            int caseId = Integer.parseInt(req.getParameter("case_id"));
            int officerId = Integer.parseInt(req.getParameter("officer_id"));
            String fileUrl = req.getParameter("file_url");
            boolean success = ioDAO.uploadCaseEvidence(caseId, officerId, fileUrl);
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            out.print(gson.toJson(response));
        } else if (path.equals("/close-case")) {
            int caseId = Integer.parseInt(req.getParameter("case_id"));
            String summary = req.getParameter("summary");
            String result = req.getParameter("final_result");
            boolean success = ioDAO.closeCase(caseId, summary, result);
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