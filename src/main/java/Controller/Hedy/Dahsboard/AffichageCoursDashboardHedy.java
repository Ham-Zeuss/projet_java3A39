package Controller.Hedy.Dahsboard;

import entite.Cours;
import entite.Module;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import service.CoursService;

import java.util.List;

public class AffichageCoursDashboardHedy {

    @FXML private TableView<Cours> coursesTable;
    @FXML private TableColumn<Cours, String> titleColumn;
    @FXML private TableColumn<Cours, String> descriptionColumn;
    @FXML private TableColumn<Cours, String> typeColumn;
    @FXML private TableColumn<Cours, String> linkColumn;

    private Module module;
    private final CoursService coursService = new CoursService();

    @FXML
    public void initialize() {
        // Set up columns
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title")); // assuming the getter is getTitle
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        linkColumn.setCellValueFactory(new PropertyValueFactory<>("pdfName")); // or lien if you renamed it

        coursesTable.getStylesheets().add(getClass().getResource("/css/DesignDashboardHedy.css").toExternalForm());
    }

    public void setModule(Module module) {
        this.module = module;
        loadCoursesForModule();
    }

    private void loadCoursesForModule() {
        if (module != null) {
            List<Cours> coursList = coursService.getCoursByModule(module.getId()); // 🔧 corrected method name
            coursesTable.setItems(FXCollections.observableArrayList(coursList));
        }
    }
}
