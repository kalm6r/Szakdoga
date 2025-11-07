package dao;

import model.Supply;
import java.util.*;

public interface SupplyDao {
	
    Optional<Supply> findByProductId(int productId);
    boolean upsert(Supply supply);
    boolean delete(int productId);
    List<Supply> findAll();
}
