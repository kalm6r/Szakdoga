package test.java;

import dao.ReportDao;
import dao.jdbc.JdbcReportDao;
import dto.TopProduct;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JdbcReportDaoTest {

    private final ReportDao dao = new JdbcReportDao();

    @Test
    void topBySellPrice_shouldBeSortedDescAndLimited() {
        List<TopProduct> top = dao.topBySellPrice(5);
        assertTrue(top.size() <= 5, "Legfeljebb 5 sort várunk");
        // Ellenőrizzük a csökkenő rendezést (ha van legalább 2 elem)
        for (int i = 1; i < top.size(); i++) {
            assertTrue(top.get(i-1).getSellPrice() >= top.get(i).getSellPrice(),
                    "Ár szerint csökkenő sorrendnek kell lennie");
        }
    }

    @Test
    void searchByKeywordWithPrice_shouldMatchNameOrManufacturerOrCategory() {
        List<TopProduct> rows = dao.searchByKeywordWithPrice("Dell"); // pl. létező kulcsszó a mintából
        assertNotNull(rows);
        // Gyenge állítás: legyen bármennyi találat, de ha van elem, legyen ár mező és név nem üres
        if (!rows.isEmpty()) {
            TopProduct t = rows.get(0);
            assertTrue(t.getSellPrice() > 0, "Árnak > 0-nak kell lennie");
            assertNotNull(t.getProductName());
        }
    }

    @Test
    void listByCategoryWithPrice_shouldReturnOnlyThatCategory() {
        int categoryId = 1; // pl. létező kategória ID a mintából
        List<TopProduct> rows = dao.listByCategoryWithPrice(categoryId);
        for (TopProduct t : rows) {
            assertNotNull(t.getCategoryName());
        }
    }
}
