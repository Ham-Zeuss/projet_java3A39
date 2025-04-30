package service.Oumaima;

import entite.User;
import entite.Oumaima.Quiz;
import entite.Oumaima.QuizResult;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class QuizResultService {
    private Connection connection; // Connexion à la base de données

    public QuizResultService(Connection connection) {
        this.connection = connection;
    }

    public QuizResultService() {
        // Constructeur par défaut (sans connexion explicite)
        // Vous devrez peut-être initialiser la connexion ici si elle est utilisée dans votre projet
        // Par exemple, en utilisant une classe utilitaire comme DataSource
        this.connection = util.DataSource.getInstance().getConnection();
        if (this.connection == null) {
            throw new IllegalStateException("Impossible d'établir une connexion à la base de données");
        }
    }

    // Ajouter un résultat de quiz
    public void addQuizResult(QuizResult quizResult) throws Exception {
        // Vérifier si l'utilisateur a le rôle "ROLE_PARENT"
        User user = quizResult.getUser();
        if (user == null || !user.getRoles().contains("ROLE_PARENT")) {
            throw new Exception("L'utilisateur doit avoir le rôle ROLE_PARENT pour enregistrer un résultat de quiz.");
        }

        // Insérer dans la table quiz_result
        String sql = "INSERT INTO quiz_result (quiz_id, user_id, note) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, quizResult.getQuiz().getId()); // quiz_id
            stmt.setInt(2, quizResult.getUser().getId()); // user_id
            stmt.setFloat(3, quizResult.getNote());       // note
            stmt.executeUpdate();

            // Récupérer l'ID généré
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    quizResult.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new Exception("Erreur lors de l'insertion dans quiz_result : " + e.getMessage());
        }
    }

    // Récupérer tous les résultats de quiz
    public List<QuizResult> readAll() throws SQLException {
        List<QuizResult> quizResults = new ArrayList<>();
        String sql = "SELECT * FROM quiz_result";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                QuizResult quizResult = new QuizResult();
                quizResult.setId(rs.getInt("id"));
                quizResult.setQuiz(new Quiz(rs.getInt("quiz_id"))); // Charger l'objet Quiz si nécessaire
                User user = new User();
                user.setId(rs.getInt("user_id"));
                quizResult.setUser(user);
                quizResult.setNote(rs.getFloat("note"));
                quizResults.add(quizResult);
            }
        }
        return quizResults;
    }

    // Récupérer tous les résultats de quiz pour un utilisateur donné
    public List<QuizResult> getQuizResultsByUser(User user) throws SQLException {
        List<QuizResult> quizResults = new ArrayList<>();
        String sql = "SELECT * FROM quiz_result WHERE user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, user.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    QuizResult quizResult = new QuizResult();
                    quizResult.setId(rs.getInt("id"));
                    quizResult.setQuiz(new Quiz(rs.getInt("quiz_id"))); // Charger l'objet Quiz si nécessaire
                    quizResult.setUser(user); // L'utilisateur est déjà fourni
                    quizResult.setNote(rs.getFloat("note"));
                    quizResults.add(quizResult);
                }
            }
        }
        return quizResults;
    }

    // Récupérer un résultat de quiz par ID
    public QuizResult getQuizResultById(int id) throws SQLException {
        String sql = "SELECT * FROM quiz_result WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    QuizResult quizResult = new QuizResult();
                    quizResult.setId(rs.getInt("id"));
                    quizResult.setQuiz(new Quiz(rs.getInt("quiz_id"))); // Charger l'objet Quiz si nécessaire
                    // Charger l'utilisateur (simplifié ici, tu peux ajouter une méthode pour charger User)
                    User user = new User();
                    user.setId(rs.getInt("user_id"));
                    quizResult.setUser(user);
                    quizResult.setNote(rs.getFloat("note"));
                    return quizResult;
                }
            }
        }
        return null;
    }

    // Mettre à jour un résultat de quiz
    public void updateQuizResult(QuizResult quizResult) throws Exception {
        // Vérifier si l'utilisateur a le rôle "ROLE_PARENT"
        User user = quizResult.getUser();
        if (user == null || !user.getRoles().contains("ROLE_PARENT")) {
            throw new Exception("L'utilisateur doit avoir le rôle ROLE_PARENT pour modifier un résultat de quiz.");
        }

        String sql = "UPDATE quiz_result SET quiz_id = ?, user_id = ?, note = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, quizResult.getQuiz().getId());
            stmt.setInt(2, quizResult.getUser().getId());
            stmt.setFloat(3, quizResult.getNote());
            stmt.setInt(4, quizResult.getId());
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new Exception("Aucun résultat de quiz trouvé avec l'ID " + quizResult.getId());
            }
        } catch (SQLException e) {
            throw new Exception("Erreur lors de la mise à jour de quiz_result : " + e.getMessage());
        }
    }

    // Supprimer un résultat de quiz
    public void deleteQuizResult(int id) throws SQLException {
        String sql = "DELETE FROM quiz_result WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}