package service;

import entite.Quiz;
import entite.Cours;
import util.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuizService implements IService<Quiz> {

    private Connection cnx;
    private PreparedStatement pst;
    private Statement ste;
    private ResultSet rs;

    public QuizService() {
        cnx = DataSource.getInstance().getConnection();
    }

    @Override
    public void create(Quiz quiz) {
        String requete = "INSERT INTO quiz (course_id, title, description, duration, created_at, note) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            pst = cnx.prepareStatement(requete, Statement.RETURN_GENERATED_KEYS);
            pst.setInt(1, quiz.getCourse().getId());
            pst.setString(2, quiz.getTitle());
            pst.setString(3, quiz.getDescription());
            pst.setInt(4, quiz.getDuration());
            pst.setTimestamp(5, new Timestamp(quiz.getCreatedAt().getTime()));
            pst.setFloat(6, quiz.getNote());
            pst.executeUpdate();

            rs = pst.getGeneratedKeys();
            if (rs.next()) {
                quiz.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(Quiz quiz) {
        String requete = "DELETE FROM quiz WHERE id = ?";
        try {
            pst = cnx.prepareStatement(requete);
            pst.setInt(1, quiz.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(Quiz quiz) {
        String requete = "UPDATE quiz SET course_id=?, title=?, description=?, duration=?, created_at=?, note=? WHERE id=?";
        try {
            pst = cnx.prepareStatement(requete);
            pst.setInt(1, quiz.getCourse().getId());
            pst.setString(2, quiz.getTitle());
            pst.setString(3, quiz.getDescription());
            pst.setInt(4, quiz.getDuration());
            pst.setTimestamp(5, new Timestamp(quiz.getCreatedAt().getTime()));
            pst.setFloat(6, quiz.getNote());
            pst.setInt(7, quiz.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Quiz> readAll() {
        List<Quiz> list = new ArrayList<>();
        String requete = "SELECT q.*, c.title AS course_title " +
                "FROM quiz q " +
                "LEFT JOIN cours c ON q.course_id = c.id";
        try {
            ste = cnx.createStatement();
            rs = ste.executeQuery(requete);
            while (rs.next()) {
                Cours cours = new Cours();
                cours.setId(rs.getInt("course_id"));
                cours.setTitle(rs.getString("course_title")); // récupération du titre du cours

                Quiz quiz = new Quiz(
                        rs.getInt("id"),
                        cours,
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getInt("duration"),
                        rs.getTimestamp("created_at"),
                        rs.getFloat("note")
                );

                list.add(quiz);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public Quiz readById(int id) {
        String requete = "SELECT q.*, c.title AS course_title " +
                "FROM quiz q " +
                "LEFT JOIN cours c ON q.course_id = c.id " +
                "WHERE q.id = ?";
        try {
            pst = cnx.prepareStatement(requete);
            pst.setInt(1, id);
            rs = pst.executeQuery();
            if (rs.next()) {
                Cours cours = new Cours();
                cours.setId(rs.getInt("course_id"));
                cours.setTitle(rs.getString("course_title")); // récupération du titre du cours

                return new Quiz(
                        rs.getInt("id"),
                        cours,
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getInt("duration"),
                        rs.getTimestamp("created_at"),
                        rs.getFloat("note")
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
