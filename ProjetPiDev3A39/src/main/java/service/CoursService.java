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
    public void createPst(Cours cours) {
        String requete = "INSERT INTO cours (title, module_id, pdf_name, updated_at) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pst = cnx.prepareStatement(requete)) {
            pst.setString(1, cours.getTitle());
            pst.setInt(2, cours.getModuleId().getId());
            pst.setString(3, cours.getPdfName());
            pst.setTimestamp(4, Timestamp.valueOf(cours.getUpdatedAt()));
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
                list.add(new Cours(
                        rs.getInt("id"),
                        rs.getString("title"),
                        module,
                        rs.getString("pdf_name"),
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
                return new Cours(
                        rs.getInt("id"),
                        rs.getString("title"),
                        module,
                        rs.getString("pdf_name"),
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
                int moduleIdValue = rs.getInt("module_id"); // Assuming moduleId is stored as an integer
                String pdfName = rs.getString("pdf_name");
                Timestamp updatedAtTimestamp = rs.getTimestamp("updated_at");

                // Handle NULL values for updatedAt
                LocalDateTime updatedAt = (updatedAtTimestamp != null) ? updatedAtTimestamp.toLocalDateTime() : null;

                // Create a Module object (replace with actual logic if needed)
                Module module = new Module();
                module.setId(moduleIdValue);

                // Create and add the Cours object
                Cours cours = new Cours(id, title, module, pdfName, updatedAt);
                coursList.add(cours);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return coursList;
    }
}