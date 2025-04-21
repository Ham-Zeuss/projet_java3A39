package service.Oumaima;

import entite.Oumaima.Question;
import entite.Oumaima.Quiz;
import service.IService;
import util.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuestionService implements IService<Question> {

    private Connection cnx;
    private PreparedStatement pst;
    private Statement ste;
    private ResultSet rs;
    private QuizService quizService; // Pour récupérer les détails complets du Quiz

    public QuestionService() {
        cnx = DataSource.getInstance().getConnection();
        if (cnx == null) {
            System.err.println("Erreur : Connexion à la base de données nulle");
            throw new IllegalStateException("Impossible d'établir une connexion à la base de données");
        }
        System.out.println("Connexion à la base de données établie");
        try {
            cnx.setAutoCommit(true); // Assurer que les changements sont validés immédiatement
        } catch (SQLException e) {
            System.err.println("Erreur lors de la configuration d'autoCommit : " + e.getMessage());
            throw new RuntimeException("Erreur lors de la configuration d'autoCommit : " + e.getMessage(), e);
        }
        quizService = new QuizService(); // Initialisation du QuizService
    }

    @Override
    public void createPst(Question question) {
        // Méthode vide dans votre code initial
    }

    @Override
    public void create(Question question) {
        System.out.println("Début de QuestionService.create");
        // Validation des données
        if (question.getQuiz() == null || question.getQuiz().getId() <= 0) {
            throw new IllegalArgumentException("L'ID du quiz est requis et doit être supérieur à 0");
        }
        if (question.getText() == null || question.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("Le texte de la question est requis");
        }
        if (question.getOptionType() == null || question.getOptionType().trim().isEmpty()) {
            throw new IllegalArgumentException("Le type d'option est requis");
        }

        String requete = "INSERT INTO question (quiz_id, text, option_type, option1, option2, option3, option4, correct_answers, reponse_soumise) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            pst = cnx.prepareStatement(requete, Statement.RETURN_GENERATED_KEYS);
            pst.setInt(1, question.getQuiz().getId());
            pst.setString(2, question.getText());
            pst.setString(3, question.getOptionType());
            pst.setString(4, question.getOption1());
            pst.setString(5, question.getOption2());
            pst.setString(6, question.getOption3());
            pst.setString(7, question.getOption4());
            pst.setString(8, question.getCorrectAnswers());
            pst.setString(9, question.getReponseSoumise());
            System.out.println("Requête SQL préparée : quiz_id=" + question.getQuiz().getId() + ", text=" + question.getText() +
                    ", correctAnswers=" + question.getCorrectAnswers());
            int rowsAffected = pst.executeUpdate();
            System.out.println("Lignes affectées : " + rowsAffected);
            rs = pst.getGeneratedKeys();
            if (rs.next()) {
                question.setId(rs.getInt(1));
                System.out.println("ID généré pour la question : " + question.getId());
            } else {
                System.out.println("Aucun ID généré");
            }
        } catch (SQLException e) {
            System.err.println("Erreur SQL : " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Échec de l'insertion : " + e.getMessage(), e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (pst != null) pst.close();
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture des ressources : " + e.getMessage());
            }
        }
    }

    @Override
    public void delete(Question question) {
        System.out.println("Début de QuestionService.delete");
        String requete = "DELETE FROM question WHERE id = ?";
        try {
            pst = cnx.prepareStatement(requete);
            pst.setInt(1, question.getId());
            int rowsAffected = pst.executeUpdate();
            System.out.println("Lignes supprimées : " + rowsAffected);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression : " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            try {
                if (pst != null) pst.close();
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture des ressources : " + e.getMessage());
            }
        }
    }

    @Override
    public void update(Question question) {
        System.out.println("Début de QuestionService.update");
        String requete = "UPDATE question SET quiz_id=?, text=?, option_type=?, option1=?, option2=?, option3=?, option4=?, correct_answers=?, reponse_soumise=? WHERE id=?";
        try {
            pst = cnx.prepareStatement(requete);
            pst.setInt(1, question.getQuiz().getId());
            pst.setString(2, question.getText());
            pst.setString(3, question.getOptionType());
            pst.setString(4, question.getOption1());
            pst.setString(5, question.getOption2());
            pst.setString(6, question.getOption3());
            pst.setString(7, question.getOption4());
            pst.setString(8, question.getCorrectAnswers());
            pst.setString(9, question.getReponseSoumise());
            pst.setInt(10, question.getId());
            int rowsAffected = pst.executeUpdate();
            System.out.println("Lignes mises à jour : " + rowsAffected);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour : " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            try {
                if (pst != null) pst.close();
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture des ressources : " + e.getMessage());
            }
        }
    }

    @Override
    public List<Question> readAll() {
        System.out.println("Début de QuestionService.readAll");
        List<Question> list = new ArrayList<>();
        String requete = "SELECT * FROM question";
        try {
            ste = cnx.createStatement();
            rs = ste.executeQuery(requete);
            while (rs.next()) {
                int quizId = rs.getInt("quiz_id");
                Quiz quiz = quizService.readById(quizId); // Charger les détails complets du Quiz
                if (quiz == null) {
                    System.err.println("Quiz avec ID " + quizId + " introuvable, création d'un Quiz vide");
                    quiz = new Quiz();
                    quiz.setId(quizId);
                }
                list.add(new Question(
                        rs.getInt("id"),
                        quiz,
                        rs.getString("text"),
                        rs.getString("option_type"),
                        rs.getString("option1"),
                        rs.getString("option2"),
                        rs.getString("option3"),
                        rs.getString("option4"),
                        rs.getString("correct_answers"),
                        rs.getString("reponse_soumise")
                ));
            }
            System.out.println("Questions récupérées : " + list.size());
        } catch (SQLException e) {
            System.err.println("Erreur lors de la lecture : " + e.getMessage());
            throw new RuntimeException("Erreur lors de la lecture des questions : " + e.getMessage(), e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (ste != null) ste.close();
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture des ressources : " + e.getMessage());
            }
        }
        return list;
    }

    @Override
    public Question readById(int id) {
        System.out.println("Début de QuestionService.readById, id=" + id);
        String requete = "SELECT * FROM question WHERE id = ?";
        try {
            pst = cnx.prepareStatement(requete);
            pst.setInt(1, id);
            rs = pst.executeQuery();
            if (rs.next()) {
                int quizId = rs.getInt("quiz_id");
                Quiz quiz = quizService.readById(quizId); // Charger les détails complets du Quiz
                if (quiz == null) {
                    System.err.println("Quiz avec ID " + quizId + " introuvable, création d'un Quiz vide");
                    quiz = new Quiz();
                    quiz.setId(quizId);
                }
                Question question = new Question(
                        rs.getInt("id"),
                        quiz,
                        rs.getString("text"),
                        rs.getString("option_type"),
                        rs.getString("option1"),
                        rs.getString("option2"),
                        rs.getString("option3"),
                        rs.getString("option4"),
                        rs.getString("correct_answers"),
                        rs.getString("reponse_soumise")
                );
                System.out.println("Question récupérée : id=" + question.getId());
                return question;
            } else {
                System.out.println("Aucune question trouvée pour id=" + id);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la lecture : " + e.getMessage());
            throw new RuntimeException("Erreur lors de la lecture de la question avec id=" + id + " : " + e.getMessage(), e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (pst != null) pst.close();
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture des ressources : " + e.getMessage());
            }
        }
        return null;
    }

    public List<Question> getQuestionsByQuizId(int quizId) {
        System.out.println("Début de QuestionService.getQuestionsByQuizId, quizId=" + quizId);
        // Validation de quizId
        if (quizId <= 0) {
            throw new IllegalArgumentException("L'ID du quiz doit être supérieur à 0");
        }

        List<Question> list = new ArrayList<>();
        String requete = "SELECT * FROM question WHERE quiz_id = ?";
        try {
            pst = cnx.prepareStatement(requete);
            pst.setInt(1, quizId);
            rs = pst.executeQuery();
            Quiz quiz = quizService.readById(quizId); // Charger les détails complets du Quiz
            if (quiz == null) {
                System.err.println("Quiz avec ID " + quizId + " introuvable, création d'un Quiz vide");
                quiz = new Quiz();
                quiz.setId(quizId);
            }
            while (rs.next()) {
                list.add(new Question(
                        rs.getInt("id"),
                        quiz,
                        rs.getString("text"),
                        rs.getString("option_type"),
                        rs.getString("option1"),
                        rs.getString("option2"),
                        rs.getString("option3"),
                        rs.getString("option4"),
                        rs.getString("correct_answers"),
                        rs.getString("reponse_soumise")
                ));
            }
            System.out.println("Questions récupérées pour quizId=" + quizId + " : " + list.size());
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des questions pour quizId=" + quizId + " : " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la récupération des questions pour quizId=" + quizId + " : " + e.getMessage(), e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (pst != null) pst.close();
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture des ressources : " + e.getMessage());
            }
        }
        return list;
    }
}