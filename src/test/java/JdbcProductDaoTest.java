package test.java;

import dao.ProductDao;
import dao.jdbc.JdbcProductDao;
import model.Product;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class JdbcProductDaoTest {

    private final ProductDao dao = new JdbcProductDao();
    private static final int EXISTING_ID = 1001;

    @Test
    void findById_shouldReturnExistingProduct() {
        var opt = dao.findById(EXISTING_ID);
        assertTrue(opt.isPresent(), "A terméknek léteznie kell");
        Product p = opt.get();
        assertNotNull(p.getCategory());
        assertNotNull(p.getCategory().getSubcategory().getManufacturer());
    }

    @Test
    void update_shouldChangeNameAndRevert() {
        var p = dao.findById(EXISTING_ID).orElseThrow();
        String orig = p.getName();
        Product upd = new Product(p.getId(), orig + " *", p.getUserId(), p.getCategory(), p.getImageUrl());
        assertTrue(dao.update(upd), "Update-nek sikerülnie kell");

        var again = dao.findById(EXISTING_ID).orElseThrow();
        assertEquals(orig + " *", again.getName());

		assertTrue(dao.update(new Product(p.getId(), orig, p.getUserId(), p.getCategory(), p.getImageUrl())),
				"Visszaállító update-nek sikerülnie kell");
    }
}
