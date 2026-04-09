package com.crimereport.servlet;

import com.crimereport.dao.CitizenDAO;
import com.crimereport.dao.ComplaintDAO;
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

@WebServlet("/api/citizen/*")
public class CitizenServlet extends HttpServlet {

    private final CitizenDAO citizenDAO = new CitizenDAO();
    private final ComplaintDAO complaintDAO = new ComplaintDAO();
    private final Gson gson = new Gson();

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

        if (path.equals("/register")) {
            String fullName = req.getParameter("full_name");
            String mobile = req.getParameter("mobile_number");
            String email = req.getParameter("email");
            String address = req.getParameter("address");
            String password = req.getParameter("password");
            if (fullName == null || mobile == null || email == null || address == null || password == null
                    || fullName.isBlank() || mobile.isBlank() || email.isBlank() || address.isBlank() || password.isBlank()) {
                ServletUtil.writeError(res, gson, 400, "Missing required fields");
                return;
            }

            boolean success = citizenDAO.registerCitizen(fullName, mobile, email, address, password);
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "Registered successfully" : "Registration failed, mobile may already exist");
            out.print(gson.toJson(response));

        } else if (path.equals("/login")) {
            String mobile = req.getParameter("mobile_number");
            String password = req.getParameter("password");
            if (mobile == null || password == null || mobile.isBlank() || password.isBlank()) {
                ServletUtil.writeError(res, gson, 400, "Missing credentials");
                return;
            }

            int citizenId = citizenDAO.loginCitizen(mobile, password);
            Map<String, Object> response = new HashMap<>();
            if (citizenId != -1) {
                response.put("success", true);
                response.put("citizen_id", citizenId);
                response.put("token", AuthTokenService.createCitizenToken(citizenId));
            } else {
                response.put("success", false);
                response.put("message", "Invalid credentials");
            }
            out.print(gson.toJson(response));

        } else if (path.equals("/file-complaint")) {
            AuthTokenService.SessionData session = AuthTokenService.validate(ServletUtil.bearerToken(req));
            if (session == null || !"citizen".equals(session.type())) {
                ServletUtil.writeError(res, gson, 401, "Unauthorized");
                return;
            }
            Integer citizenId = ServletUtil.parseIntParam(req, "citizen_id");
            if (citizenId == null || citizenId != session.userId()) {
                ServletUtil.writeError(res, gson, 403, "Forbidden");
                return;
            }
            String description = req.getParameter("description");
            String date = req.getParameter("incident_date");
            String time = req.getParameter("incident_time");
            String location = req.getParameter("location");
            if (description == null || date == null || time == null || location == null
                    || description.isBlank() || date.isBlank() || time.isBlank() || location.isBlank()) {
                ServletUtil.writeError(res, gson, 400, "Missing complaint fields");
                return;
            }

            boolean success = complaintDAO.fileComplaint(citizenId, description, date, time, location);
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            if (!success) {
                res.setStatus(400);
                response.put("message", "Failed to file complaint");
            }
            out.print(gson.toJson(response));
        } else {
            ServletUtil.writeError(res, gson, 404, "Endpoint not found");
        }
    }

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
        if (session == null || !"citizen".equals(session.type())) {
            ServletUtil.writeError(res, gson, 401, "Unauthorized");
            return;
        }

        if (path.equals("/complaints")) {
            Integer citizenId = ServletUtil.parseIntParam(req, "citizen_id");
            if (citizenId == null || citizenId != session.userId()) {
                ServletUtil.writeError(res, gson, 403, "Forbidden");
                return;
            }
            List<Complaint> complaints = complaintDAO.getComplaintsByCitizen(citizenId);
            out.print(gson.toJson(complaints));

        } else if (path.equals("/case-details")) {
            Integer complaintId = ServletUtil.parseIntParam(req, "complaint_id");
            if (complaintId == null) {
                ServletUtil.writeError(res, gson, 400, "Invalid complaint_id");
                return;
            }
            if (!complaintDAO.complaintBelongsToCitizen(complaintId, session.userId())) {
                ServletUtil.writeError(res, gson, 403, "Forbidden");
                return;
            }
            String[] details = complaintDAO.getCaseDetailsForCitizen(complaintId);
            out.print(gson.toJson(details));
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
