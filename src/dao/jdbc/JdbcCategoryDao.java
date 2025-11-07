package dao.jdbc;

import dao.CategoryDao;
import dto.CategoryOption;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}
