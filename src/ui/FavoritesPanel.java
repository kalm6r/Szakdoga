package ui;

import model.Product;
import model.Supply;
import service.InventoryService;
import util.UrlImageLoader;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Displays only the favorite product cards for the current user.
 */
public class FavoritesPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private static final class CardVM {
        final int productId;
        final String name;
        final String middle;
        final String price;
        final Image image;
        CardVM(int productId, String name, String middle, String price, Image image) {
            this.productId = productId;
            this.name = name;
            this.middle = middle;
            this.price = price;
            this.image = image;
        }
    }

    private final Integer userId;
    private final FavoritesManager favoritesManager;
    private final InventoryService inventoryService = new InventoryService();

    private final JPanel cards = new JPanel(new GridLayout(0, 3, 16, 16));
    private final JScrollPane cardsScroll;
    private final CardLayout viewLayout = new CardLayout();
    private final JPanel viewPanel = new JPanel(viewLayout);
    private final JLabel messageLabel = new JLabel("", SwingConstants.CENTER);

    public FavoritesPanel(Integer userId, FavoritesManager favoritesManager) {
        this.userId = userId;
        this.favoritesManager = favoritesManager;
        if (this.favoritesManager != null) {
            this.favoritesManager.addChangeListener(this::refreshFavorites);
        }

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(896, 504));

        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(new EmptyBorder(16, 24, 8, 24));
        JLabel title = new JLabel("Kedvencek");
        title.setFont(new Font("Segoe UI", Font.PLAIN, 23));
        header.add(title, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        cards.setBorder(new EmptyBorder(16, 16, 16, 16));
        cards.setOpaque(false);

        cardsScroll = new JScrollPane(cards,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        cardsScroll.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
        cardsScroll.getVerticalScrollBar().setUnitIncrement(16);

        JPanel cardsWrapper = new JPanel(new BorderLayout());
        cardsWrapper.setBorder(new EmptyBorder(0, 24, 24, 24));
        cardsWrapper.add(cardsScroll, BorderLayout.CENTER);

        messageLabel.setFont(messageLabel.getFont().deriveFont(Font.PLAIN, 16f));
        messageLabel.setBorder(new EmptyBorder(16, 24, 24, 24));

        viewPanel.add(cardsWrapper, "cards");
        viewPanel.add(messageLabel, "message");
        add(viewPanel, BorderLayout.CENTER);

        Runnable recalcPadding = () -> {
            int viewportW = cardsScroll.getViewport().getWidth();
            int sbw = cardsScroll.getVerticalScrollBar().getWidth();
            if (sbw <= 0) {
                sbw = cardsScroll.getVerticalScrollBar().getPreferredSize().width;
            }
            final int cols = 3;
            final int gap = 16;
            final int cardWidth = ProductCard.IMG_W;
            final int minPad = 16;
            int used = cols * cardWidth + (cols - 1) * gap;
            int padLeft = Math.max(minPad, (viewportW - used) / 2);
            int padRight = padLeft + sbw + gap;
            cards.setBorder(new EmptyBorder(16, padLeft, 16, padRight));
            cards.revalidate();
            cards.repaint();
        };
        recalcPadding.run();
        cardsScroll.getViewport().addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                recalcPadding.run();
            }
        });
        cardsScroll.getVerticalScrollBar().addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                recalcPadding.run();
            }

            @Override
            public void componentHidden(java.awt.event.ComponentEvent e) {
                recalcPadding.run();
            }
        });

        refreshFavorites();
    }

    public void refreshFavorites() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::refreshFavorites);
            return;
        }

        if (userId == null) {
            showMessage("A kedvencek megtekintéséhez jelentkezz be.");
            return;
        }

        Set<Integer> favoriteIds = favoritesManager != null
                ? favoritesManager.getFavoritesSnapshot()
                : inventoryService.listFavoriteProductIds(userId);
        if (favoriteIds.isEmpty()) {
            cards.removeAll();
            cards.revalidate();
            cards.repaint();
            showMessage("Még nincs kedvenc termék.");
            return;
        }

        showMessage("Betöltés…");
        loadCardsAsync(favoriteIds);
    }

    private void loadCardsAsync(Set<Integer> favoriteIds) {
        new SwingWorker<Void, Void>() {
            List<CardVM> cardViewModels;

            @Override
            protected Void doInBackground() {
                List<Product> products = inventoryService.listAllProducts().stream()
                        .filter(p -> favoriteIds.contains(p.getId()))
                        .sorted((a, b) -> {
                            String aName = a.getName() == null ? "" : a.getName();
                            String bName = b.getName() == null ? "" : b.getName();
                            return aName.compareToIgnoreCase(bName);
                        })
                        .collect(Collectors.toList());

                Map<Integer, Supply> latestSupplyByProductId = inventoryService.listAllSupply().stream()
                        .collect(Collectors.toMap(
                                Supply::getProductId,
                                Function.identity(),
                                (a, b) -> a.getBought().isAfter(b.getBought()) ? a : b
                        ));

                cardViewModels = products.stream().map(p -> {
                    String middle = "";
                    if (p.getCategory() != null) {
                        if (p.getCategory().getSubcategory() != null && p.getCategory().getSubcategory().getName() != null) {
                            middle = p.getCategory().getSubcategory().getName();
                        } else if (p.getCategory().getName() != null) {
                            middle = p.getCategory().getName();
                        }
                    }
                    Supply supply = latestSupplyByProductId.get(p.getId());
                    String price = (supply != null) ? formatFt(supply.getSellPrice()) : "";

                    double scale;
                    java.awt.GraphicsConfiguration gc = FavoritesPanel.this.getGraphicsConfiguration();
                    if (gc != null) {
                        scale = gc.getDefaultTransform().getScaleX();
                    } else {
                        scale = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment()
                                .getDefaultScreenDevice()
                                .getDefaultConfiguration()
                                .getDefaultTransform()
                                .getScaleX();
                    }
                    int pxW = (int) Math.round(ProductCard.IMG_W * scale);
                    int pxH = (int) Math.round(ProductCard.IMG_H * scale);

                    Image hi = UrlImageLoader.get(p.getImageUrl(), pxW, pxH);
                    Image img = hi.getScaledInstance(ProductCard.IMG_W, ProductCard.IMG_H, Image.SCALE_SMOOTH);

                    return new CardVM(p.getId(), p.getName(), middle, price, img);
                }).collect(Collectors.toList());
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    cards.removeAll();
                    for (CardVM vm : cardViewModels) {
                        ProductCard card = new ProductCard();
                        card.setData(vm.name, vm.middle, vm.price, vm.image);
                        card.setFavoriteButtonVisible(true);
                        card.setFavoriteButtonEnabled(userId != null);
                        card.setFavorite(true);
                        card.addFavoriteToggleListener(evt -> {
                            boolean selected = card.isFavoriteSelected();
                            try {
                                if (favoritesManager != null) {
                                    favoritesManager.setFavorite(vm.productId, selected);
                                } else {
                                    boolean ok = selected
                                            ? inventoryService.addFavoriteProduct(userId, vm.productId)
                                            : inventoryService.removeFavoriteProduct(userId, vm.productId);
                                    if (!ok) {
                                        throw new RuntimeException("A kedvenc állapot mentése nem sikerült.");
                                    }
                                }
                            } catch (RuntimeException ex) {
                                card.setFavorite(!selected);
                                javax.swing.JOptionPane.showMessageDialog(FavoritesPanel.this,
                                        "Nem sikerült frissíteni a kedvencek listáját.\n" + ex.getMessage(),
                                        "Hiba", javax.swing.JOptionPane.ERROR_MESSAGE);
                            }
                        });
                        cards.add(card);
                    }
                    cards.revalidate();
                    cards.repaint();
                    showCards();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showMessage("Hiba a kedvencek betöltése közben: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void showCards() {
        viewLayout.show(viewPanel, "cards");
    }

    private void showMessage(String text) {
        messageLabel.setText("<html><div style='text-align:center;'>" + text + "</div></html>");
        viewLayout.show(viewPanel, "message");
    }

    private String formatFt(int value) {
        java.text.NumberFormat nf = java.text.NumberFormat.getIntegerInstance(new java.util.Locale("hu", "HU"));
        nf.setGroupingUsed(true);
        return nf.format(value) + " Ft";
    }
}