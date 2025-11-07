package dao;
import dto.TopProduct;
import java.util.List;

public interface ReportDao {
    List<TopProduct> topBySellPrice(int limit);
    List<TopProduct> listByCategoryWithPrice(int categoryId);
    List<TopProduct> searchByKeywordWithPrice(String keyword);
}
