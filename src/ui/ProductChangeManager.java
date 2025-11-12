package ui;

import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.List;

/**
 * Központi változáskezelő, ami értesíti a paneleket, ha termék változik
 */
public class ProductChangeManager {
    private static ProductChangeManager instance;
    private final List<ProductChangeListener> listeners = new ArrayList<>();
    
    private ProductChangeManager() {}
    
    public static synchronized ProductChangeManager getInstance() {
        if (instance == null) {
            instance = new ProductChangeManager();
        }
        return instance;
    }
    
    /**
     * Listener hozzáadása
     */
    public void addListener(ProductChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /**
     * Listener eltávolítása
     */
    public void removeListener(ProductChangeListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Értesítés termék hozzáadásáról
     */
    public void notifyProductAdded(int productId) {
        SwingUtilities.invokeLater(() -> {
            for (ProductChangeListener listener : new ArrayList<>(listeners)) {
                listener.onProductAdded(productId);
            }
        });
    }
    
    /**
     * Értesítés termék módosításáról
     */
    public void notifyProductUpdated(int productId) {
        SwingUtilities.invokeLater(() -> {
            for (ProductChangeListener listener : new ArrayList<>(listeners)) {
                listener.onProductUpdated(productId);
            }
        });
    }
    
    /**
     * Értesítés termék törléséről
     */
    public void notifyProductDeleted(int productId) {
        SwingUtilities.invokeLater(() -> {
            for (ProductChangeListener listener : new ArrayList<>(listeners)) {
                listener.onProductDeleted(productId);
            }
        });
    }
    
    /**
     * Általános frissítés értesítés (amikor mindent újra kell tölteni)
     */
    public void notifyProductsChanged() {
        SwingUtilities.invokeLater(() -> {
            for (ProductChangeListener listener : new ArrayList<>(listeners)) {
                listener.onProductsChanged();
            }
        });
    }
    
    /**
     * Interface a változások figyeléséhez
     */
    public interface ProductChangeListener {
        default void onProductAdded(int productId) {
            onProductsChanged();
        }
        
        default void onProductUpdated(int productId) {
            onProductsChanged();
        }
        
        default void onProductDeleted(int productId) {
            onProductsChanged();
        }
        
        void onProductsChanged();
    }
}