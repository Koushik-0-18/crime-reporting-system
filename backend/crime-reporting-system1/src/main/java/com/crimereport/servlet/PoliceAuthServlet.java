package com.crimereport.servlet;

import com.crimereport.dao.PoliceAuthDAO;
import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/police/*")
public class PoliceAuthServlet extends HttpServlet {

    private final PoliceAuthDAO policeAuthDAO = new PoliceAuthDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("application/json");
        res.setHeader("Access-Control-Allow-Origin", "*");
        PrintWriter out = res.getWriter();
        String path = req.getPathInfo();

        if (path.equals("/login")) {
            String badgeId = req.getParameter("badge_id");
            String password = req.getParameter("password");

            String[] officer = policeAuthDAO.loginPolice(badgeId, password);
            Map<String, Object> response = new HashMap<>();
            if (officer != null) {
                response.put("success", true);
                response.put("police_id", officer[0]);
                response.put("name", officer[1]);
                response.put("role", officer[2]);
            } else {
                response.put("success", false);
                response.put("message", "Invalid credentials");
            }
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