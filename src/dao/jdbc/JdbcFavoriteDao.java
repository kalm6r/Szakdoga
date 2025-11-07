package dao.jdbc;

import dao.FavoriteDao;
import util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class JdbcFavoriteDao implements FavoriteDao {

    private static final String SELECT_BY_USER =
            "SELECT product_id FROM favorite WHERE user_id = ?";
    private static final String INSERT_SQL =
            "INSERT INTO favorite(user_id, product_id) VALUES(?, ?)";
    private static final String DELETE_SQL =
            "DELETE FROM favorite WHERE user_id = ? AND product_id = ?";

    @Override
    public Set<Integer> findProductIdsByUser(int userId) {
        Set<Integer> productIds = new HashSet<>();
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_BY_USER)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    productIds.add(rs.getInt("product_id"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Nem sikerült lekérdezni a kedvenceket", e);
        }
        return productIds;
    }

    @Override
    public boolean add(int userId, int productId) {
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(INSERT_SQL)) {
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            ps.executeUpdate();
            return true;
        } catch (SQLIntegrityConstraintViolationException ignored) {
            // már létezett a rekord – ez a felhasználó szempontjából sikernek számít
            return true;
        } catch (SQLException e) {
            throw new RuntimeException("Nem sikerült kedvencnek jelölni a terméket", e);
        }
    }

    @Override
    public boolean remove(int userId, int productId) {
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(DELETE_SQL)) {
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            throw new RuntimeException("Nem sikerült eltávolítani a terméket a kedvencek közül", e);
        }
    }
}
