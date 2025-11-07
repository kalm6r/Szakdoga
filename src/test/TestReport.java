package test;

import dao.ReportDao;
import dao.jdbc.JdbcReportDao;
import dto.TopProduct;

import java.util.List;

public class TestReport {
    public static void main(String[] args) {
        ReportDao dao = new JdbcReportDao();
        List<TopProduct> top = dao.topBySellPrice(10);

        System.out.println("== Top 10 eladási ár szerint ==");
        int rank = 1;
        for (TopProduct t : top) {
            System.out.printf("%2d. %-25s | Gyártó: %-12s | Kategória: %-12s | Ár: %d Ft%n",
                    rank++, t.getProductName(), t.getManufacturerName(), t.getCategoryName(), t.getSellPrice());
        }
    }
}
