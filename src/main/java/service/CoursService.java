package service;

import entite.Cours;
import entite.Module;
import util.DataSource;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CoursService implements IService<Cours> {

    private Connection cnx;

    public CoursService() {
        cnx = DataSource.getInstance().getConnection();
    }

    @Override
    public void create(Cours cours){

    } // Using Statement

    @Override
    public void createPst(Cours cours) {
        String requete = "INSERT INTO cours (title, module_id, pdf_name, user_id, updated_at) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pst = cnx.prepareStatement(requete)) {
            pst.setString(1, cours.getTitle());
            pst.setInt(2, cours.getModuleId().getId());
            pst.setString(3, cours.getPdfName());
            pst.setInt(4, cours.getUserId()); // Add the user_id field
            pst.setTimestamp(5, Timestamp.valueOf(cours.getUpdatedAt()));
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(Cours cours) {
        String requete = "DELETE FROM cours WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(requete)) {
            pst.setInt(1, cours.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(Cours cours) {
        String requete = "UPDATE cours SET title = ?, pdf_name = ?, updated_at = ? WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(requete)) {
            pst.setString(1, cours.getTitle());
            pst.setString(2, cours.getPdfName());
            pst.setTimestamp(3, Timestamp.valueOf(cours.getUpdatedAt()));
            pst.setInt(4, cours.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Cours> readAll() {
        List<Cours> list = new ArrayList<>();
        String requete = "SELECT * FROM cours";
        try (Statement ste = cnx.createStatement(); ResultSet rs = ste.executeQuery(requete)) {
            while (rs.next()) {
                Module module = new Module();
                module.setId(rs.getInt("module_id"));
                Integer userId = rs.getObject("user_id") != null ? rs.getInt("user_id") : null; // Handle NULL values for user_id
                list.add(new Cours(
                        rs.getInt("id"),
                        rs.getString("title"),
                        module,
                        rs.getString("pdf_name"),
                        userId, // Pass the user_id
                        rs.getTimestamp("updated_at").toLocalDateTime()
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public Cours readById(int id) {
        String requete = "SELECT * FROM cours WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(requete)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                Module module = new Module();
                module.setId(rs.getInt("module_id"));
                Integer userId = rs.getObject("user_id") != null ? rs.getInt("user_id") : null; // Handle NULL values for user_id
                return new Cours(
                        rs.getInt("id"),
                        rs.getString("title"),
                        module,
                        rs.getString("pdf_name"),
                        userId, // Pass the user_id
                        rs.getTimestamp("updated_at").toLocalDateTime()
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public List<Cours> getCoursByModule(int moduleId) {
        List<Cours> coursList = new ArrayList<>();
        String requete = "SELECT * FROM cours WHERE module_id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(requete)) {
            pst.setInt(1, moduleId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Integer id = rs.getInt("id");
                String title = rs.getString("title");
                int moduleIdValue = rs.getInt("module_id");
                String pdfName = rs.getString("pdf_name");
                Integer userId = rs.getObject("user_id") != null ? rs.getInt("user_id") : null; // Handle NULL values for user_id
                Timestamp updatedAtTimestamp = rs.getTimestamp("updated_at");

                // Handle NULL values for updatedAt
                LocalDateTime updatedAt = (updatedAtTimestamp != null) ? updatedAtTimestamp.toLocalDateTime() : null;

                // Create a Module object
                Module module = new Module();
                module.setId(moduleIdValue);

                // Create and add the Cours object
                Cours cours = new Cours(id, title, module, pdfName, userId, updatedAt); // Pass the user_id
                coursList.add(cours);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return coursList;
    }}