package dao;

import java.util.Set;

/**
 * Adat-hozzáférési réteg a kedvencek kezeléséhez.
 */
public interface FavoriteDao {
    /**
     * Visszaadja a felhasználó által kedvencnek jelölt termékek azonosítóit.
     */
    Set<Integer> findProductIdsByUser(int userId);

    /**
     * Kedvencnek jelöli a megadott terméket a felhasználónál.
     */
    boolean add(int userId, int productId);

    /**
     * Eltávolítja a kedvencek közül a megadott terméket.
     */
    boolean remove(int userId, int productId);
}
