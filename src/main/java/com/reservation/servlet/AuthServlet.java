package com.reservation.servlet;

import com.reservation.dao.UserDAO;
import com.reservation.model.Client;
import com.reservation.model.User;
import com.reservation.util.JsonUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;

/**
 * POST /api/auth/login      → connexion
 * POST /api/auth/logout     → déconnexion
 * POST /api/auth/register   → inscription client
 * PUT  /api/auth/password   → changer mot de passe
 */
@WebServlet("/api/auth/*")
public class AuthServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();

        if ("/login".equals(path)) {
            handleLogin(req, resp);
        } else if ("/logout".equals(path)) {
            handleLogout(req, resp);
        } else if ("/register".equals(path)) {
            handleRegister(req, resp);
        } else {
            JsonUtil.notFound(resp, "Endpoint introuvable");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        if ("/password".equals(path)) {
            handleChangePassword(req, resp);
        } else {
            JsonUtil.notFound(resp, "Endpoint introuvable");
        }
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        if (username == null || password == null) {
            JsonUtil.badRequest(resp, "username et password sont requis");
            return;
        }

        User user = UserDAO.getInstance().findByUsername(username);
        if (user == null || !user.getPassword().equals(password)) {
            JsonUtil.sendJson(resp, 401, "{\"error\": \"Identifiants incorrects\"}");
            return;
        }

        HttpSession session = req.getSession(true);
        session.setAttribute("user", user);

        JsonUtil.ok(resp, String.format(
            "{\"message\": \"Connexion réussie\", \"userId\": \"%s\", \"role\": \"%s\", \"username\": \"%s\"}",
            user.getId(), user.getRole(), user.getUsername()
        ));
    }

    private void handleLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) session.invalidate();
        JsonUtil.ok(resp, "{\"message\": \"Déconnexion réussie\"}");
    }

    private void handleRegister(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String username  = req.getParameter("username");
        String password  = req.getParameter("password");
        String nom       = req.getParameter("nom");
        String prenom    = req.getParameter("prenom");
        String entreprise = req.getParameter("nomEntreprise");
        String email     = req.getParameter("email");
        String telephone = req.getParameter("telephone");

        if (username == null || password == null || nom == null || prenom == null
                || email == null || telephone == null) {
            JsonUtil.badRequest(resp, "Tous les champs sont requis");
            return;
        }

        if (UserDAO.getInstance().usernameExists(username)) {
            JsonUtil.badRequest(resp, "Ce nom d'utilisateur est déjà pris");
            return;
        }

        UserDAO dao = UserDAO.getInstance();
        String id = dao.generateId("client");
        Client client = new Client(id, username, password, nom, prenom,
                entreprise == null ? "" : entreprise, email, telephone);
        dao.save(client);

        JsonUtil.created(resp, String.format(
            "{\"message\": \"Compte créé avec succès\", \"userId\": \"%s\"}", id
        ));
    }

    private void handleChangePassword(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        if (user == null) {
            JsonUtil.sendJson(resp, 401, "{\"error\": \"Non authentifié\"}");
            return;
        }

        String ancienMdp = req.getParameter("ancienMotDePasse");
        String nouveauMdp = req.getParameter("nouveauMotDePasse");

        if (ancienMdp == null || nouveauMdp == null) {
            JsonUtil.badRequest(resp, "ancienMotDePasse et nouveauMotDePasse sont requis");
            return;
        }

        if (!user.getPassword().equals(ancienMdp)) {
            JsonUtil.badRequest(resp, "Ancien mot de passe incorrect");
            return;
        }

        user.setPassword(nouveauMdp);
        UserDAO.getInstance().save(user);
        JsonUtil.ok(resp, "{\"message\": \"Mot de passe modifié avec succès\"}");
    }
}
