package Controller.Boubaker;

import entite.Commande;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import service.CommandeService;

import java.sql.SQLException;

public class UpdateOrderController {

    @FXML
    private TextField idField;

    @FXML
    private TextField userIdField;

    @FXML
    private TextField packIdField;

    @FXML
    private TextField amountField;

    @FXML
    private TextField paymentMethodField;

    @FXML
    private TextField statusField;

    @FXML
    private Button updateButton;

    @FXML
    private Label errorLabel;

    private final CommandeService commandeService = new CommandeService();
    private Commande originalCommande;

    public void loadOrderData(Commande commande) {
        this.originalCommande = commande;
        idField.setText(String.valueOf(commande.getId()));
        userIdField.setText(String.valueOf(commande.getUserId()));
        packIdField.setText(String.valueOf(commande.getPackId()));
        amountField.setText(String.valueOf(commande.getAmount()));
        paymentMethodField.setText(commande.getPaymentMethod());
        statusField.setText(commande.getStatus());
    }

    @FXML
    public void initialize() {
        updateButton.setOnAction(event -> {
            try {
                // Validate inputs
                if (userIdField.getText().isEmpty() || packIdField.getText().isEmpty() ||
                        amountField.getText().isEmpty() || paymentMethodField.getText().isEmpty() ||
                        statusField.getText().isEmpty()) {
                    errorLabel.setText("All fields must be filled");
                    return;
                }

                int userId = Integer.parseInt(userIdField.getText());
                int packId = Integer.parseInt(packIdField.getText());
                double amount = Double.parseDouble(amountField.getText());

                if (userId <= 0 || packId <= 0 || amount < 0) {
                    errorLabel.setText("Invalid input: IDs must be positive, amount must be non-negative");
                    return;
                }

                Commande updatedCommande = new Commande(
                        Integer.parseInt(idField.getText()),
                        userId,
                        packId,
                        amount,
                        originalCommande.getCommandeDate(),
                        paymentMethodField.getText(),
                        originalCommande.getExpiryDate(),
                        statusField.getText()
                );

                commandeService.updateCommande(updatedCommande);
                errorLabel.setText("Order updated successfully");
                // Close window after successful update
                updateButton.getScene().getWindow().hide();
            } catch (NumberFormatException e) {
                errorLabel.setText("Invalid input format: Ensure IDs and amount are numeric");
            } catch (RuntimeException e) {
                errorLabel.setText("Error updating order: " + e.getMessage());
            }
        });
    }
}