package service;

import entite.StoreItem;
import entite.Title;
import util.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StoreItemService implements IService<StoreItem> {

    private Connection cnx;
    private Statement ste;
    private PreparedStatement pst;
    private ResultSet rs;

    public StoreItemService() {
        cnx = DataSource.getInstance().getConnection();
    }

    @Override
    public void create(StoreItem storeItem) {
        String requete = "INSERT INTO store_item (title_id, name, description, price, image, stock) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            pst = cnx.prepareStatement(requete);
            if (storeItem.getTitle() != null && storeItem.getTitle().getId() != 0) {
                pst.setInt(1, storeItem.getTitle().getId());
            } else {
                pst.setNull(1, Types.INTEGER);
            }
            pst.setString(2, storeItem.getName());
            if (storeItem.getDescription() != null) {
                pst.setString(3, storeItem.getDescription());
            } else {
                pst.setNull(3, Types.LONGVARCHAR);
            }
            pst.setInt(4, storeItem.getPrice());
            if (storeItem.getImage() != null) {
                pst.setString(5, storeItem.getImage());
            } else {
                pst.setNull(5, Types.VARCHAR);
            }
            pst.setInt(6, storeItem.getStock());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void createPst(StoreItem storeItem) {
        // Kept for compatibility, same as create
        create(storeItem);
    }

    @Override
    public void delete(StoreItem storeItem) {
        String requete = "DELETE FROM store_item WHERE id = ?";
        try {
            pst = cnx.prepareStatement(requete);
            pst.setInt(1, storeItem.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(StoreItem storeItem) {
        String requete = "UPDATE store_item SET name = ?, description = ?, price = ?, image = ?, stock = ? WHERE id = ?";
        try {
            pst = cnx.prepareStatement(requete);
            pst.setString(1, storeItem.getName());
            if (storeItem.getDescription() != null) {
                pst.setString(2, storeItem.getDescription());
            } else {
                pst.setNull(2, Types.LONGVARCHAR);
            }
            pst.setInt(3, storeItem.getPrice());
            if (storeItem.getImage() != null) {
                pst.setString(4, storeItem.getImage());
            } else {
                pst.setNull(4, Types.VARCHAR);
            }
            pst.setInt(5, storeItem.getStock());
            pst.setInt(6, storeItem.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<StoreItem> readAll() {
        List<StoreItem> list = new ArrayList<>();
        String requete = "SELECT * FROM store_item";
        try {
            ste = cnx.createStatement();
            rs = ste.executeQuery(requete);
            while (rs.next()) {
                String description = rs.getString("description");
                String image = rs.getString("image");
                int titleId = rs.getInt("title_id");
                Title title = titleId != 0 ? new Title(titleId, "", 0, null) : null; // Minimal Title object
                StoreItem item = new StoreItem(
                        rs.getInt("id"),
                        title,
                        rs.getString("name"),
                        description,
                        rs.getInt("price"),
                        image,
                        rs.getInt("stock")
                );
                list.add(item);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public StoreItem readById(int id) {
        String requete = "SELECT * FROM store_item WHERE id = ?";
        try {
            pst = cnx.prepareStatement(requete);
            pst.setInt(1, id);
            rs = pst.executeQuery();
            if (rs.next()) {
                String description = rs.getString("description");
                String image = rs.getString("image");
                int titleId = rs.getInt("title_id");
                Title title = titleId != 0 ? new Title(titleId, "", 0, null) : null;
                return new StoreItem(
                        rs.getInt("id"),
                        title,
                        rs.getString("name"),
                        description,
                        rs.getInt("price"),
                        image,
                        rs.getInt("stock")
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public StoreItem findByTitleId(int titleId) {
        String requete = "SELECT * FROM store_item WHERE title_id = ?";
        try {
            pst = cnx.prepareStatement(requete);
            pst.setInt(1, titleId);
            rs = pst.executeQuery();
            if (rs.next()) {
                String description = rs.getString("description");
                String image = rs.getString("image");
                return new StoreItem(
                        rs.getInt("id"),
                        new Title(titleId, "", 0, null),
                        rs.getString("name"),
                        description,
                        rs.getInt("price"),
                        image,
                        rs.getInt("stock")
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}