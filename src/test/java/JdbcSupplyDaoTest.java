package test.java;

import dao.SupplyDao;
import dao.jdbc.JdbcSupplyDao;
import model.Supply;
import org.junit.jupiter.api.*;

import util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class JdbcSupplyDaoTest {

    private final SupplyDao dao = new JdbcSupplyDao();
    private static final int EXISTING_PRODUCT_ID = 1001; // A DB-ben létező PRODUCT_ID-hez kapcsoljuk

    @BeforeEach
    void wipe() throws Exception {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM supply WHERE product_id=?")) {
            ps.setInt(1, EXISTING_PRODUCT_ID);
            ps.executeUpdate();
        }
    }

    @Test
    void upsert_shouldInsertThenUpdate() {
        // 1) INSERT
        Supply s1 = new Supply(EXISTING_PRODUCT_ID, LocalDate.now(), 10, 400_000, 480_000);
        assertTrue(dao.upsert(s1), "Első upsert INSERT legyen");

        Supply got = dao.findByProductId(EXISTING_PRODUCT_ID).orElseThrow();
        assertEquals(10, got.getPieces());
        assertEquals(480_000, got.getSellPrice());

        // 2) UPDATE
        Supply s2 = new Supply(EXISTING_PRODUCT_ID, LocalDate.now(), 5, 420_000, 520_000);
        assertTrue(dao.upsert(s2), "Második upsert UPDATE legyen");

        Supply got2 = dao.findByProductId(EXISTING_PRODUCT_ID).orElseThrow();
        assertEquals(5, got2.getPieces());
        assertEquals(520_000, got2.getSellPrice());
    }

    @Test
    void delete_shouldRemoveRow() {
        dao.upsert(new Supply(EXISTING_PRODUCT_ID, LocalDate.now(), 1, 1, 1));
        assertTrue(dao.delete(EXISTING_PRODUCT_ID));
        assertTrue(dao.findByProductId(EXISTING_PRODUCT_ID).isEmpty(), "Törlés után nem lehet rekord");
    }
}
