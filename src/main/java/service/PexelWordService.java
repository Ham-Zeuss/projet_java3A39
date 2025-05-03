package service;

import entite.PexelWord;
import util.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PexelWordService implements IService<PexelWord> {

    private Connection cnx;
    private Statement ste;
    private PreparedStatement pst;
    private ResultSet rs;

    public PexelWordService() {
        cnx = DataSource.getInstance().getConnection();
    }

    @Override
    public void create(PexelWord pexelWord) {
        String requete = "insert into pexel_words (word, difficulty) " +
                "values('" + pexelWord.getWord() + "','" +
                pexelWord.getDifficulty() + "')";
        try {
            ste = cnx.createStatement();
            ste.executeUpdate(requete);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void createPst(PexelWord pexelWord) {
        String requete = "insert into pexel_words (word, difficulty) values (?, ?)";
        try {
            pst = cnx.prepareStatement(requete);
            pst.setString(1, pexelWord.getWord());
            pst.setString(2, pexelWord.getDifficulty());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(PexelWord pexelWord) {
        String requete = "delete from pexel_words where id = ?";
        try {
            pst = cnx.prepareStatement(requete);
            pst.setInt(1, pexelWord.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(PexelWord pexelWord) {
        String requete = "update pexel_words set word = ?, difficulty = ? where id = ?";
        try {
            pst = cnx.prepareStatement(requete);
            pst.setString(1, pexelWord.getWord());
            pst.setString(2, pexelWord.getDifficulty());
            pst.setInt(3, pexelWord.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<PexelWord> readAll() {
        List<PexelWord> list = new ArrayList<>();
        String requete = "select * from pexel_words";
        try {
            ste = cnx.createStatement();
            rs = ste.executeQuery(requete);
            while (rs.next()) {
                list.add(new PexelWord(
                        rs.getInt("id"),
                        rs.getString("word"),
                        rs.getString("difficulty")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public PexelWord readById(int id) {
        String requete = "select * from pexel_words where id = ?";
        try {
            pst = cnx.prepareStatement(requete);
            pst.setInt(1, id);
            rs = pst.executeQuery();
            if (rs.next()) {
                return new PexelWord(
                        rs.getInt("id"),
                        rs.getString("word"),
                        rs.getString("difficulty")
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public List<PexelWord> readByDifficulty(String difficulty) {
        List<PexelWord> list = new ArrayList<>();
        String requete = "select * from pexel_words where difficulty = ?";
        try {
            pst = cnx.prepareStatement(requete);
            pst.setString(1, difficulty);
            rs = pst.executeQuery();
            while (rs.next()) {
                list.add(new PexelWord(
                        rs.getInt("id"),
                        rs.getString("word"),
                        rs.getString("difficulty")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }
}