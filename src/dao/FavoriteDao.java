package dao;

import java.util.Set;

public interface FavoriteDao {
	
    Set<Integer> findProductIdsByUser(int userId);

    boolean add(int userId, int productId);

    boolean remove(int userId, int productId);
}