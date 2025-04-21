package entite;

public class Profile {
    private int id;
    private User userId; // Reference to the User entity (for user_id foreign key)
    private String biographie; // Nullable
    private String specialite;
    private String ressources; // Nullable
    private double prixConsultation;
    private Double latitude; // Nullable
    private Double longitude; // Nullable

    // Default constructor
    public Profile() {
    }

    // Constructor with all fields (including id)
    public Profile(int id, User userId, String biographie, String specialite, String ressources, double prixConsultation, Double latitude, Double longitude) {
        this.id = id;
        this.userId = userId;
        this.biographie = biographie;
        this.specialite = specialite;
        this.ressources = ressources;
        this.prixConsultation = prixConsultation;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Constructor without id (useful for creating new records)
    public Profile(User userId, String biographie, String specialite, String ressources, double prixConsultation, Double latitude, Double longitude) {
        this.userId = userId;
        this.biographie = biographie;
        this.specialite = specialite;
        this.ressources = ressources;
        this.prixConsultation = prixConsultation;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getUserId() {
        return userId;
    }

    public void setUserId(User userId) {
        this.userId = userId;
    }

    public String getBiographie() {
        return biographie;
    }

    public void setBiographie(String biographie) {
        this.biographie = biographie;
    }

    public String getSpecialite() {
        return specialite;
    }

    public void setSpecialite(String specialite) {
        this.specialite = specialite;
    }

    public String getRessources() {
        return ressources;
    }

    public void setRessources(String ressources) {
        this.ressources = ressources;
    }

    public double getPrixConsultation() {
        return prixConsultation;
    }

    public void setPrixConsultation(double prixConsultation) {
        this.prixConsultation = prixConsultation;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "Profile{" +
                "id=" + id +
                ", userId=" + (userId != null ? userId.getNom() + " " + userId.getPrenom() : "null") +
                ", biographie='" + (biographie != null ? biographie : "null") + '\'' +
                ", specialite='" + specialite + '\'' +
                ", ressources='" + (ressources != null ? ressources : "null") + '\'' +
                ", prixConsultation=" + prixConsultation +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}