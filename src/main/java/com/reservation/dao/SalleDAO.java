package com.reservation.dao;

import com.reservation.model.Salle;
import com.reservation.model.Equipement;
import java.util.*;

public class SalleDAO {
    private static SalleDAO instance;
    private Map<String, Salle> salles = new HashMap<>();
    private int counter = 1;

    private SalleDAO() {
        // Salles par défaut pour les tests
        Salle s1 = new Salle("salle-1", "Salle Apollo", 20, Salle.TypeSalle.CONFERENCE, 150.0, "Etage 1, Bâtiment A");
        s1.addEquipement(new Equipement("eq-1", "Projecteur"));
        s1.addEquipement(new Equipement("eq-2", "Tableau blanc"));
        s1.addEquipement(new Equipement("eq-3", "WiFi"));
        s1.setGestionnaireId("gest-1");
        salles.put(s1.getId(), s1);

        Salle s2 = new Salle("salle-2", "Salle Orion", 10, Salle.TypeSalle.REUNION, 80.0, "Etage 2, Bâtiment B");
        s2.addEquipement(new Equipement("eq-4", "Ecran TV"));
        s2.addEquipement(new Equipement("eq-5", "WiFi"));
        s2.setGestionnaireId("gest-1");
        salles.put(s2.getId(), s2);

        Salle s3 = new Salle("salle-3", "Salle Formation Pro", 30, Salle.TypeSalle.FORMATION, 200.0, "Etage 3, Bâtiment A");
        s3.addEquipement(new Equipement("eq-6", "Projecteur"));
        s3.addEquipement(new Equipement("eq-7", "Tableau blanc"));
        salles.put(s3.getId(), s3);
    }

    public static SalleDAO getInstance() {
        if (instance == null) instance = new SalleDAO();
        return instance;
    }

    public Salle findById(String id) {
        return salles.get(id);
    }

    public List<Salle> findAll() {
        return new ArrayList<>(salles.values());
    }

    public List<Salle> findByGestionnaire(String gestionnaireId) {
        List<Salle> result = new ArrayList<>();
        for (Salle s : salles.values()) {
            if (gestionnaireId.equals(s.getGestionnaireId())) result.add(s);
        }
        return result;
    }

    public Salle save(Salle salle) {
        if (salle.getId() == null || salle.getId().isEmpty()) {
            // ID généré en dehors
        }
        salles.put(salle.getId(), salle);
        return salle;
    }

    public String generateId() {
        return "salle-" + (counter++);
    }

    public boolean delete(String id) {
        return salles.remove(id) != null;
    }
}
