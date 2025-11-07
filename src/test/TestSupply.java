package test;

import dao.SupplyDao;
import dao.jdbc.JdbcSupplyDao;
import model.Supply;

import java.time.LocalDate;

public class TestSupply {
    public static void main(String[] args) {
        SupplyDao dao = new JdbcSupplyDao();

        int productId = 1001; // A DB-ben létező PRODUCT_ID-hez kapcsoljuk

        // 1) Beszúrás vagy frissítés
        Supply s1 = new Supply(productId, LocalDate.now(), 10, 400000, 480000);
        boolean ok1 = dao.upsert(s1);
        System.out.println("Upsert (INSERT/UPDATE): " + ok1);
        
        // 2) Lekérés
        dao.findByProductId(productId).ifPresentOrElse(
                s -> System.out.println("Lekért supply: " + s),
                () -> System.out.println("Nincs supply ehhez a productId-hoz")
        );

        // 3) Módosítás (ár + darabszám)
        Supply s2 = new Supply(productId, LocalDate.now(), 5, 420000, 520000);
        boolean ok2 = dao.upsert(s2);
        System.out.println("Upsert (UPDATE): " + ok2);

        // Ellenőrzés
        dao.findByProductId(productId).ifPresent(s -> 
                System.out.println("Frissített supply: " + s));

        // 4) Törlés
        boolean ok3 = dao.delete(productId);
        System.out.println("Delete: " + ok3);

        // Ellenőrzés
        dao.findByProductId(productId).ifPresentOrElse(
                s -> System.out.println("HIBA: még mindig van supply"),
                () -> System.out.println("OK: supply törölve")
        );
    }
}
