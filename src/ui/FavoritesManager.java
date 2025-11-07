package ui;

import service.InventoryService;

import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Shared favorite-state coordinator that keeps the toggled products in sync across panels.
 */
public class FavoritesManager {
    private final InventoryService inventoryService;
    private final Integer userId;
    private final List<Runnable> listeners = new ArrayList<>();
    private Set<Integer> favorites = new HashSet<>();

    public FavoritesManager(Integer userId, InventoryService inventoryService) {
        this.userId = userId;
        this.inventoryService = inventoryService;
        reloadFromDatabase();
    }

    public boolean isLoggedIn() {
        return userId != null;
    }

    public synchronized void addChangeListener(Runnable listener) {
        listeners.add(listener);
    }

    public void reloadFromDatabase() {
        Set<Integer> fresh;
        if (userId == null) {
            fresh = new HashSet<>();
        } else {
            fresh = new HashSet<>(inventoryService.listFavoriteProductIds(userId));
        }
        synchronized (this) {
            favorites = fresh;
        }
        fireChanged();
    }

    public Set<Integer> getFavoritesSnapshot() {
        synchronized (this) {
            return new HashSet<>(favorites);
        }
    }

    public void setFavorite(int productId, boolean favorite) {
        if (userId == null) {
            throw new IllegalStateException("Nem sikerült frissíteni a kedvenceket bejelentkezés nélkül.");
        }

        boolean ok = favorite
                ? inventoryService.addFavoriteProduct(userId, productId)
                : inventoryService.removeFavoriteProduct(userId, productId);
        if (!ok) {
            throw new RuntimeException("A kedvenc állapot mentése nem sikerült.");
        }

        synchronized (this) {
            if (favorite) {
                favorites.add(productId);
            } else {
                favorites.remove(productId);
            }
        }
        fireChanged();
    }

    private void fireChanged() {
        List<Runnable> snapshot;
        synchronized (this) {
            snapshot = new ArrayList<>(listeners);
        }
        for (Runnable listener : snapshot) {
            SwingUtilities.invokeLater(listener);
        }
    }
}