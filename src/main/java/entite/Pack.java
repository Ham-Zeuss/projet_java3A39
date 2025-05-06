package entite;

import com.google.gson.Gson;

import java.util.Objects;

public class Pack {
    private int id; // Unique identifier for the pack (primary key in the database)
    private String name; // Name of the pack
    private double price; // Price of the pack
    private String features; // Features included in the pack (stored as a single string)
    private int validityPeriod; // Duration (e.g., in days or months) for which the pack is valid

    /**
     * Default constructor for deserialization purposes.
     */
    public Pack() {}

    /**
     * Constructor for creating a new Pack (without ID).
     *
     * @param name           The name of the pack.
     * @param price          The price of the pack.
     * @param features       The features included in the pack (as a single string).
     * @param validityPeriod The validity period of the pack.
     */
    public Pack(String name, double price, String features, int validityPeriod) {
        validateName(name);
        validatePrice(price);
        validateFeatures(features);
        validateValidityPeriod(validityPeriod);

        this.name = name;
        this.price = price;
        this.features = features;
        this.validityPeriod = validityPeriod;
    }

    /**
     * Constructor for updating an existing Pack (with ID).
     *
     * @param id             The unique identifier of the pack.
     * @param name           The name of the pack.
     * @param price          The price of the pack.
     * @param features       The features included in the pack (as a single string).
     * @param validityPeriod The validity period of the pack.
     */
    public Pack(int id, String name, double price, String features, int validityPeriod) {
        this(name, price, features, validityPeriod);
        this.id = id;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        validateName(name);
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        validatePrice(price);
        this.price = price;
    }

    public String getFeatures() {
        return features;
    }

    public void setFeatures(String features) {
        validateFeatures(features);
        this.features = features;
    }

    public int getValidityPeriod() {
        return validityPeriod;
    }

    public void setValidityPeriod(int validityPeriod) {
        validateValidityPeriod(validityPeriod);
        this.validityPeriod = validityPeriod;
    }

    // Validation Methods
    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Pack name cannot be null or empty.");
        }
    }

    private void validatePrice(double price) {
        if (price < 0) {
            throw new IllegalArgumentException("Pack price cannot be negative.");
        }
    }

    private void validateFeatures(String features) {
        if (features == null || features.trim().isEmpty()) {
            throw new IllegalArgumentException("Pack features cannot be null or empty.");
        }
    }

    private void validateValidityPeriod(int validityPeriod) {
        if (validityPeriod <= 0) {
            throw new IllegalArgumentException("Validity period must be positive.");
        }
    }

    // Utility Methods
    @Override
    public String toString() {
        return "Pack{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", features='" + features + '\'' +
                ", validityPeriod=" + validityPeriod +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pack pack = (Pack) o;
        return id == pack.id &&
                Double.compare(pack.price, price) == 0 &&
                validityPeriod == pack.validityPeriod &&
                Objects.equals(name, pack.name) &&
                Objects.equals(features, pack.features);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, price, features, validityPeriod);
    }
}