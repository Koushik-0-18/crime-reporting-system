package com.crimereport.servlet;

import com.crimereport.dao.PoliceAuthDAO;
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
import java.util.Map;

@WebServlet("/api/police/*")
public class PoliceAuthServlet extends HttpServlet {

    private final PoliceAuthDAO policeAuthDAO = new PoliceAuthDAO();
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

        if (path.equals("/login")) {
            String badgeId = req.getParameter("badge_id");
            String password = req.getParameter("password");
            if (badgeId == null || password == null || badgeId.isBlank() || password.isBlank()) {
                ServletUtil.writeError(res, gson, 400, "Missing credentials");
                return;
            }

            String[] officer = policeAuthDAO.loginPolice(badgeId, password);
            Map<String, Object> response = new HashMap<>();
            if (officer != null) {
                int policeId = Integer.parseInt(officer[0]);
                response.put("success", true);
                response.put("police_id", officer[0]);
                response.put("name", officer[1]);
                response.put("role", officer[2]);
                response.put("token", AuthTokenService.createPoliceToken(policeId, officer[2]));
            } else {
                response.put("success", false);
                response.put("message", "Invalid credentials");
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
