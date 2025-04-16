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
    private TitleService titleService; // To fetch Title objects

    public StoreItemService() {
        cnx = DataSource.getInstance().getConnection();
        titleService = new TitleService(); // Initialize TitleService to fetch Title objects
    }

    @Override
    public void create(StoreItem storeItem) {
        String requete = "insert into store_item (title_id, name, description, price, image, stock) " +
                "values(" + (storeItem.getTitle() != null ? storeItem.getTitle().getId() : "NULL") + ",'" +
                storeItem.getName() + "','" +
                (storeItem.getDescription() != null ? storeItem.getDescription() : "NULL") + "'," +
                storeItem.getPrice() + ",'" +
                (storeItem.getImage() != null ? storeItem.getImage() : "NULL") + "'," +
                storeItem.getStock() + ")";
        try {
            ste = cnx.createStatement();
            ste.executeUpdate(requete);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void createPst(StoreItem storeItem) {
        String requete = "insert into store_item (title_id, name, description, price, image, stock) values (?, ?, ?, ?, ?, ?)";
        try {
            pst = cnx.prepareStatement(requete);
            // Handle nullable title_id
            if (storeItem.getTitle() != null) {
                pst.setInt(1, storeItem.getTitle().getId());
            } else {
                pst.setNull(1, Types.INTEGER);
            }
            pst.setString(2, storeItem.getName());
            // Handle nullable description
            if (storeItem.getDescription() != null) {
                pst.setString(3, storeItem.getDescription());
            } else {
                pst.setNull(3, Types.LONGVARCHAR);
            }
            pst.setInt(4, storeItem.getPrice());
            // Handle nullable image
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

    @Override
    public void delete(StoreItem storeItem) {
        String requete = "delete from store_item where id = ?";
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
        String requete = "update store_item set title_id = ?, name = ?, description = ?, price = ?, image = ?, stock = ? where id = ?";
        try {
            pst = cnx.prepareStatement(requete);
            // Handle nullable title_id
            if (storeItem.getTitle() != null) {
                pst.setInt(1, storeItem.getTitle().getId());
            } else {
                pst.setNull(1, Types.INTEGER);
            }
            pst.setString(2, storeItem.getName());
            // Handle nullable description
            if (storeItem.getDescription() != null) {
                pst.setString(3, storeItem.getDescription());
            } else {
                pst.setNull(3, Types.LONGVARCHAR);
            }
            pst.setInt(4, storeItem.getPrice());
            // Handle nullable image
            if (storeItem.getImage() != null) {
                pst.setString(5, storeItem.getImage());
            } else {
                pst.setNull(5, Types.VARCHAR);
            }
            pst.setInt(6, storeItem.getStock());
            pst.setInt(7, storeItem.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<StoreItem> readAll() {
        List<StoreItem> list = new ArrayList<>();
        String requete = "select * from store_item";
        try {
            ste = cnx.createStatement();
            rs = ste.executeQuery(requete);
            while (rs.next()) {
                // Fetch the Title object using title_id
                Integer titleId = rs.getObject("title_id") != null ? rs.getInt("title_id") : null;
                Title title = titleId != null ? titleService.readById(titleId) : null;
                // Handle nullable fields
                String description = rs.getString("description");
                String image = rs.getString("image");
                list.add(new StoreItem(
                        rs.getInt("id"),
                        title,
                        rs.getString("name"),
                        description,
                        rs.getInt("price"),
                        image,
                        rs.getInt("stock")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public StoreItem readById(int id) {
        String requete = "select * from store_item where id = ?";
        try {
            pst = cnx.prepareStatement(requete);
            pst.setInt(1, id);
            rs = pst.executeQuery();
            if (rs.next()) {
                // Fetch the Title object using title_id
                Integer titleId = rs.getObject("title_id") != null ? rs.getInt("title_id") : null;
                Title title = titleId != null ? titleService.readById(titleId) : null;
                // Handle nullable fields
                String description = rs.getString("description");
                String image = rs.getString("image");
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
}