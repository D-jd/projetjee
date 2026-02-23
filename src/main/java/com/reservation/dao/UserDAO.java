package com.reservation.dao;

import com.reservation.model.User;
import com.reservation.model.Client;
import java.util.*;

public class UserDAO {
    private static UserDAO instance;
    private Map<String, User> users = new HashMap<>();
    private int counter = 1;

    private UserDAO() {
        // Administrateur par défaut
        User admin = new User("admin-1", "admin", "admin", User.Role.ADMIN);
        users.put(admin.getId(), admin);

        // Gestionnaire par défaut pour les tests
        User gestionnaire = new User("gest-1", "gestionnaire", "gest123", User.Role.GESTIONNAIRE);
        users.put(gestionnaire.getId(), gestionnaire);

        // Client exemple
        Client client = new Client("client-1", "client1", "client123",
                "Dupont", "Jean", "Tech Corp", "jean.dupont@email.com", "0612345678");
        users.put(client.getId(), client);
    }

    public static UserDAO getInstance() {
        if (instance == null) instance = new UserDAO();
        return instance;
    }

    public User findByUsername(String username) {
        return users.values().stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst().orElse(null);
    }

    public User findById(String id) {
        return users.get(id);
    }

    public List<User> findByRole(User.Role role) {
        List<User> result = new ArrayList<>();
        for (User u : users.values()) {
            if (u.getRole() == role) result.add(u);
        }
        return result;
    }

    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    public User save(User user) {
        if (user.getId() == null || user.getId().isEmpty()) {
            // Pas applicable ici car les IDs sont fournis à la création
        }
        users.put(user.getId(), user);
        return user;
    }

    public String generateId(String prefix) {
        return prefix + "-" + (counter++);
    }

    public boolean delete(String id) {
        return users.remove(id) != null;
    }

    public boolean usernameExists(String username) {
        return users.values().stream().anyMatch(u -> u.getUsername().equals(username));
    }
}
