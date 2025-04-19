package service;

import entite.Cours;
import entite.Module;
import util.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ModuleService implements IService<Module> {

    private Connection cnx;

    public ModuleService() {
        cnx = DataSource.getInstance().getConnection();
    }


    @Override
    public void create(Module module){

    } // Using Statement

    @Override
    public void createPst(Module module) {
        String requete = "INSERT INTO module (title, description, nombre_cours, level) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pst = cnx.prepareStatement(requete)) {
            pst.setString(1, module.getTitle());
            pst.setString(2, module.getDescription());
            pst.setInt(3, module.getNombreCours());
            pst.setString(4, module.getLevel());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(Module module) {
        String requete = "DELETE FROM module WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(requete)) {
            pst.setInt(1, module.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(Module module) {
        String requete = "UPDATE module SET title = ?, description = ?, nombre_cours = ?, level = ? WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(requete)) {
            pst.setString(1, module.getTitle());
            pst.setString(2, module.getDescription());
            pst.setInt(3, module.getNombreCours());
            pst.setString(4, module.getLevel());
            pst.setInt(5, module.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Module> readAll() {
        List<Module> list = new ArrayList<>();
        String requete = "SELECT * FROM module";
        try (Statement ste = cnx.createStatement(); ResultSet rs = ste.executeQuery(requete)) {
            while (rs.next()) {
                list.add(new Module(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getInt("nombre_cours"),
                        rs.getString("level")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public Module readById(int id) {
        String requete = "SELECT * FROM module WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(requete)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return new Module(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getInt("nombre_cours"),
                        rs.getString("level")
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}