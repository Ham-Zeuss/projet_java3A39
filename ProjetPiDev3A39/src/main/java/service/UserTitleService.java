package service;

import entite.UserTitle;
import util.DataSource;

import java.sql.*;

public class UserTitleService {

    private Connection cnx;
    private PreparedStatement pst;
    private ResultSet rs;

    public UserTitleService() {
        cnx = DataSource.getInstance().getConnection();
    }

    public void create(UserTitle userTitle) {
        String requete = "INSERT INTO user_titles (user_id, title_id) VALUES (?, ?)";
        try {
            pst = cnx.prepareStatement(requete);
            pst.setInt(1, userTitle.getUserId());
            pst.setInt(2, userTitle.getTitleId());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean userOwnsTitle(int userId, int titleId) {
        String requete = "SELECT COUNT(*) FROM user_titles WHERE user_id = ? AND title_id = ?";
        try {
            pst = cnx.prepareStatement(requete);
            pst.setInt(1, userId);
            pst.setInt(2, titleId);
            rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }
}