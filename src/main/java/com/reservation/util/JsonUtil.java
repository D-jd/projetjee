package com.reservation.util;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JsonUtil {

    public static void sendJson(HttpServletResponse response, int status, String json) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json);
    }

    public static void ok(HttpServletResponse response, String json) throws IOException {
        sendJson(response, 200, json);
    }

    public static void created(HttpServletResponse response, String json) throws IOException {
        sendJson(response, 201, json);
    }

    public static void badRequest(HttpServletResponse response, String message) throws IOException {
        sendJson(response, 400, "{\"error\": \"" + escape(message) + "\"}");
    }

    public static void notFound(HttpServletResponse response, String message) throws IOException {
        sendJson(response, 404, "{\"error\": \"" + escape(message) + "\"}");
    }

    public static void forbidden(HttpServletResponse response, String message) throws IOException {
        sendJson(response, 403, "{\"error\": \"" + escape(message) + "\"}");
    }

    public static void serverError(HttpServletResponse response, String message) throws IOException {
        sendJson(response, 500, "{\"error\": \"" + escape(message) + "\"}");
    }

    private static String escape(String s) {
        return s == null ? "" : s.replace("\"", "\\\"");
    }
}
