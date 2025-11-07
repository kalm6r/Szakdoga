package service;

import dao.ProductDao;
import dao.ReportDao;
import dao.SupplyDao;
import dao.jdbc.JdbcProductDao;
import dao.jdbc.JdbcReportDao;
import dao.jdbc.JdbcSupplyDao;
import dto.TopProduct;
import model.Product;
import model.Supply;

import java.util.List;
import java.util.Optional;

public class InventoryService {
    private final ProductDao productDao = new JdbcProductDao();
    private final SupplyDao supplyDao   = new JdbcSupplyDao();
    private final ReportDao reportDao   = new JdbcReportDao();

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
}
