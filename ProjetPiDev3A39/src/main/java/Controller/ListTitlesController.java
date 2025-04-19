package Controller;

import entite.Title;
import service.TitleService;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import java.io.IOException;

public class ListTitlesController {

    @FXML
    private TableView<Title> titlesTable;

    @FXML
    private TableColumn<Title, Integer> idColumn;

    @FXML
    private TableColumn<Title, String> nameColumn;

    @FXML
    private TableColumn<Title, Integer> pointsRequiredColumn;

    @FXML
    private TableColumn<Title, Integer> priceColumn;

    @FXML
    private TableColumn<Title, Void> actionColumn;

    @FXML
    private Label errorLabel;

    private TitleService titleService;

    @FXML
    public void initialize() {
        titleService = new TitleService();

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        pointsRequiredColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getpoints_required()).asObject()
        );
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));

        addActionButtons();

        loadTitles();
    }

    private void loadTitles() {
        try {
            ObservableList<Title> titles = FXCollections.observableArrayList(titleService.readAll());
            titlesTable.setItems(titles);
            errorLabel.setText("");
        } catch (Exception e) {
            errorLabel.setText("Error loading titles: " + e.getMessage());
        }
    }

    private void addActionButtons() {
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button("Delete");
            private final Button updateButton = new Button("Update");
            private final HBox hbox = new HBox(10, updateButton, deleteButton);

            {
                deleteButton.setOnAction(event -> {
                    Title selectedTitle = getTableView().getItems().get(getIndex());
                    try {
                        titleService.delete(selectedTitle);
                        loadTitles(); // Refresh table
                    } catch (Exception e) {
                        errorLabel.setText("Error deleting title: " + e.getMessage());
                    }
                });

                updateButton.setOnAction(event -> {
                    Title selectedTitle = getTableView().getItems().get(getIndex());
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/update_title.fxml"));
                        Parent root = loader.load();

                        UpdateTitleController controller = loader.getController();
                        controller.setTitle(selectedTitle);

                        Stage stage = new Stage();
                        stage.setTitle("Update Title");
                        stage.setScene(new Scene(root));
                        stage.showAndWait(); // Wait until window closes

                        loadTitles(); // Refresh table after update

                    } catch (IOException e) {
                        e.printStackTrace();
                        errorLabel.setText("Error opening update window.");
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(hbox);
                }
            }
        });
    }
}
