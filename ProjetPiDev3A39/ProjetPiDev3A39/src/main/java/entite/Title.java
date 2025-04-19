package entite;

public class Title {
    private int id;
    private String name;
    private int points_required;
    private Integer price; // Using Integer instead of int to allow null values

    // Default constructor
    public Title() {
    }

    // Constructor with all fields (including id)
    public Title(int id, String name, int points_required, Integer price) {
        this.id = id;
        this.name = name;
        this.points_required = points_required;
        this.price = price;
    }

    // Constructor without id (useful for creating new records)
    public Title(String name, int points_required, Integer price) {
        this.name = name;
        this.points_required = points_required;
        this.price = price;
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
        this.name = name;
    }

    public int getpoints_required() {
        return points_required;
    }

    public void setpoints_required(int points_required) {
        this.points_required = points_required;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "Title{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", points_required=" + points_required +
                ", price=" + (price != null ? price : "null") +
                '}';
    }
}