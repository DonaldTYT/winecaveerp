package com.kikyosoft.servlet.web;

//TelemetryServlet.java
import java.io.IOException;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet(name="TelemetryServlet", urlPatterns={"/telemetry"})
public class TelemetryServlet extends HttpServlet {
 @Override
 protected void doPost(HttpServletRequest req, HttpServletResponse resp)
         throws IOException {
     resp.setStatus(HttpServletResponse.SC_NO_CONTENT); // 204

     String body = new String(req.getInputStream().readAllBytes(),
                              java.nio.charset.StandardCharsets.UTF_8);

     // Log, persist, or enqueue for later analysis
     System.out.println("Telemetry received: " + body);

     // Don’t do heavy work here—enqueue if you want analytics.
 }
}
