package Controller.Maryem;

import entite.Commentaire;
import entite.Profile;
import entite.User;
import service.CommentaireService;
import service.UserService;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.util.List;

public class DisplayCommentsController {

    @FXML
    private TableView<Commentaire> commentsTable;

    @FXML
    private TableColumn<Commentaire, Number> idColumn;

    @FXML
    private TableColumn<Commentaire, String> userNameColumn;

    @FXML
    private TableColumn<Commentaire, String> commentColumn;

    @FXML
    private TableColumn<Commentaire, Number> consultationIdColumn;

    @FXML
    private TableColumn<Commentaire, String> reportReasonColumn;

    @FXML
    private TableColumn<Commentaire, Boolean> reportedColumn;

    @FXML
    private TableColumn<Commentaire, Void> actionColumn;

    @FXML
    private Label errorLabel;

    private CommentaireService commentaireService;
    private UserService userService;
    private Profile profile;

    public void initialize(Profile profile) {
        System.out.println("Entering DisplayCommentsController.initialize for profile ID: " + profile.getId());
        this.profile = profile;
        this.commentaireService = new CommentaireService();
        this.userService = new UserService();

        try {
            idColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()));

            if (userNameColumn == null) {
                throw new IllegalStateException("userNameColumn is null - check FXML file for fx:id='userNameColumn'");
            }
            userNameColumn.setCellValueFactory(cellData -> {
                try {
                    User user = userService.readById(cellData.getValue().getUserId());
                    String userName = user != null
                            ? (user.getNom() != null ? user.getNom() : "") + " " +
                            (user.getPrenom() != null ? user.getPrenom() : "")
                            : "Unknown";
                    return new SimpleStringProperty(userName.trim().isEmpty() ? "Unknown" : userName.trim());
                } catch (Exception e) {
                    e.printStackTrace();
                    return new SimpleStringProperty("Error");
                }
            });

            commentColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getComment()));

            consultationIdColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getConsultationId()));

            reportReasonColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getReportReason()));

            reportedColumn.setCellValueFactory(cellData -> new SimpleBooleanProperty(cellData.getValue().isReported()));

            actionColumn.setCellFactory(param -> new TableCell<>() {
                private final Button deleteButton = new Button();

                {
                    setupButton(deleteButton, "https://img.icons8.com/?size=100&id=97745&format=png&color=000000", "Delete Comment");
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        Commentaire commentaire = getTableView().getItems().get(getIndex());
                        deleteButton.setOnAction(event -> deleteCommentaire(commentaire));
                        setGraphic(new HBox(deleteButton));
                    }
                }
            });

            refreshTable();
            System.out.println("DisplayCommentsController initialized successfully");
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Error initializing comments: " + e.getMessage());
        }
    }

    private void setupButton(Button button, String iconUrl, String tooltipText) {
        try {
            ImageView icon = new ImageView(new Image(iconUrl));
            icon.setFitWidth(48);
            icon.setFitHeight(48);
            button.setGraphic(icon);
            button.setText("");
            button.setTooltip(new Tooltip(tooltipText));
            button.setMinSize(60, 60);
            button.setStyle("-fx-background-color: transparent; -fx-padding: 8;");
            button.getStyleClass().add("icon-button");
        } catch (Exception e) {
            System.out.println("Failed to load icon from " + iconUrl + ": " + e.getMessage());
            // Fallback: Set text if icon fails to load
            button.setText(tooltipText);
            button.setTooltip(new Tooltip(tooltipText));
            button.setMinSize(60, 60);
            button.getStyleClass().add("icon-button");
        }
    }

    private void deleteCommentaire(Commentaire commentaire) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Deletion");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Are you sure you want to delete this comment?");
        if (confirmation.showAndWait().get() == ButtonType.OK) {
            try {
                commentaireService.delete(commentaire);
                refreshTable();
                errorLabel.setText("Comment deleted successfully.");
            } catch (Exception e) {
                e.printStackTrace();
                errorLabel.setText("Error deleting comment: " + e.getMessage());
            }
        }
    }

    public void refreshTable() {
        try {
            List<Commentaire> commentaires = commentaireService.readByProfileId(profile.getId());
            commentsTable.getItems().setAll(commentaires);
            if (commentaires.isEmpty()) {
                errorLabel.setText("No comments found for this profile.");
            } else {
                errorLabel.setText("");
            }
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Error loading comments: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}