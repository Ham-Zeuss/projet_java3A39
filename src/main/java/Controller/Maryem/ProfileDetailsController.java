package Controller.Maryem;

import entite.Profile;
import entite.User;
import entite.Commentaire;
import entite.Session;
import service.CommentaireService;
import service.UserService;
import service.PerspectiveApiService;
import utils.ConfigLoader;
import com.gluonhq.maps.MapLayer;
import com.gluonhq.maps.MapPoint;
import com.gluonhq.maps.MapView;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class ProfileDetailsController implements Initializable {

    private static final double DEFAULT_ZOOM = 15.0;

    @FXML
    private Label nameLabel;

    @FXML
    private Label biographyLabel;

    @FXML
    private Label specialtyLabel;

    @FXML
    private Button resourcesButton;

    @FXML
    private Label priceLabel;

    @FXML
    private Label latitudeLabel;

    @FXML
    private Label longitudeLabel;

    @FXML
    private Button backButton;

    @FXML
    private Button addCommentButton;

    @FXML
    private VBox commentsContainer;

    @FXML
    private Label commentsErrorLabel;

    @FXML
    private TextArea commentTextArea;

    @FXML
    private Label commentErrorLabel;

    @FXML
    private ProgressIndicator commentLoadingIndicator;

    @FXML
    private MapView mapView;

    private Profile profile;
    private CommentaireService commentaireService;
    private UserService userService;
    private PerspectiveApiService perspectiveApiService;
    private CustomMarkerLayer markerLayer;
    private MapPoint markerPosition;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Configure buttons with icons and text
        if (backButton != null) {
            setupButton(backButton, "https://img.icons8.com/?size=100&id=113571&format=png&color=000000", "Retour", true);
            backButton.setOnAction(e -> goBack());
        }

        if (resourcesButton != null) {
            setupButton(resourcesButton, "https://img.icons8.com/?size=100&id=115637&format=png&color=000000", "Voir Resources", true);
            resourcesButton.setOnAction(e -> openResources());
        }

        if (addCommentButton != null) {
            setupButton(addCommentButton, "https://img.icons8.com/?size=100&id=114252&format=png&color=000000", "Envoyer", true);
            addCommentButton.setOnAction(e -> addComment());
        }
    }

    public void initialize(Profile profile) {
        this.profile = profile;
        this.commentaireService = new CommentaireService();
        this.userService = new UserService();
        this.perspectiveApiService = new PerspectiveApiService(ConfigLoader.getProperty("perspective.api.key"));
        populateProfileDetails();
        initializeMap();
        loadComments();
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
        this.commentaireService = new CommentaireService();
        this.userService = new UserService();
        this.perspectiveApiService = new PerspectiveApiService(ConfigLoader.getProperty("perspective.api.key"));
        populateProfileDetails();
        initializeMap();
        loadComments();
    }

    private void setupButton(Button button, String iconUrl, String tooltipText, boolean showText) {
        try {
            ImageView icon = new ImageView(new Image(iconUrl));
            icon.setFitWidth(48);
            icon.setFitHeight(48);
            button.setGraphic(icon);
            button.setText(showText ? tooltipText : "");
            button.setTooltip(new Tooltip(tooltipText));
            button.setMinSize(showText ? 150 : 60, 60);
            button.getStyleClass().add("icon-button");
        } catch (Exception e) {
            System.out.println("Failed to load icon from " + iconUrl + ": " + e.getMessage());
            button.setText(tooltipText);
            button.setTooltip(new Tooltip(tooltipText));
            button.setMinSize(showText ? 150 : 60, 60);
            button.getStyleClass().add("icon-button");
        }
    }

    private void populateProfileDetails() {
        if (profile == null) {
            nameLabel.setText("No profile selected.");
            return;
        }

        User user = profile.getUserId();
        String userName = (user.getNom() != null ? user.getNom() : "") + " " +
                (user.getPrenom() != null ? user.getPrenom() : "");
        nameLabel.setText(userName.trim().isEmpty() ? "Anonymous" : userName.trim());

        biographyLabel.setText(profile.getBiographie() != null ? profile.getBiographie() : "N/A");
        specialtyLabel.setText(profile.getSpecialite() != null ? profile.getSpecialite() : "N/A");
        resourcesButton.setDisable(profile.getRessources() == null || profile.getRessources().isEmpty());
        priceLabel.setText(profile.getPrixConsultation() != 0 ? String.format("%.2f", profile.getPrixConsultation()) : "N/A");
        latitudeLabel.setText(profile.getLatitude() != 0 ? String.valueOf(profile.getLatitude()) : "N/A");
        longitudeLabel.setText(profile.getLongitude() != 0 ? String.valueOf(profile.getLongitude()) : "N/A");
    }

    private void initializeMap() {
        if (profile == null || profile.getLatitude() == 0 || profile.getLongitude() == 0) {
            latitudeLabel.setText("N/A");
            longitudeLabel.setText("N/A");
            mapView.setDisable(true);
            return;
        }

        markerPosition = new MapPoint(profile.getLatitude(), profile.getLongitude());
        mapView.setCenter(markerPosition);
        mapView.setZoom(DEFAULT_ZOOM);

        markerLayer = new CustomMarkerLayer(mapView, markerPosition);
        mapView.addLayer(markerLayer);

        latitudeLabel.setText(String.format("%.6f", markerPosition.getLatitude()));
        longitudeLabel.setText(String.format("%.6f", markerPosition.getLongitude()));

        mapView.addEventFilter(MouseEvent.MOUSE_DRAGGED, event -> event.consume());

        mapView.setOnMouseClicked(event -> {
            if (event.isStillSincePress()) {
                MapPoint clickedPoint = mapView.getMapPosition(event.getX(), event.getY());
                markerPosition = clickedPoint;
                markerLayer.updateMarker(markerPosition);
                latitudeLabel.setText(String.format("%.6f", markerPosition.getLatitude()));
                longitudeLabel.setText(String.format("%.6f", markerPosition.getLongitude()));
                System.out.println("Marker set to: Lat=" + markerPosition.getLatitude() +
                        ", Lon=" + markerPosition.getLongitude());
            }
        });
    }

    private void loadComments() {
        commentsContainer.getChildren().clear();
        commentsErrorLabel.setText("");

        try {
            var comments = commentaireService.readByProfileId(profile.getId());
            if (comments.isEmpty()) {
                commentsErrorLabel.setText("No comments found for this profile.");
                return;
            }

            for (Commentaire comment : comments) {
                User commenter = userService.readById(comment.getUserId());
                String commenterName = commenter != null ?
                        (commenter.getNom() != null ? commenter.getNom() : "") + " " +
                                (commenter.getPrenom() != null ? commenter.getPrenom() : "") : "Anonymous";
                commenterName = commenterName.trim().isEmpty() ? "Anonymous" : commenterName.trim();

                VBox card = new VBox();
                card.getStyleClass().add("card");
                card.setSpacing(20);

                Label titleLabel = new Label(commenterName);
                titleLabel.getStyleClass().add("title");
                titleLabel.setWrapText(true);

                Label descriptionLabel = new Label(comment.getComment());
                descriptionLabel.getStyleClass().add("description");
                descriptionLabel.setWrapText(true);

                card.getChildren().addAll(titleLabel, descriptionLabel);

                commentsContainer.getChildren().add(card);
            }

        } catch (Exception e) {
            e.printStackTrace();
            commentsErrorLabel.setText("Error loading comments: " + e.getMessage());
        }
    }

    private void openReportPopup(Commentaire comment) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MaryemFXML/ReportComment.fxml"));
            VBox root = loader.load();

            ReportCommentController controller = loader.getController();
            controller.initialize(comment);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Report Comment");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadComments();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open report pop-up: " + e.getMessage());
        }
    }

    @FXML
    private void addComment() {
        commentErrorLabel.getStyleClass().remove("success-label");
        commentErrorLabel.getStyleClass().add("error-label");

        if (commentLoadingIndicator != null) {
            commentLoadingIndicator.setVisible(true);
        }

        try {
            Session session = Session.getInstance();
            if (!session.isActive()) {
                commentErrorLabel.setText("Aucune session active. Veuillez vous connecter.");
                return;
            }
            int userId = session.getUserId();

            String commentText = commentTextArea.getText();
            if (commentText == null || commentText.trim().isEmpty()) {
                commentErrorLabel.setText("Le commentaire ne peut pas être vide.");
                return;
            }

            int consultationId = commentaireService.findCompletedConsultationId(userId, profile.getId());
            if (consultationId == 0) {
                commentErrorLabel.setText("Vous devez avoir une consultation terminée pour commenter.");
                return;
            }

            // Asynchronous toxicity check
            perspectiveApiService.isToxicAsync(commentText, "fr").whenComplete((isToxic, throwable) -> {
                Platform.runLater(() -> {
                    try {
                        if (throwable != null) {
                            throwable.printStackTrace(); // Log full exception
                            String message = throwable.getMessage().contains("Failed to serialize JSON payload") ? "Erreur de formatage du commentaire. Essayez un texte plus simple." :
                                    throwable.getMessage().contains("400") ? "Format de commentaire invalide. Évitez les caractères spéciaux ou réessayez." :
                                            throwable.getMessage().contains("429") ? "Limite de requêtes API dépassée. Réessayez plus tard." :
                                                    throwable.getMessage().contains("Invalid API key") ? "Clé API invalide. Contactez l'administrateur." :
                                                            "Erreur lors de l'analyse de la toxicité : " + throwable.getMessage();
                            commentErrorLabel.setText(message);
                            return;
                        }

                        if (isToxic) {
                            commentErrorLabel.setText("Votre commentaire a été détecté comme toxique et ne peut pas être publié.");
                            return;
                        }

                        Commentaire commentaire = new Commentaire();
                        commentaire.setUserId(userId);
                        commentaire.setProfileId(profile.getId());
                        commentaire.setComment(commentText);
                        commentaire.setConsultationId(consultationId);
                        commentaire.setReportReason(null);
                        commentaire.setReported(false);

                        commentaireService.create(commentaire);
                        commentErrorLabel.getStyleClass().remove("error-label");
                        commentErrorLabel.getStyleClass().add("success-label");
                        commentErrorLabel.setText("Commentaire ajouté avec succès.");

                        commentTextArea.clear();
                        loadComments();
                    } catch (Exception e) {
                        e.printStackTrace();
                        commentErrorLabel.setText("Erreur lors de l'ajout du commentaire : " + e.getMessage());
                    } finally {
                        if (commentLoadingIndicator != null) {
                            commentLoadingIndicator.setVisible(false);
                        }
                    }
                });
            });
        } catch (Exception e) {
            e.printStackTrace();
            commentErrorLabel.setText("Erreur lors de l'ajout du commentaire : " + e.getMessage());
            if (commentLoadingIndicator != null) {
                commentLoadingIndicator.setVisible(false);
            }
        }
    }

    @FXML
    private void openResources() {
        if (profile.getRessources() != null && !profile.getRessources().isEmpty()) {
            try {
                java.awt.Desktop.getDesktop().open(new java.io.File(profile.getRessources()));
            } catch (Exception e) {
                e.printStackTrace();
                nameLabel.setText("Error opening resources: " + e.getMessage());
            }
        }
    }

    @FXML
    private void goBack() {
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void openIcons8Link() {
        try {
            java.awt.Desktop.getDesktop().browse(new java.net.URI("https://icons8.com"));
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open Icons8 website: " + e.getMessage());
        }
    }

    private class CustomMarkerLayer extends MapLayer {
        private final ImageView marker;
        private final MapView mapView;
        private MapPoint currentPosition;
        private static final double ICON_WIDTH = 40.0;
        private static final double ICON_HEIGHT = 47.0;
        private static final double ICON_ANCHOR_X = ICON_WIDTH / 2.0;
        private static final double ICON_ANCHOR_Y = ICON_HEIGHT;

        public CustomMarkerLayer(MapView mapView, MapPoint initialPosition) {
            this.mapView = mapView;
            this.currentPosition = initialPosition;

            Image icon = new Image("https://img.icons8.com/?size=100&id=gh2uD53Hj8rj&format=png&color=000000");
            marker = new ImageView(icon);
            marker.setFitWidth(ICON_WIDTH);
            marker.setFitHeight(ICON_HEIGHT);
            this.getChildren().add(marker);

            layoutLayer();
        }

        public void updateMarker(MapPoint newPosition) {
            currentPosition = newPosition;
            layoutLayer();
        }

        @Override
        protected void layoutLayer() {
            if (currentPosition == null || mapView == null) return;

            MapPoint center = mapView.getCenter();
            double zoom = mapView.getZoom();
            double width = mapView.getWidth();
            double height = mapView.getHeight();

            double tileSize = 256;
            double pixelsPerLonDegree = tileSize * Math.pow(2, zoom) / 360;
            double pixelsPerLonRadian = tileSize * Math.pow(2, zoom) / (2 * Math.PI);

            double latRad = Math.toRadians(currentPosition.getLatitude());
            double centerLatRad = Math.toRadians(center.getLatitude());

            double mercatorY = Math.log(Math.tan(Math.PI/4 + latRad/2));
            double centerMercatorY = Math.log(Math.tan(Math.PI/4 + centerLatRad/2));

            double x = (currentPosition.getLongitude() - center.getLongitude()) * pixelsPerLonDegree;
            double y = (centerMercatorY - mercatorY) * pixelsPerLonRadian;

            marker.setTranslateX(width/2 + x - ICON_ANCHOR_X);
            marker.setTranslateY(height/2 + y - ICON_ANCHOR_Y);
        }

        @Override
        public void layoutChildren() {
            super.layoutChildren();
            layoutLayer();
        }
    }
}