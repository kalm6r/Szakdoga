package util;

import model.*;
import service.InventoryService;

import java.sql.*;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class SeedData {

    // --- Előre kiosztott, változatos dátumok (2023–2025, sok hónap) ---
    private static final List<LocalDate> DATES = Arrays.asList(
            // 2023
            LocalDate.of(2023, 1, 12), LocalDate.of(2023, 2, 23), LocalDate.of(2023, 3, 7),
            LocalDate.of(2023, 4, 18), LocalDate.of(2023, 5, 29), LocalDate.of(2023, 6, 11),
            LocalDate.of(2023, 7, 22), LocalDate.of(2023, 8, 5), LocalDate.of(2023, 9, 16),
            LocalDate.of(2023, 10, 3), LocalDate.of(2023, 11, 14), LocalDate.of(2023, 12, 27),
            // 2024
            LocalDate.of(2024, 1, 9), LocalDate.of(2024, 2, 21), LocalDate.of(2024, 3, 5),
            LocalDate.of(2024, 4, 17), LocalDate.of(2024, 5, 28), LocalDate.of(2024, 6, 10),
            LocalDate.of(2024, 7, 19), LocalDate.of(2024, 8, 2), LocalDate.of(2024, 9, 13),
            LocalDate.of(2024, 10, 25), LocalDate.of(2024, 11, 6), LocalDate.of(2024, 12, 18),
            // 2025
            LocalDate.of(2025, 1, 8), LocalDate.of(2025, 2, 12), LocalDate.of(2025, 3, 24),
            LocalDate.of(2025, 4, 4), LocalDate.of(2025, 5, 15), LocalDate.of(2025, 6, 27),
            LocalDate.of(2025, 7, 9), LocalDate.of(2025, 8, 20), LocalDate.of(2025, 9, 1),
            LocalDate.of(2025, 10, 14), LocalDate.of(2025, 11, 3)
    );
    private static int dateIdx = 0;

    private static LocalDate nextDate() {
        LocalDate d = DATES.get(dateIdx % DATES.size());
        dateIdx++;
        return d;
    }

    public static void main(String[] args) throws Exception {
        // 0) TÖRLÉS – FK-sorrendben
        purgeAll();

        // 1) Felhasználó(ka)t seedelünk – hogy legyen USER_ID = 1
        ensureUsers();

        // 2) Gyártók
        List<Manufacturer> mans = Arrays.asList(
                new Manufacturer(101, "Logitech", "MX Master"),
                new Manufacturer(102, "HP", "Pavilion"),
                new Manufacturer(103, "Dell", "Inspiron"),
                new Manufacturer(104, "Samsung", "Odyssey"),
                new Manufacturer(105, "Kingston", "NV2"),
                new Manufacturer(106, "Razer", "Kraken"),
                new Manufacturer(107, "ASUS", "ZenBook"),
                new Manufacturer(108, "Lenovo", "ThinkPad"),
                new Manufacturer(109, "Acer", "Aspire"),
                new Manufacturer(110, "LG", "UltraGear"),
                new Manufacturer(111, "Corsair", "K-series"),
                new Manufacturer(112, "SteelSeries", "Arctis"),
                new Manufacturer(113, "Apple", "MacBook"),
                new Manufacturer(114, "MSI", "Katana"),
                new Manufacturer(115, "Western Digital", "Blue"),
                new Manufacturer(116, "Crucial", "MX500"),
                new Manufacturer(117, "Gigabyte", "AORUS"),
                new Manufacturer(118, "TP-Link", "Archer"),
                new Manufacturer(119, "Seagate", "Barracuda")
        );
        ensureManufacturers(mans);

        // 3) Kategóriák (1 név = 1 kategória)
        ensureCategories();

        // 4) Subcategory-k (gyártóhoz + kategóriához kötve)
        List<Subcategory> subs = Arrays.asList(
                new Subcategory(211, "Egér", mans.get(0)),         // Logitech
                new Subcategory(212, "Billentyűzet", mans.get(0)), // Logitech
                new Subcategory(213, "Headset", mans.get(5)),      // Razer
                new Subcategory(221, "Notebook", mans.get(2)),     // Dell
                new Subcategory(222, "Notebook", mans.get(1)),     // HP
                new Subcategory(231, "Monitor", mans.get(3)),      // Samsung
                new Subcategory(241, "SSD", mans.get(4)),          // Kingston

                new Subcategory(252, "Notebook", mans.get(6)),     // ASUS
                new Subcategory(253, "Notebook", mans.get(7)),     // Lenovo
                new Subcategory(254, "Notebook", mans.get(8)),     // Acer
                new Subcategory(255, "Notebook", mans.get(12)),    // Apple
                new Subcategory(256, "Notebook", mans.get(13)),    // MSI
                new Subcategory(261, "Monitor", mans.get(9)),      // LG
                new Subcategory(262, "Billentyűzet", mans.get(10)),// Corsair
                new Subcategory(263, "Headset", mans.get(11)),     // SteelSeries
                new Subcategory(271, "SSD", mans.get(14)),         // WD
                new Subcategory(272, "SSD", mans.get(15)),         // Crucial

                new Subcategory(281, "Videókártya", mans.get(16)), // Gigabyte
                new Subcategory(282, "Router", mans.get(17)),      // TP-Link
                new Subcategory(283, "HDD", mans.get(18)),         // Seagate
                new Subcategory(284, "Memória", mans.get(10)),     // Corsair
                new Subcategory(285, "Memória", mans.get(4)),      // Kingston
                new Subcategory(286, "Tablet", mans.get(12)),      // Apple
                new Subcategory(287, "Tablet", mans.get(3)),       // Samsung
                new Subcategory(288, "Telefon", mans.get(3))       // Samsung
        );
        ensureSubcategories(subs);

        // 5) Termékek + Supply + IMAGE_URL – kategória ID-k az új CATEGORY-hez igazítva
        InventoryService inv = new InventoryService();

     // Periféria (1) – Logitech / Razer
        add(inv, 1001, "Logitech MX Master 3S",
                1, 1, 211, 30, 22000, 32990, "classpath:/images/mxmaster3s.jpg", nextDate()); // Egér / Logitech

        add(inv, 1002, "Logitech K380",
                1, 1, 212, 20, 12000, 16990, "classpath:/images/k380.jpg", nextDate());       // Billentyűzet / Logitech

        add(inv, 1003, "Razer Kraken V3",
                1, 1, 213, 12, 24000, 34990, "classpath:/images/krakenv3.jpg", nextDate());   // Headset / Razer

        // Laptop (2)
        add(inv, 1004, "Dell Inspiron 14",
                1, 2, 221, 15, 220000, 259000, "classpath:/images/inspiron14.jpg", nextDate()); // Notebook / Dell

        add(inv, 1005, "HP Pavilion 15",
                1, 2, 222, 10, 210000, 249000, "classpath:/images/pavilion15.jpg", nextDate()); // Notebook / HP

        // Monitor (3)
        add(inv, 1006, "Samsung 27\" Monitor",
                1, 3, 231, 12, 65000, 79990, "classpath:/images/27monitor.jpg", nextDate());    // Monitor / Samsung

        // SSD (4)
        add(inv, 1007, "Kingston NV2 1TB SSD",
                1, 4, 241, 25, 21000, 27990, "classpath:/images/nv2ssd.jpg", nextDate());      // SSD / Kingston

        // Perifériák (1)
        add(inv, 1010, "Logitech M185",
                1, 1, 211, 40, 5000, 6990, "classpath:/images/m185.jpg", nextDate());          // Egér / Logitech

        add(inv, 1011, "Logitech G502 Hero",
                1, 1, 211, 18, 15000, 21990, "classpath:/images/g52hero.jpg", nextDate());     // Egér / Logitech

        add(inv, 1012, "Corsair K70 RGB MK.2",
                1, 1, 262, 12, 32000, 44990, "classpath:/images/corsairk70.jpg", nextDate());  // Billentyűzet / Corsair

        add(inv, 1013, "SteelSeries Arctis 7",
                1, 1, 263, 10, 38000, 54990, "classpath:/images/arctis7.jpg", nextDate());     // Headset / SteelSeries

        add(inv, 1014, "Logitech MX Keys",
                1, 1, 212, 22, 29000, 39990, "classpath:/images/keys.jpg", nextDate());        // Billentyűzet / Logitech

        // Laptopok (2)
        add(inv, 1015, "ASUS ZenBook 14",
                1, 2, 252, 14, 220000, 279000, "classpath:/images/zenbook14.jpg", nextDate()); // Notebook / ASUS

        add(inv, 1016, "Lenovo ThinkPad T14",
                1, 2, 253, 9, 240000, 299000, "classpath:/images/thinkpadt14.jpg", nextDate()); // Notebook / Lenovo

        add(inv, 1017, "Acer Aspire 5",
                1, 2, 254, 11, 180000, 229000, "classpath:/images/aspire5.jpg", nextDate());   // Notebook / Acer

        add(inv, 1018, "Apple MacBook Air 13",
                1, 2, 255, 7, 380000, 449000, "classpath:/images/air13.jpg", nextDate());      // Notebook / Apple

        add(inv, 1019, "MSI Katana 15",
                1, 2, 256, 8, 300000, 359000, "classpath:/images/katana15.jpg", nextDate());   // Notebook / MSI

        add(inv, 1020, "Lenovo IdeaPad 5",
                1, 2, 253, 13, 190000, 239000, "classpath:/images/ideapad5.jpg", nextDate());  // Notebook / Lenovo

        // Monitorok (3)
        add(inv, 1021, "Samsung Odyssey G5 32\"",
                1, 3, 231, 6, 110000, 139990, "classpath:/images/g5.jpg", nextDate());         // Monitor / Samsung

        add(inv, 1022, "LG UltraGear 27GL850",
                1, 3, 261, 5, 120000, 149990, "classpath:/images/27gl.jpg", nextDate());       // Monitor / LG

        add(inv, 1023, "LG 27MP400-B",
                1, 3, 261, 16, 60000, 79990, "classpath:/images/27mp.jpg", nextDate());        // Monitor / LG

        // SSD-k (4)
        add(inv, 1024, "WD Blue 1TB SSD",
                1, 4, 271, 20, 19000, 25990, "classpath:/images/m2.jpg", nextDate());          // SSD / WD

        add(inv, 1025, "Crucial MX500 1TB",
                1, 4, 272, 18, 20000, 27990, "classpath:/images/mx500.jpg", nextDate());       // SSD / Crucial

        add(inv, 1026, "Kingston NV2 2TB SSD",
                1, 4, 241, 12, 38000, 49990, "classpath:/images/nv2.jpg", nextDate());         // SSD / Kingston

        // Videókártya (5)
        add(inv, 1030, "Gigabyte GeForce RTX 4060",
                1, 5, 281, 7, 120000, 159900, "classpath:/images/rtx4060.jpg", nextDate());    // Videókártya / Gigabyte

        // Hálózat (6)
        add(inv, 1031, "TP-Link Archer AX55 Router",
                1, 6, 282, 15, 24000, 32990, "classpath:/images/ax55.jpg", nextDate());        // Router / TP-Link

        // HDD (7)
        add(inv, 1032, "Seagate Barracuda 2TB HDD",
                1, 7, 283, 20, 18000, 24990, "classpath:/images/barra.jpg", nextDate());       // HDD / Seagate

        // Memória (8)
        add(inv, 1033, "Corsair Vengeance 16GB DDR4",
                1, 8, 284, 25, 16000, 21990, "classpath:/images/veng.jpg", nextDate());        // Memória / Corsair

        add(inv, 1034, "Kingston FURY Beast 16GB DDR5",
                1, 8, 285, 22, 24000, 32990, "classpath:/images/beast.jpg", nextDate());       // Memória / Kingston

        // Tablet (9)
        add(inv, 1035, "Apple iPad Air 10.9\"",
                1, 9, 286, 6, 220000, 279990, "classpath:/images/air10.jpg", nextDate());      // Tablet / Apple

        add(inv, 1036, "Samsung Galaxy Tab S9",
                1, 9, 287, 5, 260000, 319990, "classpath:/images/s9.jpg", nextDate());         // Tablet / Samsung

        // Telefon (10)
        add(inv, 1037, "Samsung Galaxy S24",
                1, 10, 288, 8, 280000, 349990, "classpath:/images/s24.jpg", nextDate());       // Telefon / Samsung


        System.out.println("Kész: bővített seed az új sémához.");
    }

    // ====== TÖRLÉS – FK-sorrendben ======
    private static void purgeAll() throws SQLException {
        try (Connection c = DBUtil.getConnection()) {
            c.setAutoCommit(false);
            // FK: FAVORITE -> (USERS, PRODUCT)
            exec(c, "DELETE FROM favorite");
            // FK: SUPPLY -> PRODUCT
            exec(c, "DELETE FROM supply");
            // FK: PRODUCT -> (USERS, CATEGORY)
            exec(c, "DELETE FROM product");
            // FK: SUBCATEGORY -> (MANUFACTURER, CATEGORY)
            exec(c, "DELETE FROM subcategory");
            exec(c, "DELETE FROM category");
            exec(c, "DELETE FROM manufacturer");
            exec(c, "DELETE FROM users");
            c.commit();
        }
    }

    private static void exec(Connection c, String sql) throws SQLException {
        try (Statement st = c.createStatement()) {
            st.executeUpdate(sql);
        }
    }

    // ====== ENSURE blokkok ======

    // Alap user, hogy a PRODUCT.USER_ID FK ne hasaljon el
    private static void ensureUsers() throws SQLException {
        try (Connection c = DBUtil.getConnection()) {
            String chk = "SELECT 1 FROM users WHERE user_id = ?";
            String ins = "INSERT INTO users(user_id, username, email, password, createdat, role) " +
                         "VALUES(?,?,?,?,?,?)";
            try (PreparedStatement psChk = c.prepareStatement(chk);
                 PreparedStatement psIns = c.prepareStatement(ins)) {

                psChk.setInt(1, 1);
                try (ResultSet rs = psChk.executeQuery()) {
                    if (!rs.next()) {
                        psIns.setInt(1, 1);
                        psIns.setString(2, "admin");
                        psIns.setString(3, "admin@example.com");
                        psIns.setString(4, "admin"); // TODO: jelszó hash, ha kell
                        psIns.setDate(5, new java.sql.Date(System.currentTimeMillis()));
                        psIns.setString(6, "ADMIN"); // vagy "USER", ahogy szeretnéd
                        psIns.executeUpdate();
                    }
                }
            }
        }
    }

    private static void ensureManufacturers(List<Manufacturer> mans) throws SQLException {
        try (Connection c = DBUtil.getConnection()) {
            String chk = "SELECT 1 FROM manufacturer WHERE manufacturer_id=?";
            String ins = "INSERT INTO manufacturer(manufacturer_id, manufacturer_name, model_name) VALUES(?,?,?)";
            try (PreparedStatement psChk = c.prepareStatement(chk);
                 PreparedStatement psIns = c.prepareStatement(ins)) {
                for (Manufacturer m : mans) {
                    psChk.setInt(1, m.getId());
                    try (ResultSet rs = psChk.executeQuery()) {
                        if (!rs.next()) {
                            psIns.setInt(1, m.getId());
                            psIns.setString(2, m.getName());
                            psIns.setString(3, m.getModelName());
                            psIns.executeUpdate();
                        }
                    }
                }
            }
        }
    }

    // CATEGORY: 1–10, 1 név = 1 sor
    private static void ensureCategories() throws SQLException {
        Object[][] rows = {
                {1, "Periféria"},
                {2, "Laptop"},
                {3, "Monitor"},
                {4, "SSD"},
                {5, "Videókártya"},
                {6, "Hálózat"},
                {7, "HDD"},
                {8, "Memória"},
                {9, "Tablet"},
                {10, "Telefon"}
        };

        try (Connection c = DBUtil.getConnection()) {
            String chk = "SELECT 1 FROM category WHERE category_id=?";
            String ins = "INSERT INTO category(category_id, categoryname) VALUES(?,?)";
            try (PreparedStatement psChk = c.prepareStatement(chk);
                 PreparedStatement psIns = c.prepareStatement(ins)) {

                for (Object[] r : rows) {
                    int id = (Integer) r[0];
                    String name = (String) r[1];

                    psChk.setInt(1, id);
                    try (ResultSet rs = psChk.executeQuery()) {
                        if (!rs.next()) {
                            psIns.setInt(1, id);
                            psIns.setString(2, name);
                            psIns.executeUpdate();
                        }
                    }
                }
            }
        }
    }

    // Subcategory -> CATEGORY_ID hozzárendelés név alapján
    private static int categoryIdForSubcategoryName(String name) {
        switch (name) {
            case "Egér":
            case "Billentyűzet":
            case "Headset":
                return 1; // Periféria
            case "Notebook":
                return 2; // Laptop
            case "Monitor":
                return 3; // Monitor
            case "SSD":
                return 4; // SSD
            case "Videókártya":
                return 5; // Videókártya
            case "Router":
                return 6; // Hálózat
            case "HDD":
                return 7; // HDD
            case "Memória":
                return 8; // Memória
            case "Tablet":
                return 9; // Tablet
            case "Telefon":
                return 10; // Telefon
            default:
                throw new IllegalArgumentException("Ismeretlen subcategory név: " + name);
        }
    }

    private static void ensureSubcategories(List<Subcategory> subs) throws SQLException {
        try (Connection c = DBUtil.getConnection()) {
            String chk = "SELECT 1 FROM subcategory WHERE subcat_id=?";
            String ins = "INSERT INTO subcategory(subcat_id, subcatname, manufacturer_id, category_id) " +
                    "VALUES(?,?,?,?)";
            try (PreparedStatement psChk = c.prepareStatement(chk);
                 PreparedStatement psIns = c.prepareStatement(ins)) {

                for (Subcategory s : subs) {
                    psChk.setInt(1, s.getId());
                    try (ResultSet rs = psChk.executeQuery()) {
                        if (!rs.next()) {
                            psIns.setInt(1, s.getId());
                            psIns.setString(2, s.getName());
                            psIns.setInt(3, s.getManufacturer().getId());
                            psIns.setInt(4, categoryIdForSubcategoryName(s.getName()));
                            psIns.executeUpdate();
                        }
                    }
                }
            }
        }
    }

    // ====== Termék + Supply (DÁTUMMAL) + IMAGE_URL ======
    private static void add(InventoryService inv,
                            int productId, String name, int userId,
                            int categoryId, int subcatId,
                            int pieces, int purchasePrice, int sellPrice,
                            String imageUrl, LocalDate boughtDate) {

        Product p = new Product(
                productId,
                name,
                userId,
                new Category(categoryId, "", null),
                imageUrl,
                subcatId
        );
        inv.addProduct(p);

        Supply s = new Supply(productId, boughtDate, pieces, purchasePrice, sellPrice);
        inv.upsertSupply(s);
    }
}
