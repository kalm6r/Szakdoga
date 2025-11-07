package dao.jdbc;

import dao.ReportDao;
import dto.TopProduct;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcReportDao implements ReportDao {
    @Override
    public List<TopProduct> topBySellPrice(int limit) {
        String sql = """
            SELECT * FROM (
              SELECT p.product_id, p.productname, s.sell_price,
                     m.manufacturer_name, c.categoryname
              FROM supply s
              JOIN product p     ON p.product_id = s.product_id
              JOIN category c    ON c.category_id = p.category_id
              JOIN subcategory sc ON sc.subcat_id = c.subcat_id
              JOIN manufacturer m ON m.manufacturer_id = sc.manufacturer_id
              ORDER BY s.sell_price DESC
            ) WHERE ROWNUM <= ?
        """;
        List<TopProduct> list = new ArrayList<>();
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new TopProduct(
                        rs.getInt("product_id"),
                        rs.getString("productname"),
                        rs.getString("manufacturer_name"),
                        rs.getString("categoryname"),
                        rs.getInt("sell_price")
                    ));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
    
    @Override
    public List<TopProduct> listByCategoryWithPrice(int categoryId) {
        String sql = """
            SELECT p.product_id, p.productname, s.sell_price,
                   m.manufacturer_name, c.categoryname
            FROM supply s
            JOIN product p      ON p.product_id = s.product_id
            JOIN category c     ON c.category_id = p.category_id
            JOIN subcategory sc ON sc.subcat_id = c.subcat_id
            JOIN manufacturer m ON m.manufacturer_id = sc.manufacturer_id
            WHERE c.category_id = ?
            ORDER BY s.sell_price DESC
        """;
        List<TopProduct> list = new ArrayList<>();
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new TopProduct(
                        rs.getInt("product_id"),
                        rs.getString("productname"),
                        rs.getString("manufacturer_name"),
                        rs.getString("categoryname"),
                        rs.getInt("sell_price")
                    ));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    @Override
    public List<TopProduct> searchByKeywordWithPrice(String keyword) {
        String sql = """
            SELECT p.product_id, p.productname, s.sell_price,
                   m.manufacturer_name, c.categoryname
            FROM supply s
            JOIN product p      ON p.product_id = s.product_id
            JOIN category c     ON c.category_id = p.category_id
            JOIN subcategory sc ON sc.subcat_id = c.subcat_id
            JOIN manufacturer m ON m.manufacturer_id = sc.manufacturer_id
            WHERE UPPER(p.productname)      LIKE ?
               OR UPPER(m.manufacturer_name) LIKE ?
               OR UPPER(c.categoryname)      LIKE ?
            ORDER BY s.sell_price DESC
        """;
        List<TopProduct> list = new ArrayList<>();
        String pat = "%" + (keyword == null ? "" : keyword.trim().toUpperCase()) + "%";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, pat);
            ps.setString(2, pat);
            ps.setString(3, pat);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new TopProduct(
                        rs.getInt("product_id"),
                        rs.getString("productname"),
                        rs.getString("manufacturer_name"),
                        rs.getString("categoryname"),
                        rs.getInt("sell_price")
                    ));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

}
