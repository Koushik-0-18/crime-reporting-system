package com.crimereport.servlet;

import com.crimereport.dao.CitizenDAO;
import com.crimereport.dao.ComplaintDAO;
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

@WebServlet("/api/citizen/*")
public class CitizenServlet extends HttpServlet {

    private final CitizenDAO citizenDAO = new CitizenDAO();
    private final ComplaintDAO complaintDAO = new ComplaintDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("application/json");
        res.setHeader("Access-Control-Allow-Origin", "*");
        PrintWriter out = res.getWriter();
        String path = req.getPathInfo();

        if (path.equals("/register")) {
            String fullName = req.getParameter("full_name");
            String mobile = req.getParameter("mobile_number");
            String email = req.getParameter("email");
            String address = req.getParameter("address");
            String password = req.getParameter("password");

            boolean success = citizenDAO.registerCitizen(fullName, mobile, email, address, password);
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "Registered successfully" : "Registration failed, mobile may already exist");
            out.print(gson.toJson(response));

        } else if (path.equals("/login")) {
            String mobile = req.getParameter("mobile_number");
            String password = req.getParameter("password");

            int citizenId = citizenDAO.loginCitizen(mobile, password);
            Map<String, Object> response = new HashMap<>();
            if (citizenId != -1) {
                response.put("success", true);
                response.put("citizen_id", citizenId);
            } else {
                response.put("success", false);
                response.put("message", "Invalid credentials");
            }
            out.print(gson.toJson(response));

        } else if (path.equals("/file-complaint")) {
            int citizenId = Integer.parseInt(req.getParameter("citizen_id"));
            String description = req.getParameter("description");
            String date = req.getParameter("incident_date");
            String time = req.getParameter("incident_time");
            String location = req.getParameter("location");

            boolean success = complaintDAO.fileComplaint(citizenId, description, date, time, location);
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            out.print(gson.toJson(response));
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("application/json");
        res.setHeader("Access-Control-Allow-Origin", "*");
        PrintWriter out = res.getWriter();
        String path = req.getPathInfo();

        if (path.equals("/complaints")) {
            int citizenId = Integer.parseInt(req.getParameter("citizen_id"));
            List<Complaint> complaints = complaintDAO.getComplaintsByCitizen(citizenId);
            out.print(gson.toJson(complaints));

        } else if (path.equals("/case-details")) {
            int complaintId = Integer.parseInt(req.getParameter("complaint_id"));
            String[] details = complaintDAO.getCaseDetailsForCitizen(complaintId);
            out.print(gson.toJson(details));
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