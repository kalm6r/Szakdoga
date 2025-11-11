package dao.jdbc;

import dao.SubcategoryDao;
import model.Manufacturer;
import model.Subcategory;
import util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class JdbcSubcategoryDao implements SubcategoryDao {

    private Subcategory mapRow(ResultSet rs) throws SQLException {
        Manufacturer manufacturer = new Manufacturer(
                rs.getInt("manufacturer_id"),
                rs.getString("manufacturer_name"),
                rs.getString("model_name")
        );
        return new Subcategory(
                rs.getInt("subcat_id"),
                rs.getString("subcatname"),
                manufacturer
        );
    }

    @Override
    public List<Subcategory> findByName(String name) {
        List<Subcategory> results = new ArrayList<>();
        if (name == null) {
            return results;
        }

        String normalized = name.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            return results;
        }

        String sql = """
                SELECT s.subcat_id, s.subcatname,
                       m.manufacturer_id, m.manufacturer_name, m.model_name
                FROM subcategory s
                JOIN manufacturer m ON m.manufacturer_id = s.manufacturer_id
                WHERE LOWER(TRIM(s.subcatname)) = ?
                ORDER BY s.subcat_id
                """;

        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, normalized);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return results;
    }

    @Override
    public Optional<Subcategory> findByNameAndManufacturer(String name, String manufacturerName) {
        if (name == null || manufacturerName == null) {
            return Optional.empty();
        }

        String normalizedName = name.trim().toLowerCase(Locale.ROOT);
        String normalizedManufacturer = manufacturerName.trim().toLowerCase(Locale.ROOT);
        if (normalizedName.isEmpty() || normalizedManufacturer.isEmpty()) {
            return Optional.empty();
        }

        String sql = """
                SELECT s.subcat_id, s.subcatname,
                       m.manufacturer_id, m.manufacturer_name, m.model_name
                FROM subcategory s
                JOIN manufacturer m ON m.manufacturer_id = s.manufacturer_id
                WHERE LOWER(TRIM(s.subcatname)) = ?
                  AND LOWER(TRIM(m.manufacturer_name)) = ?
                """;

        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, normalizedName);
            ps.setString(2, normalizedManufacturer);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    @Override
    public Optional<Subcategory> create(String name, String manufacturerName) {
        if (name == null || manufacturerName == null) {
            return Optional.empty();
        }

        String trimmedName = name.trim();
        String trimmedManufacturer = manufacturerName.trim();
        if (trimmedName.isEmpty() || trimmedManufacturer.isEmpty()) {
            return Optional.empty();
        }

        String normalizedManufacturer = trimmedManufacturer.toLowerCase(Locale.ROOT);

        String manufacturerSql = """
                SELECT manufacturer_id, manufacturer_name, model_name
                FROM manufacturer
                WHERE LOWER(TRIM(manufacturer_name)) = ?
                """;

        try (Connection con = DBUtil.getConnection();
             PreparedStatement manufacturerPs = con.prepareStatement(manufacturerSql)) {
            manufacturerPs.setString(1, normalizedManufacturer);
            Manufacturer manufacturer;
            try (ResultSet rs = manufacturerPs.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                manufacturer = new Manufacturer(
                        rs.getInt("manufacturer_id"),
                        rs.getString("manufacturer_name"),
                        rs.getString("model_name")
                );
            }

            int newId = determineNextId(con);

            String insertSql = "INSERT INTO subcategory (subcat_id, subcatname, manufacturer_id) VALUES (?, ?, ?)";
            try (PreparedStatement insertPs = con.prepareStatement(insertSql)) {
                insertPs.setInt(1, newId);
                insertPs.setString(2, trimmedName);
                insertPs.setInt(3, manufacturer.getId());
                int rows = insertPs.executeUpdate();
                if (rows == 1) {
                    return Optional.of(new Subcategory(newId, trimmedName, manufacturer));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    private int determineNextId(Connection con) throws SQLException {
        String sql = "SELECT NVL(MAX(subcat_id), 0) + 1 AS next_id FROM subcategory";
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("next_id");
            }
        }
        throw new SQLException("Nem sikerült új alkategória azonosítót képezni.");
    }
}