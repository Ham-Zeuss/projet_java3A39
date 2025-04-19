package Entity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class User {
    private int id;
    private String nom;
    private String prenom;
    private String email;
    private String numero;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    private String password;
    private List<String> roles = new ArrayList<>();
    private boolean active = true;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    private String status;

    private static final Gson gson = new Gson();
    private static final Type listType = new TypeToken<List<String>>() {
    }.getType();

    public User() {
        this.roles.add("ROLE_USER");
    }

    public User(int id, String nom, String prenom, String email, String numero,String password) {
        this();
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.numero = numero;
        this.password = password;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<String> getRoles() {
        return new ArrayList<>(roles);
    }

    public void setRoles(List<String> roles) {
        this.roles = roles != null ? new ArrayList<>(roles) : new ArrayList<>();
        ensureRoleUser();
    }

    public void setRolesFromJson(String json) {
        this.roles = new ArrayList<>();
        if (json != null && !json.isEmpty()) {
            try {
                List<String> parsedRoles = gson.fromJson(json, listType);
                if (parsedRoles != null) {
                    this.roles.addAll(parsedRoles);
                }
            } catch (com.google.gson.JsonSyntaxException e) {
                this.roles.addAll(Arrays.asList(json.split(",")));
            }
        }
        ensureRoleUser();
    }

    public String getRolesAsJson() {
        return gson.toJson(roles);
    }

    private void ensureRoleUser() {
        if (!roles.contains("ROLE_USER")) {
            roles.add("ROLE_USER");
        }
        roles = roles.stream().filter(role -> role != null && !role.trim().isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }

    public String getFullName() {
        return prenom + " " + nom;
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", nom='" + nom + "', prenom='" + prenom + "', email='" + email +
                "', numero='" + numero + "', roles=" + roles + ", active=" + active + '}';
    }
}