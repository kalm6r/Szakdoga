package test;

import model.Category;
import model.Product;
import model.Subcategory;
import org.junit.jupiter.api.Test;
import service.InventoryService;

import java.util.List;

public class ListProductsTest {

    @Test
    void printProductsWithCategoryAndSubcategory() {
        InventoryService inv = new InventoryService();
        List<Product> products = inv.listAllProducts();

        System.out.println("ID | Product | Category | Subcategory");
        System.out.println("--------------------------------------");
        for (Product p : products) {
            Category c = p.getCategory();
            String cat = (c != null && c.getName() != null) ? c.getName() : "";
            Subcategory sc = (c != null) ? c.getSubcategory() : null;
            String subcat = (sc != null && sc.getName() != null) ? sc.getName() : "";
            System.out.printf("%d | %s | %s | %s%n",
                    p.getId(),
                    p.getName(),
                    cat,
                    subcat);
        }
    }
}
