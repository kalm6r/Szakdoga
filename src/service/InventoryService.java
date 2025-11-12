package service;

import dao.CategoryDao;
import dao.FavoriteDao;
import dao.ProductDao;
import dao.ReportDao;
import dao.SupplyDao;
import dao.SubcategoryDao;
import dao.jdbc.JdbcCategoryDao;
import dao.jdbc.JdbcFavoriteDao;
import dao.jdbc.JdbcProductDao;
import dao.jdbc.JdbcReportDao;
import dao.jdbc.JdbcSupplyDao;
import dao.jdbc.JdbcSubcategoryDao;
import dto.TopProduct;
import dto.CategoryOption;
import model.Product;
import model.Supply;
import model.Subcategory;
import java.util.Optional;

import java.util.List;
import java.util.Optional;

public class InventoryService {
    private final ProductDao productDao   = new JdbcProductDao();
    private final SupplyDao supplyDao     = new JdbcSupplyDao();
    private final ReportDao reportDao     = new JdbcReportDao();
    private final FavoriteDao favoriteDao = new JdbcFavoriteDao();
    private final CategoryDao categoryDao = new JdbcCategoryDao();
    private final SubcategoryDao subcategoryDao = new JdbcSubcategoryDao();
    
 // Kategória CRUD műveletek
    public Optional<CategoryOption> createCategory(String name) {
        return categoryDao.create(name);
    }
    
    public boolean updateCategory(int categoryId, String newName) {
        return categoryDao.update(categoryId, newName);
    }
    
    public boolean deleteCategory(int categoryId) {
        return categoryDao.delete(categoryId);
    }
    
    public Optional<CategoryOption> findCategoryById(int categoryId) {
        return categoryDao.findById(categoryId);
    }
    
    public Optional<CategoryOption> findCategoryByName(String name) {
        return categoryDao.findByName(name);
    }

    // Product műveletek
    public List<Product> listAllProducts() {
        return productDao.findAll();
    }

    public Optional<Product> findProductById(int id) {
        return productDao.findById(id);
    }

    public List<Product> filterByCategory(int categoryId) {
        return productDao.findByCategory(categoryId);
    }

    public int addProduct(Product p) {
        return productDao.create(p);
    }

    public boolean updateProduct(Product p) {
        return productDao.update(p);
    }

    public boolean deleteProduct(int productId) {
        return productDao.delete(productId);
    }
    
    public List<CategoryOption> listAllCategories() {
        return categoryDao.listOptions();
    }
    public List<Subcategory> findSubcategoriesByName(String name) {
        return subcategoryDao.findByName(name);
    }

    public Optional<Subcategory> findSubcategoryByNameAndManufacturer(String name, String manufacturerName) {
        return subcategoryDao.findByNameAndManufacturer(name, manufacturerName);
    }

    public Optional<Subcategory> createSubcategory(String name, String manufacturerName) {
        return subcategoryDao.create(name, manufacturerName);
    }

    // Supply műveletek
    public Optional<Supply> findSupplyByProductId(int productId) {
        return supplyDao.findByProductId(productId);
    }

    public boolean upsertSupply(Supply s) {
        return supplyDao.upsert(s);
    }

    public boolean deleteSupply(int productId) {
        return supplyDao.delete(productId);
    }

    public List<Supply> listAllSupply() {
        return supplyDao.findAll();
    }

    // Rangsor
    public List<TopProduct> topBySellPrice(int n) {
        return reportDao.topBySellPrice(n);
    }

    // Kedvencek
    public java.util.Set<Integer> listFavoriteProductIds(int userId) {
        return favoriteDao.findProductIdsByUser(userId);
    }

    public boolean addFavoriteProduct(int userId, int productId) {
        return favoriteDao.add(userId, productId);
    }

    public boolean removeFavoriteProduct(int userId, int productId) {
        return favoriteDao.remove(userId, productId);
    }
}