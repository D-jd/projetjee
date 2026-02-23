package com.reservation.servlet;

import com.reservation.dao.*;
import com.reservation.model.*;
import com.reservation.util.JsonUtil;
import com.reservation.util.ReservationUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Routes GESTIONNAIRE :
 * 
 * GET  /api/gestionnaire/reservations              → planning des réservations de ses salles
 * PUT  /api/gestionnaire/reservations/{id}/valider → valider une réservation
 * PUT  /api/gestionnaire/reservations/{id}/rejeter → rejeter une réservation
 * DELETE /api/gestionnaire/reservations/{id}       → annuler une réservation
 * GET  /api/gestionnaire/salles                    → ses salles
 * PUT  /api/gestionnaire/equipements/{id}/panne    → signaler panne équipement
 * PUT  /api/gestionnaire/equipements/{id}/repare   → signaler réparation
 */
@WebServlet("/api/gestionnaire/*")
public class GestionnaireServlet extends HttpServlet {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        if (path == null) path = "/";

        if (path.equals("/reservations")) {
            getReservations(req, resp);
        } else if (path.equals("/salles")) {
            getMesSalles(req, resp);
        } else {
            JsonUtil.notFound(resp, "Endpoint introuvable");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        if (path == null) path = "/";

        if (path.matches("/reservations/[^/]+/valider")) {
            String id = path.split("/")[2];
            validerReservation(req, resp, id);
        } else if (path.matches("/reservations/[^/]+/rejeter")) {
            String id = path.split("/")[2];
            rejeterReservation(req, resp, id);
        } else if (path.matches("/equipements/[^/]+/panne")) {
            String eqId = path.split("/")[2];
            signalerPanne(req, resp, eqId, true);
        } else if (path.matches("/equipements/[^/]+/repare")) {
            String eqId = path.split("/")[2];
            signalerPanne(req, resp, eqId, false);
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

    // ===================== PLANNING =====================

    private void getReservations(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        User user = (User) session.getAttribute("user");

        // Récupérer les salles dont il a la charge
        List<Salle> mesSalles = SalleDAO.getInstance().findByGestionnaire(user.getId());

        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Salle salle : mesSalles) {
            List<Reservation> reservations = ReservationDAO.getInstance().findBySalle(salle.getId());
            for (Reservation r : reservations) {
                if (!first) sb.append(",");
                first = false;
                sb.append(reservationToJson(r, salle.getNom()));
            }
        }
        sb.append("]");
        JsonUtil.ok(resp, sb.toString());
    }

    private void getMesSalles(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        User user = (User) session.getAttribute("user");
        List<Salle> salles = SalleDAO.getInstance().findByGestionnaire(user.getId());

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < salles.size(); i++) {
            if (i > 0) sb.append(",");
            Salle s = salles.get(i);
            sb.append(String.format(
                "{\"id\":\"%s\",\"nom\":\"%s\",\"capacite\":%d,\"type\":\"%s\",\"localisation\":\"%s\"}",
                s.getId(), s.getNom(), s.getCapacite(), s.getType(), s.getLocalisation()
            ));
        }
        sb.append("]");
        JsonUtil.ok(resp, sb.toString());
    }

    // ===================== VALIDATION =====================

    private void validerReservation(HttpServletRequest req, HttpServletResponse resp, String id) throws IOException {
        Reservation reservation = getReservationIfAuthorized(req, resp, id);
        if (reservation == null) return;

        if (reservation.getStatut() != Reservation.StatutReservation.EN_ATTENTE) {
            JsonUtil.badRequest(resp, "Seules les réservations EN_ATTENTE peuvent être validées");
            return;
        }
        reservation.setStatut(Reservation.StatutReservation.VALIDEE);
        ReservationDAO.getInstance().save(reservation);
        JsonUtil.ok(resp, "{\"message\":\"Réservation validée avec succès\"}");
    }

    private void rejeterReservation(HttpServletRequest req, HttpServletResponse resp, String id) throws IOException {
        Reservation reservation = getReservationIfAuthorized(req, resp, id);
        if (reservation == null) return;

        if (reservation.getStatut() != Reservation.StatutReservation.EN_ATTENTE) {
            JsonUtil.badRequest(resp, "Seules les réservations EN_ATTENTE peuvent être rejetées");
            return;
        }
        reservation.setStatut(Reservation.StatutReservation.REJETEE);
        ReservationDAO.getInstance().save(reservation);
        JsonUtil.ok(resp, "{\"message\":\"Réservation rejetée\"}");
    }

    private void annulerReservation(HttpServletRequest req, HttpServletResponse resp, String id) throws IOException {
        Reservation reservation = getReservationIfAuthorized(req, resp, id);
        if (reservation == null) return;

        if (!ReservationUtil.peutAnnuler(reservation)) {
            JsonUtil.badRequest(resp, "Annulation impossible : moins de 24h avant le début");
            return;
        }
        reservation.setStatut(Reservation.StatutReservation.ANNULEE);
        ReservationDAO.getInstance().save(reservation);
        JsonUtil.ok(resp, "{\"message\":\"Réservation annulée\"}");
    }

    // ===================== ÉQUIPEMENTS =====================

    private void signalerPanne(HttpServletRequest req, HttpServletResponse resp, String eqId, boolean enPanne) throws IOException {
        HttpSession session = req.getSession(false);
        User user = (User) session.getAttribute("user");

        // Trouver l'équipement dans les salles du gestionnaire
        List<Salle> mesSalles = SalleDAO.getInstance().findByGestionnaire(user.getId());
        for (Salle s : mesSalles) {
            for (Equipement e : s.getEquipements()) {
                if (e.getId().equals(eqId)) {
                    e.setEnPanne(enPanne);
                    String msg = enPanne ? "Équipement signalé en panne" : "Équipement signalé comme réparé";
                    JsonUtil.ok(resp, "{\"message\":\"" + msg + "\"}");
                    return;
                }
            }
        }
        JsonUtil.notFound(resp, "Équipement introuvable dans vos salles");
    }

    // ===================== HELPERS =====================

    /**
     * Vérifie que la réservation existe et concerne une salle du gestionnaire connecté.
     */
    private Reservation getReservationIfAuthorized(HttpServletRequest req, HttpServletResponse resp, String id)
            throws IOException {
        HttpSession session = req.getSession(false);
        User user = (User) session.getAttribute("user");

        Reservation reservation = ReservationDAO.getInstance().findById(id);
        if (reservation == null) {
            JsonUtil.notFound(resp, "Réservation introuvable");
            return null;
        }

        // Admin a accès à tout
        if (user.getRole() == User.Role.ADMIN) return reservation;

        // Vérifier que la salle appartient au gestionnaire
        Salle salle = SalleDAO.getInstance().findById(reservation.getSalleId());
        if (salle == null || !user.getId().equals(salle.getGestionnaireId())) {
            JsonUtil.forbidden(resp, "Vous n'êtes pas responsable de cette salle");
            return null;
        }
        return reservation;
    }

    private String reservationToJson(Reservation r, String nomSalle) {
        return String.format(
            "{\"id\":\"%s\",\"clientId\":\"%s\",\"salleId\":\"%s\",\"nomSalle\":\"%s\"," +
            "\"dateDebut\":\"%s\",\"dateFin\":\"%s\",\"dureeHeures\":%d," +
            "\"nombreParticipants\":%d,\"statut\":\"%s\",\"coutTotal\":%.2f}",
            r.getId(), r.getClientId(), r.getSalleId(), nomSalle,
            r.getDateDebut().format(FORMATTER), r.getDateFin().format(FORMATTER),
            r.getDureeHeures(), r.getNombreParticipants(), r.getStatut(), r.getCoutTotal()
        );
    }
}
