package com.reservation.util;

import com.reservation.dao.ReservationDAO;
import com.reservation.model.Reservation;
import com.reservation.model.Salle;
import com.reservation.model.ServiceSupplementaire;
import java.time.LocalDateTime;
import java.util.List;

public class ReservationUtil {

    /**
     * Vérifie si une salle est disponible pour le créneau demandé.
     */
    public static boolean isSalleDisponible(String salleId, LocalDateTime debut, int dureeHeures) {
        LocalDateTime fin = debut.plusHours(dureeHeures);
        List<Reservation> reservations = ReservationDAO.getInstance().findBySalle(salleId);

        for (Reservation r : reservations) {
            // Ignorer les réservations annulées/rejetées
            if (r.getStatut() == Reservation.StatutReservation.ANNULEE ||
                r.getStatut() == Reservation.StatutReservation.REJETEE) {
                continue;
            }
            // Vérifier chevauchement
            if (debut.isBefore(r.getDateFin()) && fin.isAfter(r.getDateDebut())) {
                return false; // Conflit !
            }
        }
        return true;
    }

    /**
     * Calcule le coût total d'une réservation.
     */
    public static double calculerCout(Salle salle, int dureeHeures, List<ServiceSupplementaire> services) {
        double cout = salle.getPrixParHeure() * dureeHeures;
        for (ServiceSupplementaire s : services) {
            cout += s.getPrix();
        }
        return cout;
    }

    /**
     * Vérifie si une annulation est possible (au moins 24h avant).
     */
    public static boolean peutAnnuler(Reservation reservation) {
        return LocalDateTime.now().plusHours(24).isBefore(reservation.getDateDebut());
    }

    /**
     * Vérifie que la réservation est dans le futur.
     */
    public static boolean estDansFutur(LocalDateTime dateDebut) {
        return dateDebut.isAfter(LocalDateTime.now());
    }
}
