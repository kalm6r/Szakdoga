package test;

import dto.TopProduct;
import model.Product;
import model.Supply;
import service.InventoryService;

import java.time.LocalDate;

public class TestService {
    public static void main(String[] args) {
        InventoryService service = new InventoryService();

        // 1) Listázás
        System.out.println("== Minden termék ==");
        for (Product p : service.listAllProducts()) {
            System.out.println("- " + p.getName() + " (id=" + p.getId() + ")");
        }

        // 2) Supply beszúrás/frissítés
        Supply s = new Supply(1001, LocalDate.now(), 7, 450000, 500000);
        service.upsertSupply(s);

        // 3) Supply lekérdezés
        service.findSupplyByProductId(1001).ifPresent(sup ->
                System.out.println("Frissített ár: " + sup.getSellPrice() + " Ft")
        );

        // 4) Top 5 riport
        System.out.println("\n== Top 5 termék ár szerint ==");
        for (TopProduct tp : service.topBySellPrice(5)) {
            System.out.println(tp.getProductName() + " | " + tp.getSellPrice() + " Ft");
        }
    }
}
