package dao;
import model.Product;
import dto.ProductCardData;
import java.util.*;

public interface ProductDao {
    Optional<Product> findById(int id);
    List<Product> findAll();
    List<Product> findByCategory(int categoryId);
    int create(Product p);
    boolean update(Product p);
    boolean delete(int id);
}
