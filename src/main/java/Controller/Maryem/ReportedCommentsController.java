package Controller.Maryem;

import entite.Commentaire;
import entite.User;
import entite.Profile;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Button;
import javafx.scene.control.cell.PropertyValueFactory;
import service.CommentaireService;
import service.UserService;
import service.ProfileService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportedCommentsController {

    @FXML
    private TableView<Commentaire> commentsTable;

    @FXML
    private TableColumn<Commentaire, Integer> idColumn;

    @FXML
    private TableColumn<Commentaire, String> userNameColumn;

    @FXML
    private TableColumn<Commentaire, String> profileNameColumn;

    @FXML
    private TableColumn<Commentaire, String> commentColumn;

    @FXML
    private TableColumn<Commentaire, String> reportReasonColumn;

    @FXML
    private TableColumn<Commentaire, Boolean> reportedColumn;

    @FXML
    private TableColumn<Commentaire, Void> deleteColumn;

    @FXML
    private Label errorLabel;

    private CommentaireService commentaireService;
    private UserService userService;
    private ProfileService profileService;
    private Map<Integer, User> userCache = new HashMap<>();
    private Map<Integer, Profile> profileCache = new HashMap<>();

    @FXML
    public void initialize() {
        try {
            // Initialize services
            commentaireService = new CommentaireService();
            userService = new UserService();
            profileService = new ProfileService();

            // Set up table columns
            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            commentColumn.setCellValueFactory(new PropertyValueFactory<>("comment"));
            reportReasonColumn.setCellValueFactory(new PropertyValueFactory<>("reportReason"));
            reportedColumn.setCellValueFactory(new PropertyValueFactory<>("reported"));

            // Custom cell value factory for user name
            userNameColumn.setCellValueFactory(cellData -> {
                Commentaire commentaire = cellData.getValue();
                try {
                    User user = userCache.computeIfAbsent(commentaire.getUserId(), id -> {
                        try {
                            return userService.readById(id);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return null;
                        }
                    });
                    return new javafx.beans.property.SimpleStringProperty(
                            user != null ? user.getPrenom() + " " + user.getNom() : "Unknown"
                    );
                } catch (Exception e) {
                    e.printStackTrace();
                    return new javafx.beans.property.SimpleStringProperty("Error");
                }
            });

            // Custom cell value factory for profile name
            profileNameColumn.setCellValueFactory(cellData -> {
                Commentaire commentaire = cellData.getValue();
                try {
                    Profile profile = profileCache.computeIfAbsent(commentaire.getProfileId(), id -> {
                        try {
                            return profileService.readById(id);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return null;
                        }
                    });
                    if (profile != null && profile.getUserId() != null) {
                        User user = profile.getUserId(); // Directly use the User object from Profile
                        return new javafx.beans.property.SimpleStringProperty(
                                user != null ? user.getPrenom() + " " + user.getNom() : "Unknown"
                        );
                    }
                    return new javafx.beans.property.SimpleStringProperty("Unknown");
                } catch (Exception e) {
                    e.printStackTrace();
                    return new javafx.beans.property.SimpleStringProperty("Error");
                }
            });

            // Set up delete button column
            deleteColumn.setCellFactory(param -> new javafx.scene.control.TableCell<>() {
                private final Button deleteButton = new Button("Delete");

                {
                    deleteButton.setStyle("-fx-background-color: #ff4d4d; -fx-text-fill: white;");
                    deleteButton.setOnAction(event -> {
                        Commentaire commentaire = getTableView().getItems().get(getIndex());
                        try {
                            commentaireService.delete(commentaire);
                            loadReportedComments(); // Refresh table
                        } catch (Exception e) {
                            e.printStackTrace();
                            errorLabel.setText("Error deleting comment: " + e.getMessage());
                        }
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(deleteButton);
                    }
                }
            });

            // Load reported comments
            loadReportedComments();
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Error initializing table: " + e.getMessage());
        }
    }

    private void loadReportedComments() {
        try {
            List<Commentaire> reportedComments = commentaireService.readReportedComments();
            if (reportedComments.isEmpty()) {
                errorLabel.setText("No reported comments found.");
            }
            commentsTable.getItems().setAll(reportedComments);
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Error loading reported comments: " + e.getMessage());
        }
    }
}