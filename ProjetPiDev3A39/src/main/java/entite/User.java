package entite;

public class User {
    private int id;
    private Title currentTitle; // Reference to the Title entity (for current_title_id foreign key)
    private String nom;
    private String prenom;
    private String email;
    private String roles; // JSON field, stored as String for simplicity
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
    private boolean isActive; // tinyint(1) maps to boolean
    private Double balance; // decimal(10,2), nullable
    private String featuresUnlocked; // JSON field, stored as String, nullable
    private String totpSecret; // Nullable

    // Default constructor
    public User() {
    }

    // Constructor with all fields (including id)
    public User(int id, Title currentTitle, String nom, String prenom, String email, String roles, String password,
                boolean isVerified, int age, String gouvernorat, Integer points, String numero, Integer enfantId,
                String photo, String status, Integer scoreTotal, boolean isActive, Double balance,
                String featuresUnlocked, String totpSecret) {
        this.id = id;
        this.currentTitle = currentTitle;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.roles = roles;
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
        this.isActive = isActive;
        this.balance = balance;
        this.featuresUnlocked = featuresUnlocked;
        this.totpSecret = totpSecret;
    }

    // Constructor without id (useful for creating new records)
    public User(Title currentTitle, String nom, String prenom, String email, String roles, String password,
                boolean isVerified, int age, String gouvernorat, Integer points, String numero, Integer enfantId,
                String photo, String status, Integer scoreTotal, boolean isActive, Double balance,
                String featuresUnlocked, String totpSecret) {
        this.currentTitle = currentTitle;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.roles = roles;
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
        this.isActive = isActive;
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

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

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

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

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
                ", isActive=" + isActive +
                ", balance=" + balance +
                ", featuresUnlocked='" + featuresUnlocked + '\'' +
                ", totpSecret='" + (totpSecret != null ? "[HIDDEN]" : "null") + '\'' +
                '}';
    }
}