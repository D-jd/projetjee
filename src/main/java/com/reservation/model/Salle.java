package com.reservation.model;

import java.util.List;
import java.util.ArrayList;

public class Salle {
    private String id;
    private String nom;
    private int capacite;
    private TypeSalle type;
    private double prixParHeure;
    private List<Equipement> equipements;
    private String localisation; // ex: "Etage 2, Bâtiment A"
    private String gestionnaireId; // ID du gestionnaire affecté

    public enum TypeSalle {
        REUNION, CONFERENCE, FORMATION, AUTRE
    }

    public Salle(String id, String nom, int capacite, TypeSalle type,
                 double prixParHeure, String localisation) {
        this.id = id;
        this.nom = nom;
        this.capacite = capacite;
        this.type = type;
        this.prixParHeure = prixParHeure;
        this.localisation = localisation;
        this.equipements = new ArrayList<>();
    }

    public String getId() { return id; }
    public String getNom() { return nom; }
    public int getCapacite() { return capacite; }
    public TypeSalle getType() { return type; }
    public double getPrixParHeure() { return prixParHeure; }
    public List<Equipement> getEquipements() { return equipements; }
    public String getLocalisation() { return localisation; }
    public String getGestionnaireId() { return gestionnaireId; }

    public void setNom(String nom) { this.nom = nom; }
    public void setCapacite(int capacite) { this.capacite = capacite; }
    public void setType(TypeSalle type) { this.type = type; }
    public void setPrixParHeure(double prixParHeure) { this.prixParHeure = prixParHeure; }
    public void setLocalisation(String localisation) { this.localisation = localisation; }
    public void setGestionnaireId(String gestionnaireId) { this.gestionnaireId = gestionnaireId; }
    public void addEquipement(Equipement e) { this.equipements.add(e); }
    public void removeEquipement(String equipementId) {
        this.equipements.removeIf(e -> e.getId().equals(equipementId));
    }
}
