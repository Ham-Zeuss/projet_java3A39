package service.Oumaima;

import entite.Oumaima.Cours;
import util.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CoursService {

    private Connection cnx;

    public CoursService() {
        cnx = DataSource.getInstance().getConnection();
    }

    public void create(Cours cours) {
        String sql = "INSERT INTO cours (module_id, title) VALUES (?, ?)";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setInt(1, cours.getModuleId());
            pst.setString(2, cours.getTitle());
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void update(Cours cours) {
        String sql = "UPDATE cours SET module_id = ?, title = ? WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setInt(1, cours.getModuleId());
            pst.setString(2, cours.getTitle());
            pst.setInt(3, cours.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(Cours cours) {
        String sql = "DELETE FROM cours WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setInt(1, cours.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Cours> readAll() {
        List<Cours> list = new ArrayList<>();
        String sql = "SELECT * FROM cours";
        try (Statement ste = cnx.createStatement(); ResultSet rs = ste.executeQuery(sql)) {
            while (rs.next()) {
                Cours cours = new Cours(
                        rs.getInt("id"),
                        rs.getInt("module_id"),
                        rs.getString("title")
                );
                list.add(cours);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Cours readById(int id) {
        String sql = "SELECT * FROM cours WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return new Cours(
                            rs.getInt("id"),
                            rs.getInt("module_id"),
                            rs.getString("title")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }




}
