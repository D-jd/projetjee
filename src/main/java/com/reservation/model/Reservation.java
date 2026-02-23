package com.reservation.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

public class Reservation {
    private String id;
    private String clientId;
    private String salleId;
    private LocalDateTime dateDebut;
    private int dureeHeures;
    private int nombreParticipants;
    private List<ServiceSupplementaire> services;
    private StatutReservation statut;
    private double coutTotal;

    public enum StatutReservation {
        EN_ATTENTE, VALIDEE, REJETEE, ANNULEE
    }

    public Reservation(String id, String clientId, String salleId,
                       LocalDateTime dateDebut, int dureeHeures, int nombreParticipants) {
        this.id = id;
        this.clientId = clientId;
        this.salleId = salleId;
        this.dateDebut = dateDebut;
        this.dureeHeures = dureeHeures;
        this.nombreParticipants = nombreParticipants;
        this.services = new ArrayList<>();
        this.statut = StatutReservation.EN_ATTENTE;
    }

    public LocalDateTime getDateFin() {
        return dateDebut.plusHours(dureeHeures);
    }

    public String getId() { return id; }
    public String getClientId() { return clientId; }
    public String getSalleId() { return salleId; }
    public LocalDateTime getDateDebut() { return dateDebut; }
    public int getDureeHeures() { return dureeHeures; }
    public int getNombreParticipants() { return nombreParticipants; }
    public List<ServiceSupplementaire> getServices() { return services; }
    public StatutReservation getStatut() { return statut; }
    public double getCoutTotal() { return coutTotal; }

    public void setStatut(StatutReservation statut) { this.statut = statut; }
    public void setCoutTotal(double coutTotal) { this.coutTotal = coutTotal; }
    public void addService(ServiceSupplementaire s) { this.services.add(s); }
}
