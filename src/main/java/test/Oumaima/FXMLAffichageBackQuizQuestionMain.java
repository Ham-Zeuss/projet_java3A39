package test.Oumaima;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class FXMLAffichageBackQuizQuestionMain extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            // Charger le fichier FXML
            Parent root = FXMLLoader.load(getClass().getResource("/OumaimaFXML/affichageBackQuiz.fxml"));
            if (root == null) {
                System.out.println("Erreur : Impossible de charger affichageBackQuiz.fxml. Vérifiez le chemin du fichier.");
                return;
            }

            // Créer la scène avec la taille préférée définie dans le FXML (1000x600)
            Scene scene = new Scene(root, 1000, 600);

            // Ajouter le fichier CSS
            String cssPath = getClass().getResource("/css/styles.css") != null ?
                    getClass().getResource("/css/styles.css").toExternalForm() : null;
            if (cssPath != null) {
                scene.getStylesheets().add(cssPath);
                System.out.println("CSS chargé avec succès : " + cssPath);
            } else {
                System.out.println("Erreur : Impossible de charger /css/styles.css. Vérifiez le chemin du fichier.");
            }

            // Configurer la fenêtre
            primaryStage.setTitle("Gestion des Quiz (Backend) - Test");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erreur lors du chargement de l'interface : " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}