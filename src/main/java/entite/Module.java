package entite;
import javafx.beans.property.*;
import java.util.ArrayList;
import java.util.List;


public class Module {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty title = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final IntegerProperty nombreCours = new SimpleIntegerProperty();
    private final StringProperty level = new SimpleStringProperty();
    private List<Cours> coursList;

    public Module() {}

    public Module(String title, String description, Integer nombreCours, String level) {
        setTitle(title);
        setDescription(description);
        setNombreCours(nombreCours);
        setLevel(level);
        this.coursList = new ArrayList<>();
    }
    public Module(Integer id, String title, String description, Integer nombreCours, String level) {
        setId(id);
        setTitle(title);
        setDescription(description);
        setNombreCours(nombreCours);
        setLevel(level);
        this.coursList = new ArrayList<>();
    }

    // Getters and Setters for JavaFX properties
    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public String getTitle() {
        return title.get();
    }

    public void setTitle(String title) {
        this.title.set(title);
    }

    public StringProperty titleProperty() {
        return title;
    }

    public String getDescription() {
        return description.get();
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public int getNombreCours() {
        return nombreCours.get();
    }

    public void setNombreCours(int nombreCours) {
        this.nombreCours.set(nombreCours);
    }

    public IntegerProperty nombreCoursProperty() {
        return nombreCours;
    }

    public String getLevel() {
        return level.get();
    }

    public void setLevel(String level) {
        this.level.set(level);
    }

    public StringProperty levelProperty() {
        return level;
    }

    public List<Cours> getCoursList() {
        return coursList;
    }

    public void setCoursList(List<Cours> coursList) {
        this.coursList = coursList;
    }
}