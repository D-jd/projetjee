package com.reservation.servlet;

import com.reservation.dao.*;
import com.reservation.model.*;
import com.reservation.util.JsonUtil;
import com.reservation.util.ReservationUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.List;

/**
 * Routes CLIENT :
 * 
 * GET  /api/client/salles                   → catalogue des salles disponibles
 * GET  /api/client/salles/{id}              → détail d'une salle
 * GET  /api/client/services                 → liste des services supplémentaires
 * POST /api/client/reservations             → créer une réservation
 * GET  /api/client/reservations             → historique des réservations du client
 * DELETE /api/client/reservations/{id}      → annuler une réservation
 */
@WebServlet("/api/client/*")
public class ClientServlet extends HttpServlet {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        if (path == null) path = "/";

        if (path.equals("/salles")) {
            getCatalogue(req, resp);
        } else if (path.matches("/salles/[^/]+")) {
            getSalle(resp, path.split("/")[2]);
        } else if (path.equals("/services")) {
            getServices(resp);
        } else if (path.equals("/reservations")) {
            getMesReservations(req, resp);
        } else {
            JsonUtil.notFound(resp, "Endpoint introuvable");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        if ("/reservations".equals(path)) {
            creerReservation(req, resp);
        } else {
            JsonUtil.notFound(resp, "Endpoint introuvable");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        if (path != null && path.matches("/reservations/[^/]+")) {
            annulerReservation(req, resp, path.split("/")[2]);
        } else {
            JsonUtil.notFound(resp, "Endpoint introuvable");
        }
    }

    // ===================== CATALOGUE =====================

    private void getCatalogue(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        List<Salle> salles = SalleDAO.getInstance().findAll();
        
        // Filtres optionnels
        String typeFilter = req.getParameter("type");
        String capaciteFilter = req.getParameter("capaciteMin");

        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Salle s : salles) {
            if (typeFilter != null && !s.getType().name().equalsIgnoreCase(typeFilter)) continue;
            if (capaciteFilter != null && s.getCapacite() < Integer.parseInt(capaciteFilter)) continue;
            if (!first) sb.append(",");
            first = false;
            sb.append(salleToJson(s));
        }
        sb.append("]");
        JsonUtil.ok(resp, sb.toString());
    }

    private void getSalle(HttpServletResponse resp, String id) throws IOException {
        Salle salle = SalleDAO.getInstance().findById(id);
        if (salle == null) {
            JsonUtil.notFound(resp, "Salle introuvable");
            return;
        }
        JsonUtil.ok(resp, salleToJson(salle));
    }

    private void getServices(HttpServletResponse resp) throws IOException {
        List<ServiceSupplementaire> services = ServiceDAO.getInstance().findAll();
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < services.size(); i++) {
            if (i > 0) sb.append(",");
            ServiceSupplementaire s = services.get(i);
            sb.append(String.format(Locale.US, "{\"id\":\"%s\",\"nom\":\"%s\",\"prix\":%.2f}",
                    s.getId(), s.getNom(), s.getPrix()));
        }
        sb.append("]");
        JsonUtil.ok(resp, sb.toString());
    }

    // ===================== RÉSERVATIONS =====================

    private void creerReservation(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Récupérer le client depuis la session
        HttpSession session = req.getSession(false);
        User user = (User) session.getAttribute("user");
        if (user.getRole() != User.Role.CLIENT) {
            JsonUtil.forbidden(resp, "Seuls les clients peuvent effectuer des réservations");
            return;
        }

        String salleId           = req.getParameter("salleId");
        String dateDebutStr      = req.getParameter("dateDebut");   // format: yyyy-MM-dd HH:mm
        String dureeStr          = req.getParameter("dureeHeures");
        String participantsStr   = req.getParameter("nombreParticipants");
        String[] serviceIds      = req.getParameterValues("serviceIds"); // optionnel

        if (salleId == null || dateDebutStr == null || dureeStr == null || participantsStr == null) {
            JsonUtil.badRequest(resp, "Champs requis: salleId, dateDebut, dureeHeures, nombreParticipants");
            return;
        }

        Salle salle = SalleDAO.getInstance().findById(salleId);
        if (salle == null) {
            JsonUtil.notFound(resp, "Salle introuvable");
            return;
        }

        LocalDateTime dateDebut;
        try {
            dateDebut = LocalDateTime.parse(dateDebutStr, FORMATTER);
        } catch (DateTimeParseException e) {
            JsonUtil.badRequest(resp, "Format de date invalide. Utilisez: yyyy-MM-dd HH:mm");
            return;
        }

        int dureeHeures       = Integer.parseInt(dureeStr);
        int nombreParticipants = Integer.parseInt(participantsStr);

        // Validations métier
        if (!ReservationUtil.estDansFutur(dateDebut)) {
            JsonUtil.badRequest(resp, "La réservation doit être dans le futur");
            return;
        }
        if (dureeHeures > 8) {
            JsonUtil.badRequest(resp, "La durée ne peut pas dépasser 8 heures sans approbation spéciale");
            return;
        }
        if (nombreParticipants > salle.getCapacite()) {
            JsonUtil.badRequest(resp, "Nombre de participants dépasse la capacité de la salle (" + salle.getCapacite() + ")");
            return;
        }
        if (!ReservationUtil.isSalleDisponible(salleId, dateDebut, dureeHeures)) {
            JsonUtil.badRequest(resp, "La salle n'est pas disponible pour ce créneau");
            return;
        }

        // Créer la réservation
        ReservationDAO dao = ReservationDAO.getInstance();
        String id = dao.generateId();
        Reservation reservation = new Reservation(id, user.getId(), salleId,
                dateDebut, dureeHeures, nombreParticipants);

        // Ajouter les services supplémentaires
        double coutTotal = salle.getPrixParHeure() * dureeHeures;
        if (serviceIds != null) {
            for (String sid : serviceIds) {
                ServiceSupplementaire s = ServiceDAO.getInstance().findById(sid);
                if (s != null) {
                    reservation.addService(s);
                    coutTotal += s.getPrix();
                }
            }
        }
        reservation.setCoutTotal(coutTotal);
        dao.save(reservation);

        JsonUtil.created(resp, String.format(Locale.US,
            "{\"message\":\"Réservation créée, en attente de validation\",\"id\":\"%s\",\"coutTotal\":%.2f}",
            id, coutTotal
        ));
    }

    private void getMesReservations(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        User user = (User) session.getAttribute("user");

        List<Reservation> reservations = ReservationDAO.getInstance().findByClient(user.getId());
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < reservations.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(reservationToJson(reservations.get(i)));
        }
        sb.append("]");
        JsonUtil.ok(resp, sb.toString());
    }

    private void annulerReservation(HttpServletRequest req, HttpServletResponse resp, String id) throws IOException {
        HttpSession session = req.getSession(false);
        User user = (User) session.getAttribute("user");

        Reservation reservation = ReservationDAO.getInstance().findById(id);
        if (reservation == null) {
            JsonUtil.notFound(resp, "Réservation introuvable");
            return;
        }
        if (!reservation.getClientId().equals(user.getId())) {
            JsonUtil.forbidden(resp, "Vous ne pouvez annuler que vos propres réservations");
            return;
        }
        if (!ReservationUtil.peutAnnuler(reservation)) {
            JsonUtil.badRequest(resp, "L'annulation doit être effectuée au moins 24h à l'avance");
            return;
        }
        if (reservation.getStatut() == Reservation.StatutReservation.ANNULEE) {
            JsonUtil.badRequest(resp, "Cette réservation est déjà annulée");
            return;
        }

        reservation.setStatut(Reservation.StatutReservation.ANNULEE);
        ReservationDAO.getInstance().save(reservation);
        JsonUtil.ok(resp, "{\"message\":\"Réservation annulée avec succès\"}");
    }

    // ===================== HELPERS JSON =====================

    private String salleToJson(Salle s) {
        StringBuilder eq = new StringBuilder("[");
        for (int i = 0; i < s.getEquipements().size(); i++) {
            if (i > 0) eq.append(",");
            Equipement e = s.getEquipements().get(i);
            if (!e.isEnPanne()) {
                eq.append(String.format("{\"id\":\"%s\",\"nom\":\"%s\"}", e.getId(), e.getNom()));
            }
        }
        eq.append("]");
        return String.format(Locale.US,
            "{\"id\":\"%s\",\"nom\":\"%s\",\"capacite\":%d,\"type\":\"%s\"," +
            "\"prixParHeure\":%.2f,\"localisation\":\"%s\",\"equipements\":%s}",
            s.getId(), s.getNom(), s.getCapacite(), s.getType(),
            s.getPrixParHeure(), s.getLocalisation(), eq.toString()
        );
    }

    private String reservationToJson(Reservation r) {
        return String.format(Locale.US,
            "{\"id\":\"%s\",\"salleId\":\"%s\",\"dateDebut\":\"%s\",\"dureeHeures\":%d," +
            "\"nombreParticipants\":%d,\"statut\":\"%s\",\"coutTotal\":%.2f}",
            r.getId(), r.getSalleId(), r.getDateDebut().format(FORMATTER),
            r.getDureeHeures(), r.getNombreParticipants(), r.getStatut(), r.getCoutTotal()
        );
    }
}
