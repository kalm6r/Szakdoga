package dao.jdbc;

import dao.SupplyDao;
import model.Supply;
import util.DBUtil;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcSupplyDao implements SupplyDao {

    private Supply mapRow(ResultSet rs) throws SQLException {
        return new Supply(
                rs.getInt("product_id"),
                rs.getDate("bought").toLocalDate(),
                rs.getInt("pieces"),
                rs.getInt("purchase_price"),
                rs.getInt("sell_price")
        );
    }

    @Override
    public Optional<Supply> findByProductId(int productId) {
        String sql = "SELECT * FROM supply WHERE product_id = ?";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public boolean upsert(Supply s) {
        // 1. update próba
        String upd = """
            UPDATE supply
            SET bought=?, pieces=?, purchase_price=?, sell_price=?
            WHERE product_id=?
        """;
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(upd)) {
            ps.setDate(1, Date.valueOf(s.getBought()));
            ps.setInt(2, s.getPieces());
            ps.setInt(3, s.getPurchasePrice());
            ps.setInt(4, s.getSellPrice());
            ps.setInt(5, s.getProductId());
            int n = ps.executeUpdate();
            if (n == 1) return true; // sikeres update
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // 2. Ha nem volt update, insert próba
        String ins = """
            INSERT INTO supply(product_id, bought, pieces, purchase_price, sell_price)
            VALUES(?,?,?,?,?)
        """;
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(ins)) {
            ps.setInt(1, s.getProductId());
            ps.setDate(2, Date.valueOf(s.getBought()));
            ps.setInt(3, s.getPieces());
            ps.setInt(4, s.getPurchasePrice());
            ps.setInt(5, s.getSellPrice());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(int productId) {
        String sql = "DELETE FROM supply WHERE product_id=?";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, productId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<Supply> findAll() {
        String sql = "SELECT * FROM supply ORDER BY product_id";
        List<Supply> list = new ArrayList<>();
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
