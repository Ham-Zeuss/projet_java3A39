package service;

import entite.Title;
import util.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TitleService implements IService<Title> {

    private Connection cnx;
    private Statement ste;
    private PreparedStatement pst;
    private ResultSet rs;

    public TitleService() {
        cnx = DataSource.getInstance().getConnection();
    }

    @Override
    public void create(Title title) {
        String requete = "insert into title (name, points_required, price) " +
                "values('" + title.getName() + "'," +
                title.getpoints_required() + "," +
                (title.getPrice() != null ? title.getPrice() : "NULL") + ")";
        try {
            ste = cnx.createStatement();
            ste.executeUpdate(requete);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void createPst(Title title) {
        String requete = "insert into title (name, points_required, price) values (?, ?, ?)";
        try {
            pst = cnx.prepareStatement(requete);
            pst.setString(1, title.getName());
            pst.setInt(2, title.getpoints_required());
            // Handle nullable price
            if (title.getPrice() != null) {
                pst.setInt(3, title.getPrice());
            } else {
                pst.setNull(3, Types.INTEGER);
            }
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(Title title) {
        String requete = "delete from title where id = ?";
        try {
            pst = cnx.prepareStatement(requete);
            pst.setInt(1, title.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(Title title) {
        String requete = "update title set name = ?, points_required = ?, price = ? where id = ?";
        try {
            pst = cnx.prepareStatement(requete);
            pst.setString(1, title.getName());
            pst.setInt(2, title.getpoints_required());
            // Handle nullable price
            if (title.getPrice() != null) {
                pst.setInt(3, title.getPrice());
            } else {
                pst.setNull(3, Types.INTEGER);
            }
            pst.setInt(4, title.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Title> readAll() {
        List<Title> list = new ArrayList<>();
        String requete = "select * from title";
        try {
            ste = cnx.createStatement();
            rs = ste.executeQuery(requete);
            while (rs.next()) {
                // Handle nullable price
                Integer price = rs.getObject("price") != null ? rs.getInt("price") : null;
                list.add(new Title(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("points_required"),
                        price
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public Title readById(int id) {
        String requete = "select * from title where id = ?";
        try {
            pst = cnx.prepareStatement(requete);
            pst.setInt(1, id);
            rs = pst.executeQuery();
            if (rs.next()) {
                // Handle nullable price
                Integer price = rs.getObject("price") != null ? rs.getInt("price") : null;
                return new Title(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("points_required"),
                        price
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}