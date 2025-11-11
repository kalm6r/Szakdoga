package dao;

import model.Subcategory;

import java.util.List;
import java.util.Optional;

public interface SubcategoryDao {
    List<Subcategory> findByName(String name);
    Optional<Subcategory> findByNameAndManufacturer(String name, String manufacturerName);
    Optional<Subcategory> create(String name, String manufacturerName);
}