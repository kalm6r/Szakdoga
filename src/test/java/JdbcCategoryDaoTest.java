package test.java;

import dao.CategoryDao;
import dao.jdbc.JdbcCategoryDao;
import dto.CategoryOption;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JdbcCategoryDaoTest {

    private final CategoryDao dao = new JdbcCategoryDao();

    @Test
    void listOptions_shouldReturnAtLeastOne() {
        List<CategoryOption> cats = dao.listOptions();
        assertNotNull(cats);
        assertFalse(cats.isEmpty(), "Legalább egy kategóriának lennie kell a mintában");
        assertNotNull(cats.get(0).getName());
    }
}
