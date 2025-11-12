package dao;

import dto.CategoryOption;
import java.util.List;
import java.util.Optional;

public interface CategoryDao {
    List<CategoryOption> listOptions();
    
    /**
     * Új kategória létrehozása
     */
    Optional<CategoryOption> create(String name);
    
    /**
     * Kategória átnevezése
     */
    boolean update(int categoryId, String newName);
    
    /**
     * Kategória törlése
     */
    boolean delete(int categoryId);
    
    /**
     * Kategória keresése ID alapján
     */
    Optional<CategoryOption> findById(int categoryId);
    
    /**
     * Kategória keresése név alapján
     */
    Optional<CategoryOption> findByName(String name);
}