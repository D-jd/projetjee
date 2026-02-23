package com.reservation.filter;

import com.reservation.model.User;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebFilter("/*")
public class AuthFilter implements Filter {

    // Chemins publics (pas besoin d'être connecté)
    private static final String[] PUBLIC_PATHS = {
        "/login", "/register", "/api/auth/login", "/api/auth/register"
    };

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String path = request.getServletPath();

        // Laisser passer les chemins publics
        for (String p : PUBLIC_PATHS) {
            if (path.startsWith(p)) {
                chain.doFilter(req, res);
                return;
            }
        }

        // Vérifier la session
        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Non authentifié. Veuillez vous connecter.\"}");
            return;
        }

        // Contrôle d'accès par rôle selon le chemin
        String role = user.getRole().name();

        if (path.startsWith("/api/admin") && !role.equals("ADMIN")) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Accès refusé. Rôle ADMIN requis.\"}");
            return;
        }

        if (path.startsWith("/api/gestionnaire") && !role.equals("GESTIONNAIRE") && !role.equals("ADMIN")) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Accès refusé. Rôle GESTIONNAIRE requis.\"}");
            return;
        }

        chain.doFilter(req, res);
    }

    @Override public void init(FilterConfig filterConfig) {}
    @Override public void destroy() {}
}
