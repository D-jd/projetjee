package com.reservation.model;

public class Client extends User {
    private String nom;
    private String prenom;
    private String nomEntreprise;
    private String email;
    private String telephone;

    public Client(String id, String username, String password,
                  String nom, String prenom, String nomEntreprise,
                  String email, String telephone) {
        super(id, username, password, Role.CLIENT);
        this.nom = nom;
        this.prenom = prenom;
        this.nomEntreprise = nomEntreprise;
        this.email = email;
        this.telephone = telephone;
    }

    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public String getNomEntreprise() { return nomEntreprise; }
    public String getEmail() { return email; }
    public String getTelephone() { return telephone; }

    public void setNom(String nom) { this.nom = nom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public void setNomEntreprise(String nomEntreprise) { this.nomEntreprise = nomEntreprise; }
    public void setEmail(String email) { this.email = email; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
}
