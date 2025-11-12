package dao.jdbc;

import dao.CategoryDao;
import dto.CategoryOption;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class JdbcCategoryDao implements CategoryDao {
    
    @Override
    public List<CategoryOption> listOptions() {
        String sql = "SELECT category_id, categoryname FROM category ORDER BY categoryname";
        List<CategoryOption> list = new ArrayList<>();
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new CategoryOption(
                        rs.getInt("category_id"),
                        rs.getString("categoryname")
                ));
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return list;
    }
    
    @Override
    public Optional<CategoryOption> create(String name) {
        if (name == null || name.trim().isEmpty()) {
            return Optional.empty();
        }
        
        String trimmedName = name.trim();
        
        // Ellenőrizzük, hogy már létezik-e
        if (findByName(trimmedName).isPresent()) {
            throw new IllegalArgumentException("Ez a kategória már létezik: " + trimmedName);
        }
        
        String sql = "INSERT INTO category (category_id, categoryname) VALUES (?, ?)";
        
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            int newId = getNextCategoryId(con);
            ps.setInt(1, newId);
            ps.setString(2, trimmedName);
            
            int rows = ps.executeUpdate();
            if (rows == 1) {
                return Optional.of(new CategoryOption(newId, trimmedName));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return Optional.empty();
    }
    
    @Override
    public boolean update(int categoryId, String newName) {
        if (newName == null || newName.trim().isEmpty()) {
            return false;
        }
        
        String trimmedName = newName.trim();
        
        // Ellenőrizzük, hogy a kategória létezik-e
        if (findById(categoryId).isEmpty()) {
            return false;
        }
        
        // Ellenőrizzük, hogy már létezik-e másik kategória ezzel a névvel
        Optional<CategoryOption> existing = findByName(trimmedName);
        if (existing.isPresent() && existing.get().getId() != categoryId) {
            throw new IllegalArgumentException("Már létezik kategória ezzel a névvel: " + trimmedName);
        }
        
        String sql = "UPDATE category SET categoryname = ? WHERE category_id = ?";
        
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, trimmedName);
            ps.setInt(2, categoryId);
            
            return ps.executeUpdate() == 1;
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    @Override
    public boolean delete(int categoryId) {
        // Ellenőrizzük, hogy van-e termék ebben a kategóriában
        if (hasProducts(categoryId)) {
            throw new IllegalArgumentException(
                "Nem törölhető a kategória, mert van hozzá tartozó termék. " +
                "Először töröld vagy helyezd át a termékeket.");
        }
        
        // Ellenőrizzük, hogy van-e alkategória ebben a kategóriában
        if (hasSubcategories(categoryId)) {
            throw new IllegalArgumentException(
                "Nem törölhető a kategória, mert van hozzá tartozó alkategória. " +
                "Először töröld az alkategóriákat.");
        }
        
        String sql = "DELETE FROM category WHERE category_id = ?";
        
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, categoryId);
            return ps.executeUpdate() == 1;
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    @Override
    public Optional<CategoryOption> findById(int categoryId) {
        String sql = "SELECT category_id, categoryname FROM category WHERE category_id = ?";
        
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new CategoryOption(
                        rs.getInt("category_id"),
                        rs.getString("categoryname")
                    ));
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return Optional.empty();
    }
    
    @Override
    public Optional<CategoryOption> findByName(String name) {
        if (name == null) {
            return Optional.empty();
        }
        
        String normalized = name.trim().toLowerCase(Locale.ROOT);
        String sql = "SELECT category_id, categoryname FROM category " +
                     "WHERE LOWER(TRIM(categoryname)) = ?";
        
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, normalized);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new CategoryOption(
                        rs.getInt("category_id"),
                        rs.getString("categoryname")
                    ));
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return Optional.empty();
    }
    
    /**
     * Ellenőrzi, hogy van-e termék a kategóriában
     */
    private boolean hasProducts(int categoryId) {
        String sql = "SELECT COUNT(*) FROM product WHERE category_id = ?";
        
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Ellenőrzi, hogy van-e alkategória a kategóriában
     */
    private boolean hasSubcategories(int categoryId) {
        String sql = "SELECT COUNT(*) FROM subcategory WHERE category_id = ?";
        
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Következő szabad kategória ID meghatározása
     */
    private int getNextCategoryId(Connection con) throws SQLException {
        String sql = "SELECT NVL(MAX(category_id), 0) + 1 AS next_id FROM category";
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("next_id");
            }
        }
        throw new SQLException("Nem sikerült új kategória azonosítót képezni.");
    }
}