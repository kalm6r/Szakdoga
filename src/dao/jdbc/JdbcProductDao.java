package dao.jdbc;

import dao.ProductDao;
import model.*;
import util.DBUtil;

import java.sql.*;
import java.util.*;

public class JdbcProductDao implements ProductDao {

    private Product mapRow(ResultSet rs) throws SQLException {
        Manufacturer man = new Manufacturer(
                rs.getInt("manufacturer_id"),
                rs.getString("manufacturer_name"),
                rs.getString("model_name")
        );
        Subcategory sub = new Subcategory(
                rs.getInt("subcat_id"),
                rs.getString("subcatname"),
                man
        );
        Category cat = new Category(
                rs.getInt("category_id"),
                rs.getString("categoryname"),
                sub
        );
        Product p = new Product(
                rs.getInt("product_id"),
                rs.getString("productname"),
                rs.getInt("user_id"),
                cat,
                rs.getString("image_url")
        );

        return p;
    }

    @Override public Optional<Product> findById(int id) {
        String sql = """
            SELECT p.product_id, p.productname, p.user_id, p.image_url,
                   c.category_id, c.categoryname,
                   sC.subcat_id, sC.subcatname,
                   m.manufacturer_id, m.manufacturer_name, m.model_name
            FROM product p
            JOIN category c     ON c.category_id = p.category_id
            JOIN subcategory sC ON sC.subcat_id = c.subcat_id
            JOIN manufacturer m ON m.manufacturer_id = sC.manufacturer_id
            WHERE p.product_id = ?
        """;
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return Optional.empty();
    }

    @Override public List<Product> findAll() {
        String sql = """
            SELECT p.product_id, p.productname, p.user_id, p.image_url,
                   c.category_id, c.categoryname,
                   sC.subcat_id, sC.subcatname,
                   m.manufacturer_id, m.manufacturer_name, m.model_name
            FROM product p
            JOIN category c     ON c.category_id = p.category_id
            JOIN subcategory sC ON sC.subcat_id = c.subcat_id
            JOIN manufacturer m ON m.manufacturer_id = sC.manufacturer_id
            ORDER BY p.product_id
        """;
        List<Product> list = new ArrayList<>();
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    @Override public List<Product> findByCategory(int categoryId) {
        String sql = """
            SELECT p.product_id, p.productname, p.user_id, p.image_url,
                   c.category_id, c.categoryname,
                   sC.subcat_id, sC.subcatname,
                   m.manufacturer_id, m.manufacturer_name, m.model_name
            FROM product p
            JOIN category c     ON c.category_id = p.category_id
            JOIN subcategory sC ON sC.subcat_id = c.subcat_id
            JOIN manufacturer m ON m.manufacturer_id = sC.manufacturer_id
            WHERE c.category_id = ?
            ORDER BY p.productname
        """;
        List<Product> list = new ArrayList<>();
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    @Override public int create(Product p) {
        String sql = "INSERT INTO product(product_id, user_id, category_id, productname, image_url) VALUES(?, ?, ?, ?, ?)";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, p.getId());
            ps.setInt(2, p.getUserId());
            ps.setInt(3, p.getCategory().getId());
            ps.setString(4, p.getName());
            ps.setString(5, p.getImageUrl());
            return ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    @Override public boolean update(Product p) {
        String sql = "UPDATE product SET user_id=?, category_id=?, productname=?, image_url=? WHERE product_id=?";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, p.getUserId());
            ps.setInt(2, p.getCategory().getId());
            ps.setString(3, p.getName());
            ps.setString(4, p.getImageUrl());
            ps.setInt(5, p.getId());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    @Override public boolean delete(int id) {
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM product WHERE product_id=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }
}
