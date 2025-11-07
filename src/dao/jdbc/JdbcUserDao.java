package dao.jdbc;

import dao.UserDao;
import util.DBUtil;

import java.sql.*;
import java.util.Optional;

public class JdbcUserDao implements UserDao {

    private static final String AUTH_SQL =
            "SELECT USER_ID, USERNAME FROM USERS WHERE USERNAME = ? AND PASSWORD = ?";

    @Override
    public Optional<UserDao.UserRecord> authenticate(String username, String password) {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(AUTH_SQL)) {
            ps.setString(1, username);
            ps.setString(2, password); // HA HASH-ELSZ: itt a hash-elt értéket küldd!
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("USER_ID");      // ← oszlopnév igazítása!
                    String un = rs.getString("USERNAME");
                    return Optional.of(new UserDao.UserRecord(id, un));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Autentikáció sikertelen", e);
        }
        return Optional.empty();
    }
}
