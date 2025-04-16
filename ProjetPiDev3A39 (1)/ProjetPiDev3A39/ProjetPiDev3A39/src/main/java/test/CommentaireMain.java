package test;

import entite.Commentaire;
import entite.User;
import service.CommentaireService;

public class CommentaireMain {

    public static void main(String[] args) {
        CommentaireService commentaireService = new CommentaireService();
        Commentaire commentaire = null; // Déclaration en dehors du try

        try {
            // Créer un commentaire
            commentaire = new Commentaire();
            User user = new User();
            user.setId(1);
            commentaire.setUserId(user);
            commentaire.setProfileId(1);
            commentaire.setComment("Test comment");
            commentaire.setConsultationId(1);
            commentaire.setReportReason("None");
            commentaire.setReported(false);

            // Ajouter le commentaire
            commentaireService.create(commentaire);
            System.out.println("Commentaire ajouté avec succès");

            // Lire les commentaires
            System.out.println("Commentaires pour le profil ID 1 :");
            commentaireService.readByProfileId(1).forEach(System.out::println);

            // Supprimer le commentaire
            commentaireService.delete(commentaire);
            System.out.println("Commentaire supprimé avec succès");

            // Vérifier après suppression
            System.out.println("Commentaires pour le profil ID 1 après suppression :");
            commentaireService.readByProfileId(1).forEach(System.out::println);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}