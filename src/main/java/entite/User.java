package entite;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class User {
    private int id;
    private Title currentTitle; // Reference to the Title entity (for current_title_id foreign key)
    private String nom;
    private String prenom;
    private String email;

//dali
    private List<String> roles = new ArrayList<>();
    private boolean active = true;
    //private String roles; // JSON field, stored as String for simplicity


    private String password;
    private boolean isVerified; // tinyint(1) maps to boolean
    private int age;
    private String gouvernorat;
    private Integer points; // Nullable
    private String numero;
    private Integer enfantId; // Nullable
    private String photo;
    private String status; // Nullable
    private Integer scoreTotal; // Nullable


 //   private boolean isActive;



    private Double balance; // decimal(10,2), nullable
    private String featuresUnlocked; // JSON field, stored as String, nullable
    private String totpSecret; // Nullable


/*
    // Default constructor
    public User() {
    }
*/
    //dali

    public User() {
        this.roles.add("ROLE_USER");
    }

    //dali

    public User(int id, String nom, String prenom, String email, String numero,String password) {
        this();
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.numero = numero;
        this.password = password;
    }



    // Constructor with all fields (including id)
    public User(int id, Title currentTitle, String nom, String prenom, String email, List<String> roles, String password,
                boolean isVerified, int age, String gouvernorat, Integer points, String numero, Integer enfantId,
                String photo, String status, Integer scoreTotal, boolean isActive, Double balance,
                String featuresUnlocked, String totpSecret) {
        this.id = id;
        this.currentTitle = currentTitle;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.roles = new ArrayList<>();
        this.password = password;
        this.isVerified = isVerified;
        this.age = age;
        this.gouvernorat = gouvernorat;
        this.points = points;
        this.numero = numero;
        this.enfantId = enfantId;
        this.photo = photo;
        this.status = status;
        this.scoreTotal = scoreTotal;
        this.active = isActive;
        this.balance = balance;
        this.featuresUnlocked = featuresUnlocked;
        this.totpSecret = totpSecret;
    }

    // Constructor without id (useful for creating new records)
    public User(Title currentTitle, String nom, String prenom, String email, List<String> roles, String password,
                boolean isVerified, int age, String gouvernorat, Integer points, String numero, Integer enfantId,
                String photo, String status, Integer scoreTotal, boolean isActive, Double balance,
                String featuresUnlocked, String totpSecret) {
        this.currentTitle = currentTitle;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.roles = new ArrayList<>();
        this.password = password;
        this.isVerified = isVerified;
        this.age = age;
        this.gouvernorat = gouvernorat;
        this.points = points;
        this.numero = numero;
        this.enfantId = enfantId;
        this.photo = photo;
        this.status = status;
        this.scoreTotal = scoreTotal;
        this.active = isActive;
        this.balance = balance;
        this.featuresUnlocked = featuresUnlocked;
        this.totpSecret = totpSecret;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Title getCurrentTitle() {
        return currentTitle;
    }

    public void setCurrentTitle(Title currentTitle) {
        this.currentTitle = currentTitle;
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

 /*   public String getRoles() {
        return roles;
    }



  public void setRoles(String roles) {
       this.roles = roles;
    }
*/



    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGouvernorat() {
        return gouvernorat;
    }

    public void setGouvernorat(String gouvernorat) {
        this.gouvernorat = gouvernorat;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public Integer getEnfantId() {
        return enfantId;
    }

    public void setEnfantId(Integer enfantId) {
        this.enfantId = enfantId;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getScoreTotal() {
        return scoreTotal;
    }

    public void setScoreTotal(Integer scoreTotal) {
        this.scoreTotal = scoreTotal;
    }

 /*   public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

*/



    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public String getFeaturesUnlocked() {
        return featuresUnlocked;
    }

    public void setFeaturesUnlocked(String featuresUnlocked) {
        this.featuresUnlocked = featuresUnlocked;
    }

    public String getTotpSecret() {
        return totpSecret;
    }

    public void setTotpSecret(String totpSecret) {
        this.totpSecret = totpSecret;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", currentTitle=" + (currentTitle != null ? currentTitle.getName() : "null") +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", roles='" + roles + '\'' +
                ", password='[HIDDEN]'" + // Avoid printing password for security
                ", isVerified=" + isVerified +
                ", age=" + age +
                ", gouvernorat='" + gouvernorat + '\'' +
                ", points=" + points +
                ", numero='" + numero + '\'' +
                ", enfantId=" + enfantId +
                ", photo='" + photo + '\'' +
                ", status='" + status + '\'' +
                ", scoreTotal=" + scoreTotal +
                ", isActive=" + active +
                ", balance=" + balance +
                ", featuresUnlocked='" + featuresUnlocked + '\'' +
                ", totpSecret='" + (totpSecret != null ? "[HIDDEN]" : "null") + '\'' +
                '}';
    }







    private static final Gson gson = new Gson();
    private static final Type listType = new TypeToken<List<String>>() {
    }.getType();


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




}