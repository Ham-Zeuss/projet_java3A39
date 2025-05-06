package Controller.Boubaker;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class PaiementManagementController {

    @FXML private Button viewOrdersBtn;
    @FXML private Button viewPacksBtn;
    @FXML private Button addPackBtn;

    @FXML
    public void initialize() {
        viewOrdersBtn.setOnAction(event -> openFXML("/Boubaker/view_orders.fxml", "View Orders"));
        viewPacksBtn.setOnAction(event -> openFXML("/Boubaker/view_packs.fxml", "View Packs"));
        addPackBtn.setOnAction(event -> openFXML("/Boubaker/add_pack.fxml", "Add Pack"));
    }

    private void openFXML(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            if (loader.getLocation() == null) {
                System.out.println("Error: FXML not found at " + fxmlPath);
                return;
            }
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error loading FXML: " + fxmlPath + " - " + e.getMessage());
        }
    }
}