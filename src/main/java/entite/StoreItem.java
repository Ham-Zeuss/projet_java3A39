package entite;

public class StoreItem {
    private int id;
    private Title title; // Reference to the Title entity (for title_id foreign key)
    private String name;
    private String description; // Using String for longtext
    private int price;
    private String image;
    private int stock;

    // Default constructor
    public StoreItem() {
    }

    // Constructor with all fields (including id)
    public StoreItem(int id, Title title, String name, String description, int price, String image, int stock) {
        this.id = id;
        this.title = title;
        this.name = name;
        this.description = description;
        this.price = price;
        this.image = image;
        this.stock = stock;
    }

    // Constructor without id (useful for creating new records)
    public StoreItem(Title title, String name, String description, int price, String image, int stock) {
        this.title = title;
        this.name = name;
        this.description = description;
        this.price = price;
        this.image = image;
        this.stock = stock;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Title getTitle() {
        return title;
    }

    public void setTitle(Title title) {
        this.title = title;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    @Override
    public String toString() {
        return "StoreItem{" +
                "id=" + id +
                ", title=" + (title != null ? title.getName() : "null") +
                ", name='" + name + '\'' +
                ", description='" + (description != null ? description : "null") + '\'' +
                ", price=" + price +
                ", image='" + (image != null ? image : "null") + '\'' +
                ", stock=" + stock +
                '}';
    }
}