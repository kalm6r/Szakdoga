package dao.jdbc;

import dao.UserDao;
import util.DBUtil;
import util.PasswordUtil;

import java.sql.*;
import java.util.Optional;

public class JdbcUserDao implements UserDao {

    private static final String AUTH_SQL =
            "SELECT USER_ID, USERNAME, PASSWORD FROM USERS WHERE USERNAME = ?";
    
    private static final String UPDATE_PASSWORD_SQL =
            "UPDATE USERS SET PASSWORD = ? WHERE USER_ID = ?";

    @Override
    public Optional<UserDao.UserRecord> authenticate(String username, String password) {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(AUTH_SQL)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("USER_ID");
                    String un = rs.getString("USERNAME");
                    String storedPassword = rs.getString("PASSWORD");
                    
                    // Ellenőrizzük, hogy hash-elt vagy plaintext jelszó
                    boolean isValid = false;
                    
                    if (PasswordUtil.isPlaintext(storedPassword)) {
                        // BACKWARD COMPATIBILITY: Régi plaintext jelszó
                        // Ellenőrizzük, hogy egyezik-e
                        isValid = password.equals(storedPassword);
                        
                        // Ha sikerült, migráljuk hash-elt verzióra
                        if (isValid) {
                            migratePasswordToHashed(c, id, password);
                        }
                    } else {
                        // Hash-elt jelszó ellenőrzése
                        isValid = PasswordUtil.verifyPassword(password, storedPassword);
                    }
                    
                    if (isValid) {
                        return Optional.of(new UserDao.UserRecord(id, un));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Autentikáció sikertelen", e);
        }
        return Optional.empty();
    }
    
    /**
     * Migrálja a plaintext jelszót hash-elt verzióra
     */
    private void migratePasswordToHashed(Connection c, int userId, String plainPassword) {
        try (PreparedStatement ps = c.prepareStatement(UPDATE_PASSWORD_SQL)) {
            String hashedPassword = PasswordUtil.hashPassword(plainPassword);
            ps.setString(1, hashedPassword);
            ps.setInt(2, userId);
            ps.executeUpdate();
            System.out.println("✓ Jelszó migrálva hash-elt verzióra (User ID: " + userId + ")");
        } catch (SQLException e) {
            // Nem kritikus hiba, csak logoljuk
            System.err.println("Nem sikerült a jelszó migrálása: " + e.getMessage());
        }
    }
    
    /**
     * Új felhasználó létrehozása hash-elt jelszóval
     */
    public boolean createUser(String username, String email, String password, String role) {
        String sql = "INSERT INTO USERS (USER_ID, USERNAME, EMAIL, PASSWORD, CREATEDAT, ROLE) " +
                     "VALUES (?, ?, ?, ?, SYSDATE, ?)";
        
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            
            // Új user ID generálása
            int newUserId = getNextUserId(c);
            
            ps.setInt(1, newUserId);
            ps.setString(2, username);
            ps.setString(3, email);
            ps.setString(4, PasswordUtil.hashPassword(password));  // Hash-elt jelszó!
            ps.setString(5, role);
            
            return ps.executeUpdate() == 1;
            
        } catch (SQLException e) {
            throw new RuntimeException("Nem sikerült a felhasználó létrehozása", e);
        }
    }
    
    /**
     * Jelszó megváltoztatása
     */
    public boolean changePassword(int userId, String newPassword) {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(UPDATE_PASSWORD_SQL)) {
            
            ps.setString(1, PasswordUtil.hashPassword(newPassword));
            ps.setInt(2, userId);
            
            return ps.executeUpdate() == 1;
            
        } catch (SQLException e) {
            throw new RuntimeException("Nem sikerült a jelszó megváltoztatása", e);
        }
    }
    
    private int getNextUserId(Connection c) throws SQLException {
        String sql = "SELECT NVL(MAX(USER_ID), 0) + 1 AS NEXT_ID FROM USERS";
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("NEXT_ID");
            }
        }
        throw new SQLException("Nem sikerült új User ID-t generálni");
    }
}