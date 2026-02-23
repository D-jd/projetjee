package com.reservation.servlet;

import com.reservation.dao.*;
import com.reservation.model.*;
import com.reservation.util.JsonUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Routes ADMIN :
 * 
 * === GESTIONNAIRES ===
 * GET    /api/admin/gestionnaires              → liste des gestionnaires
 * POST   /api/admin/gestionnaires              → ajouter un gestionnaire
 * PUT    /api/admin/gestionnaires/{id}         → modifier un gestionnaire
 * DELETE /api/admin/gestionnaires/{id}         → supprimer un gestionnaire
 * 
 * === SALLES ===
 * GET    /api/admin/salles                     → liste des salles
 * POST   /api/admin/salles                     → ajouter une salle
 * PUT    /api/admin/salles/{id}                → modifier une salle
 * DELETE /api/admin/salles/{id}                → supprimer une salle
 * POST   /api/admin/salles/{id}/equipements    → ajouter équipement
 * DELETE /api/admin/salles/{id}/equipements/{eqId} → supprimer équipement
 * PUT    /api/admin/salles/{id}/gestionnaire   → affecter gestionnaire
 * 
 * === RAPPORTS ===
 * GET    /api/admin/rapports/occupation        → taux d'occupation
 * GET    /api/admin/rapports/revenus           → revenus générés
 * GET    /api/admin/rapports/clients-actifs    → clients les plus actifs
 */
@WebServlet("/api/admin/*")
public class AdminServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        if (path == null) path = "/";

        if (path.startsWith("/gestionnaires")) {
            getGestionnaires(resp);
        } else if (path.startsWith("/salles")) {
            getSalles(resp);
        } else if (path.equals("/rapports/occupation")) {
            getRapportOccupation(resp);
        } else if (path.equals("/rapports/revenus")) {
            getRapportRevenus(resp);
        } else if (path.equals("/rapports/clients-actifs")) {
            getRapportClientsActifs(resp);
        } else {
            JsonUtil.notFound(resp, "Endpoint introuvable");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        if (path == null) path = "/";

        if (path.equals("/gestionnaires")) {
            addGestionnaire(req, resp);
        } else if (path.equals("/salles")) {
            addSalle(req, resp);
        } else if (path.matches("/salles/[^/]+/equipements")) {
            String salleId = path.split("/")[2];
            addEquipement(req, resp, salleId);
        } else {
            JsonUtil.notFound(resp, "Endpoint introuvable");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        if (path == null) path = "/";

        if (path.matches("/gestionnaires/[^/]+")) {
            String id = path.split("/")[2];
            updateGestionnaire(req, resp, id);
        } else if (path.matches("/salles/[^/]+/gestionnaire")) {
            String salleId = path.split("/")[2];
            affecterGestionnaire(req, resp, salleId);
        } else if (path.matches("/salles/[^/]+")) {
            String id = path.split("/")[2];
            updateSalle(req, resp, id);
        } else {
            JsonUtil.notFound(resp, "Endpoint introuvable");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        if (path == null) path = "/";

        if (path.matches("/gestionnaires/[^/]+")) {
            String id = path.split("/")[2];
            deleteGestionnaire(resp, id);
        } else if (path.matches("/salles/[^/]+/equipements/[^/]+")) {
            String[] parts = path.split("/");
            deleteEquipement(resp, parts[2], parts[4]);
        } else if (path.matches("/salles/[^/]+")) {
            String id = path.split("/")[2];
            deleteSalle(resp, id);
        } else {
            JsonUtil.notFound(resp, "Endpoint introuvable");
        }
    }

    // ===================== GESTIONNAIRES =====================

    private void getGestionnaires(HttpServletResponse resp) throws IOException {
        List<User> gestionnaires = UserDAO.getInstance().findByRole(User.Role.GESTIONNAIRE);
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < gestionnaires.size(); i++) {
            User g = gestionnaires.get(i);
            if (i > 0) sb.append(",");
            sb.append(String.format("{\"id\":\"%s\",\"username\":\"%s\",\"role\":\"%s\"}",
                    g.getId(), g.getUsername(), g.getRole()));
        }
        sb.append("]");
        JsonUtil.ok(resp, sb.toString());
    }

    private void addGestionnaire(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        if (username == null || password == null) {
            JsonUtil.badRequest(resp, "username et password sont requis");
            return;
        }
        if (UserDAO.getInstance().usernameExists(username)) {
            JsonUtil.badRequest(resp, "Ce nom d'utilisateur est déjà pris");
            return;
        }

        UserDAO dao = UserDAO.getInstance();
        String id = dao.generateId("gest");
        User g = new User(id, username, password, User.Role.GESTIONNAIRE);
        dao.save(g);
        JsonUtil.created(resp, String.format("{\"message\":\"Gestionnaire créé\",\"id\":\"%s\"}", id));
    }

    private void updateGestionnaire(HttpServletRequest req, HttpServletResponse resp, String id) throws IOException {
        User g = UserDAO.getInstance().findById(id);
        if (g == null || g.getRole() != User.Role.GESTIONNAIRE) {
            JsonUtil.notFound(resp, "Gestionnaire introuvable");
            return;
        }
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        if (username != null) g.setUsername(username);
        if (password != null) g.setPassword(password);
        UserDAO.getInstance().save(g);
        JsonUtil.ok(resp, "{\"message\":\"Gestionnaire mis à jour\"}");
    }

    private void deleteGestionnaire(HttpServletResponse resp, String id) throws IOException {
        User g = UserDAO.getInstance().findById(id);
        if (g == null || g.getRole() != User.Role.GESTIONNAIRE) {
            JsonUtil.notFound(resp, "Gestionnaire introuvable");
            return;
        }
        UserDAO.getInstance().delete(id);
        JsonUtil.ok(resp, "{\"message\":\"Gestionnaire supprimé\"}");
    }

    // ===================== SALLES =====================

    private void getSalles(HttpServletResponse resp) throws IOException {
        List<Salle> salles = SalleDAO.getInstance().findAll();
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < salles.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(salleToJson(salles.get(i)));
        }
        sb.append("]");
        JsonUtil.ok(resp, sb.toString());
    }

    private void addSalle(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String nom = req.getParameter("nom");
        String capaciteStr = req.getParameter("capacite");
        String typeStr = req.getParameter("type");
        String prixStr = req.getParameter("prixParHeure");
        String localisation = req.getParameter("localisation");

        if (nom == null || capaciteStr == null || typeStr == null || prixStr == null || localisation == null) {
            JsonUtil.badRequest(resp, "Tous les champs sont requis: nom, capacite, type, prixParHeure, localisation");
            return;
        }

        try {
            int capacite = Integer.parseInt(capaciteStr);
            double prix = Double.parseDouble(prixStr);
            Salle.TypeSalle type = Salle.TypeSalle.valueOf(typeStr.toUpperCase());
            SalleDAO dao = SalleDAO.getInstance();
            String id = dao.generateId();
            Salle salle = new Salle(id, nom, capacite, type, prix, localisation);
            dao.save(salle);
            JsonUtil.created(resp, String.format("{\"message\":\"Salle créée\",\"id\":\"%s\"}", id));
        } catch (IllegalArgumentException e) {
            JsonUtil.badRequest(resp, "Type invalide. Valeurs: REUNION, CONFERENCE, FORMATION, AUTRE");
        }
    }

    private void updateSalle(HttpServletRequest req, HttpServletResponse resp, String id) throws IOException {
        Salle salle = SalleDAO.getInstance().findById(id);
        if (salle == null) {
            JsonUtil.notFound(resp, "Salle introuvable");
            return;
        }
        if (req.getParameter("nom") != null) salle.setNom(req.getParameter("nom"));
        if (req.getParameter("capacite") != null) salle.setCapacite(Integer.parseInt(req.getParameter("capacite")));
        if (req.getParameter("prixParHeure") != null) salle.setPrixParHeure(Double.parseDouble(req.getParameter("prixParHeure")));
        if (req.getParameter("localisation") != null) salle.setLocalisation(req.getParameter("localisation"));
        if (req.getParameter("type") != null) salle.setType(Salle.TypeSalle.valueOf(req.getParameter("type").toUpperCase()));
        SalleDAO.getInstance().save(salle);
        JsonUtil.ok(resp, "{\"message\":\"Salle mise à jour\"}");
    }

    private void deleteSalle(HttpServletResponse resp, String id) throws IOException {
        if (!SalleDAO.getInstance().delete(id)) {
            JsonUtil.notFound(resp, "Salle introuvable");
            return;
        }
        JsonUtil.ok(resp, "{\"message\":\"Salle supprimée\"}");
    }

    private void addEquipement(HttpServletRequest req, HttpServletResponse resp, String salleId) throws IOException {
        Salle salle = SalleDAO.getInstance().findById(salleId);
        if (salle == null) {
            JsonUtil.notFound(resp, "Salle introuvable");
            return;
        }
        String nomEq = req.getParameter("nom");
        if (nomEq == null) {
            JsonUtil.badRequest(resp, "Le nom de l'équipement est requis");
            return;
        }
        String eqId = "eq-" + System.currentTimeMillis();
        salle.addEquipement(new Equipement(eqId, nomEq));
        JsonUtil.created(resp, String.format("{\"message\":\"Équipement ajouté\",\"id\":\"%s\"}", eqId));
    }

    private void deleteEquipement(HttpServletResponse resp, String salleId, String eqId) throws IOException {
        Salle salle = SalleDAO.getInstance().findById(salleId);
        if (salle == null) {
            JsonUtil.notFound(resp, "Salle introuvable");
            return;
        }
        salle.removeEquipement(eqId);
        JsonUtil.ok(resp, "{\"message\":\"Équipement supprimé\"}");
    }

    private void affecterGestionnaire(HttpServletRequest req, HttpServletResponse resp, String salleId) throws IOException {
        Salle salle = SalleDAO.getInstance().findById(salleId);
        if (salle == null) {
            JsonUtil.notFound(resp, "Salle introuvable");
            return;
        }
        String gestionnaireId = req.getParameter("gestionnaireId");
        if (gestionnaireId == null) {
            JsonUtil.badRequest(resp, "gestionnaireId est requis");
            return;
        }
        User g = UserDAO.getInstance().findById(gestionnaireId);
        if (g == null || g.getRole() != User.Role.GESTIONNAIRE) {
            JsonUtil.notFound(resp, "Gestionnaire introuvable");
            return;
        }
        salle.setGestionnaireId(gestionnaireId);
        JsonUtil.ok(resp, "{\"message\":\"Gestionnaire affecté à la salle\"}");
    }

    // ===================== RAPPORTS =====================

    private void getRapportOccupation(HttpServletResponse resp) throws IOException {
        List<Salle> salles = SalleDAO.getInstance().findAll();
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < salles.size(); i++) {
            if (i > 0) sb.append(",");
            Salle s = salles.get(i);
            long nbReservations = ReservationDAO.getInstance().findBySalle(s.getId()).stream()
                    .filter(r -> r.getStatut() == Reservation.StatutReservation.VALIDEE)
                    .count();
            long totalHeures = ReservationDAO.getInstance().findBySalle(s.getId()).stream()
                    .filter(r -> r.getStatut() == Reservation.StatutReservation.VALIDEE)
                    .mapToLong(Reservation::getDureeHeures).sum();
            sb.append(String.format(
                "{\"salleId\":\"%s\",\"salle\":\"%s\",\"nbReservations\":%d,\"totalHeures\":%d}",
                s.getId(), s.getNom(), nbReservations, totalHeures
            ));
        }
        sb.append("]");
        JsonUtil.ok(resp, sb.toString());
    }

    private void getRapportRevenus(HttpServletResponse resp) throws IOException {
        double totalRevenus = ReservationDAO.getInstance().findAll().stream()
                .filter(r -> r.getStatut() == Reservation.StatutReservation.VALIDEE)
                .mapToDouble(Reservation::getCoutTotal).sum();
        JsonUtil.ok(resp, String.format("{\"totalRevenus\":%.2f}", totalRevenus));
    }

    private void getRapportClientsActifs(HttpServletResponse resp) throws IOException {
        List<User> clients = UserDAO.getInstance().findByRole(User.Role.CLIENT);
        StringBuilder sb = new StringBuilder("[");
        clients.sort((a, b) -> {
            long ra = ReservationDAO.getInstance().findByClient(a.getId()).size();
            long rb = ReservationDAO.getInstance().findByClient(b.getId()).size();
            return Long.compare(rb, ra);
        });
        for (int i = 0; i < clients.size(); i++) {
            if (i > 0) sb.append(",");
            User c = clients.get(i);
            long nb = ReservationDAO.getInstance().findByClient(c.getId()).size();
            sb.append(String.format("{\"clientId\":\"%s\",\"username\":\"%s\",\"nbReservations\":%d}",
                    c.getId(), c.getUsername(), nb));
        }
        sb.append("]");
        JsonUtil.ok(resp, sb.toString());
    }

    private String salleToJson(Salle s) {
        StringBuilder eq = new StringBuilder("[");
        for (int i = 0; i < s.getEquipements().size(); i++) {
            if (i > 0) eq.append(",");
            Equipement e = s.getEquipements().get(i);
            eq.append(String.format("{\"id\":\"%s\",\"nom\":\"%s\",\"enPanne\":%b}",
                    e.getId(), e.getNom(), e.isEnPanne()));
        }
        eq.append("]");
        return String.format(
            "{\"id\":\"%s\",\"nom\":\"%s\",\"capacite\":%d,\"type\":\"%s\",\"prixParHeure\":%.2f," +
            "\"localisation\":\"%s\",\"gestionnaireId\":\"%s\",\"equipements\":%s}",
            s.getId(), s.getNom(), s.getCapacite(), s.getType(), s.getPrixParHeure(),
            s.getLocalisation(), s.getGestionnaireId() != null ? s.getGestionnaireId() : "",
            eq.toString()
        );
    }
}
