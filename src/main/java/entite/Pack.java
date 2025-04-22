package entite;

public class Pack {
    private int id;
    private double price;
    private String features;
    private int validityPeriod;
    private String name;

    public Pack(int id, double price, String features, int validityPeriod, String name) {
        this.id = id;
        this.price = price;
        this.features = features;
        this.validityPeriod = validityPeriod;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public double getPrice() {
        return price;
    }

    public String getFeatures() {
        return features;
    }

    public int getValidityPeriod() {
        return validityPeriod;
    }

    public String getName() {
        return name;
    }
}