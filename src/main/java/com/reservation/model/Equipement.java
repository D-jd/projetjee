package com.reservation.model;

public class Equipement {
    private String id;
    private String nom;
    private boolean enPanne;

    public Equipement(String id, String nom) {
        this.id = id;
        this.nom = nom;
        this.enPanne = false;
    }

    public String getId() { return id; }
    public String getNom() { return nom; }
    public boolean isEnPanne() { return enPanne; }
    public void setEnPanne(boolean enPanne) { this.enPanne = enPanne; }
    public void setNom(String nom) { this.nom = nom; }
}
