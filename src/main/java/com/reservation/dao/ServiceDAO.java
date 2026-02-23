package com.reservation.dao;

import com.reservation.model.ServiceSupplementaire;
import java.util.*;

public class ServiceDAO {
    private static ServiceDAO instance;
    private Map<String, ServiceSupplementaire> services = new HashMap<>();
    private int counter = 1;

    private ServiceDAO() {
        // Services par défaut
        ServiceSupplementaire traiteur = new ServiceSupplementaire("srv-1", "Traiteur", 200.0);
        ServiceSupplementaire cafe = new ServiceSupplementaire("srv-2", "Café & Boissons", 50.0);
        ServiceSupplementaire sono = new ServiceSupplementaire("srv-3", "Sonorisation", 100.0);
        ServiceSupplementaire visio = new ServiceSupplementaire("srv-4", "Visioconférence", 150.0);

        services.put(traiteur.getId(), traiteur);
        services.put(cafe.getId(), cafe);
        services.put(sono.getId(), sono);
        services.put(visio.getId(), visio);
    }

    public static ServiceDAO getInstance() {
        if (instance == null) instance = new ServiceDAO();
        return instance;
    }

    public ServiceSupplementaire findById(String id) {
        return services.get(id);
    }

    public List<ServiceSupplementaire> findAll() {
        return new ArrayList<>(services.values());
    }

    public ServiceSupplementaire save(ServiceSupplementaire s) {
        services.put(s.getId(), s);
        return s;
    }

    public String generateId() {
        return "srv-" + (counter++);
    }

    public boolean delete(String id) {
        return services.remove(id) != null;
    }
}
