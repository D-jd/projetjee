package com.reservation.dao;

import com.reservation.model.Reservation;
import java.util.*;

public class ReservationDAO {
    private static ReservationDAO instance;
    private Map<String, Reservation> reservations = new HashMap<>();
    private int counter = 1;

    private ReservationDAO() {}

    public static ReservationDAO getInstance() {
        if (instance == null) instance = new ReservationDAO();
        return instance;
    }

    public Reservation findById(String id) {
        return reservations.get(id);
    }

    public List<Reservation> findAll() {
        return new ArrayList<>(reservations.values());
    }

    public List<Reservation> findByClient(String clientId) {
        List<Reservation> result = new ArrayList<>();
        for (Reservation r : reservations.values()) {
            if (r.getClientId().equals(clientId)) result.add(r);
        }
        return result;
    }

    public List<Reservation> findBySalle(String salleId) {
        List<Reservation> result = new ArrayList<>();
        for (Reservation r : reservations.values()) {
            if (r.getSalleId().equals(salleId)) result.add(r);
        }
        return result;
    }

    public Reservation save(Reservation reservation) {
        reservations.put(reservation.getId(), reservation);
        return reservation;
    }

    public String generateId() {
        return "res-" + (counter++);
    }

    public boolean delete(String id) {
        return reservations.remove(id) != null;
    }
}
