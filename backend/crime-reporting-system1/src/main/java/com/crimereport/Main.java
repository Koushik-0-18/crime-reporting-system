package com.crimereport;

import com.crimereport.servlet.*;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import java.io.File;

public class Main {
    public static void main(String[] args) throws Exception {
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(8080);
        tomcat.getConnector();

        Context ctx = tomcat.addContext("", new File(".").getAbsolutePath());

        Tomcat.addServlet(ctx, "CitizenServlet", new CitizenServlet());
        ctx.addServletMappingDecoded("/api/citizen/*", "CitizenServlet");

        Tomcat.addServlet(ctx, "ChiefServlet", new ChiefServlet());
        ctx.addServletMappingDecoded("/api/chief/*", "ChiefServlet");

        Tomcat.addServlet(ctx, "IOServlet", new IOServlet());
        ctx.addServletMappingDecoded("/api/io/*", "IOServlet");

        Tomcat.addServlet(ctx, "PoliceAuthServlet", new PoliceAuthServlet());
        ctx.addServletMappingDecoded("/api/police/*", "PoliceAuthServlet");

        tomcat.start();
        System.out.println("Server started on http://localhost:8080");
        tomcat.getServer().await();
    }
}