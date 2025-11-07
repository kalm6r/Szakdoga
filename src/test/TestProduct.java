package test;

import dao.ProductDao;
import dao.jdbc.JdbcProductDao;
import model.Category;
import model.Manufacturer;
import model.Product;
import model.Subcategory;

import java.util.List;

public class TestProduct {
    public static void main(String[] args) {
        ProductDao dao = new JdbcProductDao();

        // 1) Összes termék
        System.out.println("== Összes termék ==");
        List<Product> all = dao.findAll();
        all.forEach(p -> System.out.println(fmt(p)));

        // 2) Keresés ID alapján (pl. 1001 – a minta INSERT-ből)
        System.out.println("\n== findById(1001) ==");
        dao.findById(1001).ifPresentOrElse(
                p -> System.out.println(fmt(p)),
                () -> System.out.println("Nem található a termék.")
        );

        // 3) Kategória szerinti lista (pl. 1 = Laptop a minta INSERT alapján)
        System.out.println("\n== findByCategory(1) ==");
        dao.findByCategory(1).forEach(p -> System.out.println(fmt(p)));
    }

    private static String fmt(Product p) {
        // Gyártó neve: manufacturer -> subcategory -> category -> product láncon át
        String cat = p.getCategory().getName();
        String man = p.getCategory().getSubcategory().getManufacturer().getName();
        return p.getName()
                + " | id=" + p.getId()
                + " | kategória=" + cat
                + " | gyártó=" + man
                + " | userId=" + p.getUserId();
    }
}
