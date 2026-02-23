package com.reservation.model;

public class ServiceSupplementaire {
    private String id;
    private String nom;
    private double prix;

    public ServiceSupplementaire(String id, String nom, double prix) {
        this.id = id;
        this.nom = nom;
        this.prix = prix;
    }

    public String getId() { return id; }
    public String getNom() { return nom; }
    public double getPrix() { return prix; }
    public void setPrix(double prix) { this.prix = prix; }
    public void setNom(String nom) { this.nom = nom; }
}
