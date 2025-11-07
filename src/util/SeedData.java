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
        LocalDate.of(2023,1,12), LocalDate.of(2023,2,23), LocalDate.of(2023,3,7),
        LocalDate.of(2023,4,18), LocalDate.of(2023,5,29), LocalDate.of(2023,6,11),
        LocalDate.of(2023,7,22), LocalDate.of(2023,8,5),  LocalDate.of(2023,9,16),
        LocalDate.of(2023,10,3), LocalDate.of(2023,11,14), LocalDate.of(2023,12,27),
        // 2024
        LocalDate.of(2024,1,9),  LocalDate.of(2024,2,21), LocalDate.of(2024,3,5),
        LocalDate.of(2024,4,17), LocalDate.of(2024,5,28), LocalDate.of(2024,6,10),
        LocalDate.of(2024,7,19), LocalDate.of(2024,8,2),  LocalDate.of(2024,9,13),
        LocalDate.of(2024,10,25),LocalDate.of(2024,11,6),  LocalDate.of(2024,12,18),
        // 2025
        LocalDate.of(2025,1,8),  LocalDate.of(2025,2,12), LocalDate.of(2025,3,24),
        LocalDate.of(2025,4,4),  LocalDate.of(2025,5,15), LocalDate.of(2025,6,27),
        LocalDate.of(2025,7,9),  LocalDate.of(2025,8,20), LocalDate.of(2025,9,1),
        LocalDate.of(2025,10,14),LocalDate.of(2025,11,3) // (aktuális környéke)
    );
    private static int dateIdx = 0;
    private static LocalDate nextDate() {
        LocalDate d = DATES.get(dateIdx % DATES.size());
        dateIdx++;
        return d;
    }

    public static void main(String[] args) throws Exception {
        // 0) TÖRLÉS – helyes sorrendben
        purgeAll();

        // 1) Gyártók (bővített lista)
        List<Manufacturer> mans = Arrays.asList(
            new Manufacturer(101, "Logitech",      "MX Master"),
            new Manufacturer(102, "HP",            "Pavilion"),
            new Manufacturer(103, "Dell",          "Inspiron"),
            new Manufacturer(104, "Samsung",       "Odyssey"),
            new Manufacturer(105, "Kingston",      "NV2"),
            new Manufacturer(106, "Razer",         "Kraken"),
            new Manufacturer(107, "ASUS",          "ZenBook"),
            new Manufacturer(108, "Lenovo",        "ThinkPad"),
            new Manufacturer(109, "Acer",          "Aspire"),
            new Manufacturer(110, "LG",            "UltraGear"),
            new Manufacturer(111, "Corsair",       "K-series"),
            new Manufacturer(112, "SteelSeries",   "Arctis"),
            new Manufacturer(113, "Apple",         "MacBook"),
            new Manufacturer(114, "MSI",           "Katana"),
            new Manufacturer(115, "Western Digital","Blue"),
            new Manufacturer(116, "Crucial",       "MX500"),
            new Manufacturer(117, "Gigabyte",      "AORUS"),
            new Manufacturer(118, "TP-Link",       "Archer"),
            new Manufacturer(119, "Seagate",       "Barracuda")
        );
        ensureManufacturers(mans);

        // 2) Subcategory-k (gyártóhoz kötve)
        List<Subcategory> subs = Arrays.asList(
            new Subcategory(211, "Egér",         mans.get(0)),  // Logitech
            new Subcategory(212, "Billentyűzet", mans.get(0)),  // Logitech
            new Subcategory(213, "Headset",      mans.get(5)),  // Razer
            new Subcategory(221, "Notebook",     mans.get(2)),  // Dell
            new Subcategory(222, "Notebook",     mans.get(1)),  // HP
            new Subcategory(231, "Monitor",      mans.get(3)),  // Samsung
            new Subcategory(241, "SSD",          mans.get(4)),  // Kingston

            new Subcategory(252, "Notebook",     mans.get(6)),  // ASUS
            new Subcategory(253, "Notebook",     mans.get(7)),  // Lenovo
            new Subcategory(254, "Notebook",     mans.get(8)),  // Acer
            new Subcategory(255, "Notebook",     mans.get(12)), // Apple
            new Subcategory(256, "Notebook",     mans.get(13)), // MSI
            new Subcategory(261, "Monitor",      mans.get(9)),  // LG
            new Subcategory(262, "Billentyűzet", mans.get(10)), // Corsair
            new Subcategory(263, "Headset",      mans.get(11)), // SteelSeries
            new Subcategory(271, "SSD",          mans.get(14)), // WD
            new Subcategory(272, "SSD",          mans.get(15)), // Crucial

            new Subcategory(281, "Videókártya",  mans.get(16)), // Gigabyte
            new Subcategory(282, "Router",       mans.get(17)), // TP-Link
            new Subcategory(283, "HDD",          mans.get(18)), // Seagate
            new Subcategory(284, "Memória",      mans.get(10)), // Corsair
            new Subcategory(285, "Memória",      mans.get(4)),  // Kingston
            new Subcategory(286, "Tablet",       mans.get(12)), // Apple
            new Subcategory(287, "Tablet",       mans.get(3)),  // Samsung
            new Subcategory(288, "Telefon",      mans.get(3))   // Samsung
        );
        ensureSubcategories(subs);

        // 3) Category-k (név szerinti csoportosítás)
        ensureCategories(new Object[][]{
            {  1, "Periféria", 211 }, // Egér (Logitech)
            {  2, "Periféria", 212 }, // Billentyűzet (Logitech)
            {  3, "Periféria", 213 }, // Headset (Razer)
            {  4, "Laptop",    221 }, // Notebook (Dell)
            {  5, "Laptop",    222 }, // Notebook (HP)
            {  6, "Monitor",   231 }, // Monitor (Samsung)
            {  7, "SSD",       241 }, // SSD (Kingston)

            {  8, "Laptop",    252 }, // ASUS
            {  9, "Laptop",    253 }, // Lenovo
            { 10, "Laptop",    254 }, // Acer
            { 11, "Laptop",    255 }, // Apple
            { 12, "Laptop",    256 }, // MSI
            { 13, "Monitor",   261 }, // LG
            { 14, "Periféria", 262 }, // Billentyűzet (Corsair)
            { 15, "Periféria", 263 }, // Headset (SteelSeries)
            { 16, "SSD",       271 }, // WD
            { 17, "SSD",       272 }, // Crucial

            { 18, "Videókártya", 281 }, // Gigabyte GPU
            { 19, "Hálózat",     282 }, // Router (TP-Link)
            { 20, "HDD",         283 }, // HDD (Seagate)
            { 21, "Memória",     284 }, // RAM (Corsair)
            { 22, "Memória",     285 }, // RAM (Kingston)
            { 23, "Tablet",      286 }, // Apple
            { 24, "Tablet",      287 }, // Samsung
            { 25, "Telefon",     288 }  // Samsung
        });

        // 4) Termékek + Supply + IMAGE_URL (classpath) — dátum automatikusan szórva
        InventoryService inv = new InventoryService();

        // Eredeti 7 + bővítések (30+ tétel)
        add(inv, 1001, "Logitech MX Master 3S", 1,  1,  30,   22000,   32990, "classpath:/images/mxmaster3s.jpg", nextDate());
        add(inv, 1002, "Logitech K380",         1,  2,  20,   12000,   16990, "classpath:/images/k380.jpg",       nextDate());
        add(inv, 1003, "Razer Kraken V3",       1,  3,  12,   24000,   34990, "classpath:/images/krakenv3.jpg",   nextDate());
        add(inv, 1004, "Dell Inspiron 14",      1,  4,  15,  220000,  259000, "classpath:/images/inspiron14.jpg", nextDate());
        add(inv, 1005, "HP Pavilion 15",        1,  5,  10,  210000,  249000, "classpath:/images/pavilion15.jpg", nextDate());
        add(inv, 1006, "Samsung 27\" Monitor",  1,  6,  12,   65000,   79990, "classpath:/images/27monitor.jpg",  nextDate());
        add(inv, 1007, "Kingston NV2 1TB SSD",  1,  7,  25,   21000,   27990, "classpath:/images/nv2ssd.jpg",     nextDate());

        // Perifériák
        add(inv, 1010, "Logitech M185",               1,  1,  40,    5000,    6990, "classpath:/images/m185.jpg", nextDate());
        add(inv, 1011, "Logitech G502 Hero",          1,  1,  18,   15000,   21990, "classpath:/images/g52hero.jpg", nextDate());
        add(inv, 1012, "Corsair K70 RGB MK.2",        1, 14,  12,   32000,   44990, "classpath:/images/corsairk70.jpg",       nextDate());
        add(inv, 1013, "SteelSeries Arctis 7",        1, 15,  10,   38000,   54990, "classpath:/images/arctis7.jpg",   nextDate());
        add(inv, 1014, "Logitech MX Keys",            1,  2,  22,   29000,   39990, "classpath:/images/keys.jpg",       nextDate());

        // Laptopok
        add(inv, 1015, "ASUS ZenBook 14",             1,  8,  14,  220000,  279000, "classpath:/images/zenbook14.jpg", nextDate());
        add(inv, 1016, "Lenovo ThinkPad T14",         1,  9,   9,  240000,  299000, "classpath:/images/thinkpadt14.jpg", nextDate());
        add(inv, 1017, "Acer Aspire 5",               1, 10,  11,  180000,  229000, "classpath:/images/aspire5.jpg", nextDate());
        add(inv, 1018, "Apple MacBook Air 13",        1, 11,   7,  380000,  449000, "classpath:/images/air13.jpg", nextDate());
        add(inv, 1019, "MSI Katana 15",               1, 12,   8,  300000,  359000, "classpath:/images/katana15.jpg", nextDate());
        add(inv, 1020, "Lenovo IdeaPad 5",            1,  9,  13,  190000,  239000, "classpath:/images/ideapad5.jpg", nextDate());

        // Monitorok
        add(inv, 1021, "Samsung Odyssey G5 32\"",     1,  6,   6,  110000,  139990, "classpath:/images/g5.jpg",  nextDate());
        add(inv, 1022, "LG UltraGear 27GL850",        1, 13,   5,  120000,  149990, "classpath:/images/27gl.jpg",  nextDate());
        add(inv, 1023, "LG 27MP400-B",                1, 13,  16,   60000,   79990, "classpath:/images/27mp.jpg",  nextDate());

        // SSD-k
        add(inv, 1024, "WD Blue 1TB SSD",             1, 16,  20,   19000,   25990, "classpath:/images/m2.jpg",     nextDate());
        add(inv, 1025, "Crucial MX500 1TB",           1, 17,  18,   20000,   27990, "classpath:/images/mx500.jpg",     nextDate());
        add(inv, 1026, "Kingston NV2 2TB SSD",        1,  7,  12,   38000,   49990, "classpath:/images/nv2.jpg",     nextDate());

        // Új kategóriák
        add(inv, 1030, "Gigabyte GeForce RTX 4060",   1, 18,   7,  120000,  159900, "classpath:/images/rtx4060.jpg",  nextDate());
        add(inv, 1031, "TP-Link Archer AX55 Router",  1, 19,  15,   24000,   32990, "classpath:/images/ax55.jpg",  nextDate());
        add(inv, 1032, "Seagate Barracuda 2TB HDD",   1, 20,  20,   18000,   24990, "classpath:/images/barra.jpg",     nextDate());
        add(inv, 1033, "Corsair Vengeance 16GB DDR4", 1, 21,  25,   16000,   21990, "classpath:/images/veng.jpg",     nextDate());
        add(inv, 1034, "Kingston FURY Beast 16GB DDR5",1,22, 22,   24000,   32990, "classpath:/images/beast.jpg",      nextDate());
        add(inv, 1035, "Apple iPad Air 10.9\"",       1, 23,   6,  220000,  279990, "classpath:/images/air10.jpg", nextDate());
        add(inv, 1036, "Samsung Galaxy Tab S9",       1, 24,   5,  260000,  319990, "classpath:/images/s9.jpg",  nextDate());
        add(inv, 1037, "Samsung Galaxy S24",          1, 25,   8,  280000,  349990, "classpath:/images/s24.jpg",  nextDate());

        System.out.println("Kész: bővített seed — sokféle dátummal szétszórva 2023–2025 között.");
    }

    // ====== TÖRLÉS – FK-sorrendben ======
    private static void purgeAll() throws SQLException {
        try (Connection c = DBUtil.getConnection()) {
            c.setAutoCommit(false);
            exec(c, "DELETE FROM supply");
            exec(c, "DELETE FROM product");
            exec(c, "DELETE FROM category");
            exec(c, "DELETE FROM subcategory");
            exec(c, "DELETE FROM manufacturer");
            c.commit();
        }
    }
    private static void exec(Connection c, String sql) throws SQLException {
        try (Statement st = c.createStatement()) { st.executeUpdate(sql); }
    }

    // ====== ENSURE blokkok ======
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

    private static void ensureSubcategories(List<Subcategory> subs) throws SQLException {
        try (Connection c = DBUtil.getConnection()) {
            String chk = "SELECT 1 FROM subcategory WHERE subcat_id=?";
            String ins = "INSERT INTO subcategory(subcat_id, subcatname, manufacturer_id) VALUES(?,?,?)";
            try (PreparedStatement psChk = c.prepareStatement(chk);
                 PreparedStatement psIns = c.prepareStatement(ins)) {
                for (Subcategory s : subs) {
                    psChk.setInt(1, s.getId());
                    try (ResultSet rs = psChk.executeQuery()) {
                        if (!rs.next()) {
                            psIns.setInt(1, s.getId());
                            psIns.setString(2, s.getName());
                            psIns.setInt(3, s.getManufacturer().getId());
                            psIns.executeUpdate();
                        }
                    }
                }
            }
        }
    }

    // rows: {category_id, categoryname, subcat_id}
    private static void ensureCategories(Object[][] rows) throws SQLException {
        try (Connection c = DBUtil.getConnection()) {
            String chk = "SELECT 1 FROM category WHERE category_id=?";
            String ins = "INSERT INTO category(category_id, categoryname, subcat_id) VALUES(?,?,?)";
            try (PreparedStatement psChk = c.prepareStatement(chk);
                 PreparedStatement psIns = c.prepareStatement(ins)) {
                for (Object[] r : rows) {
                    int id = (Integer) r[0];
                    String name = (String) r[1];
                    int subcatId = (Integer) r[2];

                    psChk.setInt(1, id);
                    try (ResultSet rs = psChk.executeQuery()) {
                        if (!rs.next()) {
                            psIns.setInt(1, id);
                            psIns.setString(2, name);
                            psIns.setInt(3, subcatId);
                            psIns.executeUpdate();
                        }
                    }
                }
            }
        }
    }

    // ====== Termék + Supply (DÁTUMMAL) + IMAGE_URL ======
    private static void add(InventoryService inv,
                            int productId, String name, int userId, int categoryId,
                            int pieces, int purchasePrice, int sellPrice, String imageUrl, LocalDate boughtDate) {

        Product p = new Product(
                productId,
                name,
                userId,
                new Category(categoryId, "", null),
                imageUrl
        );
        inv.addProduct(p);

        Supply s = new Supply(productId, boughtDate, pieces, purchasePrice, sellPrice);
        inv.upsertSupply(s);
    }
}
