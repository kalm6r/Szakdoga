package ui;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.border.LineBorder;
import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JLabel;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import javax.swing.SwingWorker;
import javax.swing.ScrollPaneConstants;
import javax.swing.JOptionPane;
import javax.swing.JToggleButton;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import model.Product;
import model.Supply;
import service.InventoryService;
import util.UrlImageLoader;

public class CategoryPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private JTextField textField;
    private JToggleButton favoriteFilterButton;
    private boolean favoritesOnly = false;

    // --- HOZZÁADOTT mezők a kártyákhoz/DB-hez ---
    private JPanel cards;
    private JScrollPane cardsScroll;

    private RangeSlider priceSlider;
    private JLabel priceRangeLabel;
    private boolean priceDataAvailable = false;
    
    private JPanel categoryList;                 // panel_1-en belül, Y irányú lista
    private String activeCategoryKey = null;
    private java.util.Map<String, java.util.Set<Integer>> catNameToIds = new java.util.HashMap<>();// null = Összes
    private JButton lastSelectedBtn = null;
    
    private static final class CardVM {
        final int productId;
        final String name;
        final String middle;
        final String purchasePrice;
        final String purchaseDate;
        final String sellPrice;
        final Image image;
        final boolean favorite;
        CardVM(int id, String n, String m, String purchasePrice, String purchaseDate, String sellPrice, Image i, boolean fav) {
            productId = id;
            name = n;
            middle = m;
            this.purchasePrice = purchasePrice;
            this.purchaseDate = purchaseDate;
            this.sellPrice = sellPrice;
            image = i;
            favorite = fav;
        }
    }
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy. MM. dd.");

    private final InventoryService inv = new InventoryService();
    private final Integer userId;
    private final FavoritesManager favoritesManager;
    
    /**
     * Create the panel.
     */
    public CategoryPanel() {
        this(null, null);
    }

    public CategoryPanel(Integer userId) {
        this(userId, null);
    }

    public CategoryPanel(Integer userId, FavoritesManager favoritesManager) {
        this.userId = userId;
        this.favoritesManager = favoritesManager;
        if (this.favoritesManager != null) {
            this.favoritesManager.addChangeListener(this::refreshFavorites);
        }
        setPreferredSize(new Dimension(896, 504));
        setLayout(null);

        JPanel panel_3 = new JPanel();
        panel_3.setBounds(61, 43, 825, 38);
        add(panel_3);
        panel_3.setLayout(null);

        favoriteFilterButton = new JToggleButton("☆");
        favoriteFilterButton.setFocusPainted(false);
        favoriteFilterButton.setBorderPainted(false);
        favoriteFilterButton.setContentAreaFilled(false);
        favoriteFilterButton.setOpaque(false);
        favoriteFilterButton.setMargin(new Insets(0, 0, 0, 0));
        favoriteFilterButton.setFont(favoriteFilterButton.getFont().deriveFont(Font.PLAIN, 20f));
        favoriteFilterButton.setBounds(432, 7, 30, 30);
        favoriteFilterButton.addActionListener(e -> {
            favoritesOnly = favoriteFilterButton.isSelected();
            loadCardsAsync(textField.getText(), activeCategoryKey);
        });
        favoriteFilterButton.addChangeListener(e -> updateFavoriteFilterIcon());
        panel_3.add(favoriteFilterButton);

        JButton btnNewButton_5 = new JButton("");
        btnNewButton_5.setContentAreaFilled(false);
        btnNewButton_5.setBorderPainted(false);
        btnNewButton_5.setIcon(new ImageIcon("C:\\Users\\ASUS\\eclipse-workspace\\Szakdolgozat\\src\\resources\\search.png"));
        btnNewButton_5.setBounds(788, 7, 30, 30);
        panel_3.add(btnNewButton_5);
        panel_3.setComponentZOrder(btnNewButton_5, 0);

        textField = new JTextField();
        textField.setBounds(472, 11, 315, 27);
        panel_3.add(textField);
        textField.setColumns(10);

        boolean loggedIn = userId != null;
        favoriteFilterButton.setEnabled(loggedIn);
        if (!loggedIn) {
            favoriteFilterButton.setToolTipText("Bejelentkezéssel szűrhetsz a kedvencekre");
            favoriteFilterButton.setSelected(false);
            favoritesOnly = false;
        } else {
            favoriteFilterButton.setToolTipText("Kedvencek mutatása");
        }
        updateFavoriteFilterIcon();

        JLabel lblNewLabel = new JLabel("Kategória szerint");
        lblNewLabel.setBounds(16, 0, 171, 36);
        panel_3.add(lblNewLabel);
        lblNewLabel.setFont(new Font("Segoe UI", Font.PLAIN, 23));


        JPanel panel_1 = new JPanel();
        panel_1.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
        panel_1.setBackground(Color.WHITE);
        panel_1.setBounds(77, 110, 200, 350);
        panel_1.setLayout(new BorderLayout());
        add(panel_1);

        categoryList = new JPanel();
        categoryList.setBackground(Color.WHITE);
        categoryList.setLayout(new BoxLayout(categoryList, BoxLayout.Y_AXIS));
        JScrollPane catScroll = new JScrollPane(categoryList,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        catScroll.setBorder(null);
        ModernUIComponents.applyModernScrollbarStyle(catScroll);
        panel_1.add(catScroll, BorderLayout.CENTER);	

        // --- KÁRTYÁK + GÖRGETÉS ---
        cards = new JPanel(new GridLayout(0, 3, 16, 16));
        cards.setBackground(new Color(248, 249, 250));
        cards.setBorder(new EmptyBorder(16, 16, 16, 16));
        
        final int cardsX = 310;
        final int cardsY = 110;
        final int cardsWidth = 576;
        final int cardsHeight = 350;

        cardsScroll = new JScrollPane(cards,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        cardsScroll.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
        cardsScroll.getViewport().setBackground(new Color(248, 249, 250));  // ← ADD HOZZÁ ezt a sort
        cardsScroll.setBounds(cardsX, cardsY, cardsWidth, cardsHeight);
        cardsScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        cardsScroll.getVerticalScrollBar().setUnitIncrement(16);
        ModernUIComponents.applyModernScrollbarStyle(cardsScroll);

        add(cardsScroll);
        
        final int sliderSpacing = 8;
        final int pricePanelHeight = 36;
        final int pricePanelY = cardsY + cardsHeight + sliderSpacing;

        JPanel priceFilterPanel = new JPanel(new BorderLayout(12, 0));
        priceFilterPanel.setOpaque(false);
        priceFilterPanel.setBorder(new EmptyBorder(8, 12, 8, 12));
        priceFilterPanel.setBounds(cardsX, pricePanelY, cardsWidth, pricePanelHeight);
        add(priceFilterPanel);

        JLabel priceFilterTitle = new JLabel("Ár szerinti szűrés");
        priceFilterTitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        priceFilterPanel.add(priceFilterTitle, BorderLayout.WEST);

        priceSlider = new RangeSlider();
        priceSlider.setEnabled(false);
        priceSlider.setOpaque(false);
        priceFilterPanel.add(priceSlider, BorderLayout.CENTER);

        priceRangeLabel = new JLabel("Nincs ár adat");
        priceRangeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        priceRangeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        priceFilterPanel.add(priceRangeLabel, BorderLayout.EAST);

        Runnable recalcPadding = () -> {
            int viewportW = cardsScroll.getViewport().getWidth();

            // scrollbar szélessége (ha 0-t adna, használjuk a preferred-et)
            int sbw = cardsScroll.getVerticalScrollBar().getWidth();
            if (sbw <= 0) sbw = cardsScroll.getVerticalScrollBar().getPreferredSize().width;

            final int COLS = 3, GAP = 16, CARD_W = ProductCard.IMG_W, MIN_PAD = 16;

            int used = COLS * CARD_W + (COLS - 1) * GAP;

            // bal oldali padding a viewporton belül
            int padLeft = Math.max(MIN_PAD, (viewportW - used) / 2);

            // jobb oldali padding = bal + scrollbar + kis fix hézag (GAP)
            int padRight = padLeft + sbw + GAP;

            cards.setBorder(new EmptyBorder(16, padLeft, 16, padRight));
            cards.revalidate();
            cards.repaint();
        };


        recalcPadding.run();

        // és frissítsük, ha a viewport mérete változik (ablak átméretezés, stb.)
        cardsScroll.getViewport().addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent e) { recalcPadding.run(); }
        });

        // ha megjelenik/eltűnik a scrollbar (tartalom magassága miatt)
        cardsScroll.getVerticalScrollBar().addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentShown(java.awt.event.ComponentEvent e) { recalcPadding.run(); }
            @Override public void componentHidden(java.awt.event.ComponentEvent e) { recalcPadding.run(); }
        });

        // --- KERESÉS esemény: név szerinti szűrés ---
        btnNewButton_5.addActionListener(e -> loadCardsAsync(textField.getText()));
        textField.addActionListener(e -> loadCardsAsync(textField.getText())); // Enterre is keres

        populateCategoryList();
        initializePriceFilter();
        loadCardsAsync(null); // Összes + nincs névszűrés


    }

    // ------------------ SEGÉD: Kártyák betöltése háttérszálon ------------------
 // FŐ metódus: név + kategória szerinti szűrés
    private void loadCardsAsync(String search, String categoryNameKey) {
        final boolean filterFavorites = favoritesOnly;
        final RangeSlider slider = priceSlider;
        final boolean priceAvailable = priceDataAvailable && slider != null;
        final int sliderMin = priceAvailable ? slider.getMinimum() : Integer.MIN_VALUE;
        final int sliderMax = priceAvailable ? slider.getMaximum() : Integer.MAX_VALUE;
        final int minPriceFilter = priceAvailable ? slider.getValue() : Integer.MIN_VALUE;
        final int maxPriceFilter = priceAvailable ? slider.getUpperValue() : Integer.MAX_VALUE;
        final boolean priceFilterActive = priceAvailable && slider.isEnabled()
                && (minPriceFilter > sliderMin || maxPriceFilter < sliderMax);
        new SwingWorker<List<CardVM>, Void>() {
            List<Product> products;
            Map<Integer, Supply> latestSupplyByProductId;
            Set<Integer> favoriteIds = favoritesManager != null
                    ? favoritesManager.getFavoritesSnapshot()
                    : (userId != null ? inv.listFavoriteProductIds(userId) : Set.of());

            @Override protected List<CardVM> doInBackground() {
                // Alap: összes termék
                products = inv.listAllProducts();

                // Kategória-név szerinti szűrés: a névhez tartozó ÖSSZES category_id engedélyezett
                if (categoryNameKey != null) {
                    java.util.Set<Integer> allowed = catNameToIds.getOrDefault(categoryNameKey, java.util.Set.of());
                    products = products.stream()
                            .filter(p -> p.getCategory() != null && allowed.contains(p.getCategory().getId()))
                            .collect(Collectors.toList());
                }

                // Név szerinti szűrés (kliens oldalon)
                if (search != null && !search.isBlank()) {
                    final String q = search.toLowerCase();
                    products = products.stream()
                            .filter(p -> p.getName() != null && p.getName().toLowerCase().contains(q))
                            .collect(Collectors.toList());
                }

                if (filterFavorites) {
                    products = products.stream()
                            .filter(p -> favoriteIds.contains(p.getId()))
                            .collect(Collectors.toList());
                }

                // Legutóbbi supply
                latestSupplyByProductId = inv.listAllSupply().stream()
                        .collect(Collectors.toMap(
                                Supply::getProductId,
                                Function.identity(),
                                (a, b) -> a.getBought().isAfter(b.getBought()) ? a : b
                ));

                if (priceAvailable) {
                    products = products.stream()
                            .filter(p -> {
                                Supply s = latestSupplyByProductId.get(p.getId());
                                if (s == null) {
                                    return !priceFilterActive;
                                }
                                int sell = s.getSellPrice();
                                return sell >= minPriceFilter && sell <= maxPriceFilter;
                            })
                            .collect(Collectors.toList());
                }
                List<CardVM> vms = products.stream().map(p -> {
                    String middle = "";
                    if (p.getCategory() != null) {
                        if (p.getCategory().getSubcategory() != null && p.getCategory().getSubcategory().getName() != null) {
                            middle = p.getCategory().getSubcategory().getName();
                        } else if (p.getCategory().getName() != null) {
                            middle = p.getCategory().getName();
                        }
                    }
                    Supply s = latestSupplyByProductId.get(p.getId());
                    String purchasePrice = (s != null) ? formatFt(s.getPurchasePrice()) : "";
                    String purchaseDate = (s != null && s.getBought() != null) ? formatDate(s.getBought()) : "";
                    String sellPrice = (s != null) ? formatFt(s.getSellPrice()) : "";

                    double scale = 1.0;
                    java.awt.GraphicsConfiguration gc = CategoryPanel.this.getGraphicsConfiguration();
                    if (gc != null) {
                        scale = gc.getDefaultTransform().getScaleX();
                    } else {
                        scale = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment()
                                .getDefaultScreenDevice()
                                .getDefaultConfiguration()
                                .getDefaultTransform().getScaleX();
                    }
                    int pxW = (int)Math.round(ProductCard.IMG_W * scale);
                    int pxH = (int)Math.round(ProductCard.IMG_H * scale);
                    
                    Image hi =  UrlImageLoader.get(p.getImageUrl(), pxW, pxH);
                    Image img = hi != null
                            ? hi.getScaledInstance(ProductCard.IMG_W, ProductCard.IMG_H, Image.SCALE_SMOOTH)
                            : null;


                    boolean favorite = favoriteIds.contains(p.getId());
                    return new CardVM(p.getId(), p.getName(), middle, purchasePrice, purchaseDate, sellPrice, img, favorite);
                }).collect(Collectors.toList());

                return vms;
            }

            @Override protected void done() {
                try {
                    List<CardVM> vms = get();
                    cards.removeAll();
                    for (CardVM vm : vms) {      // <-- NEM 'products'-ból dolgozunk, hanem a vms-ből
                        ProductCard card = new ProductCard();
                        card.setData(vm.name, vm.middle, vm.purchasePrice, vm.purchaseDate, vm.sellPrice, vm.image); 
                        boolean loggedIn = userId != null;
                        card.setFavoriteButtonVisible(true);
                        card.setFavoriteButtonEnabled(loggedIn);
                        if (!loggedIn) {
                            card.setFavorite(false);
                        } else {
                            card.setFavorite(vm.favorite);
                            card.addFavoriteToggleListener(evt -> {
                                boolean selected = card.isFavoriteSelected();
                                try {
                                    if (favoritesManager != null) {
                                        favoritesManager.setFavorite(vm.productId, selected);
                                    } else {
                                        boolean ok = selected
                                                ? inv.addFavoriteProduct(userId, vm.productId)
                                                : inv.removeFavoriteProduct(userId, vm.productId);
                                        if (!ok) {
                                            throw new RuntimeException("A kedvenc állapot mentése nem sikerült.");
                                        }
                                    }
                                } catch (RuntimeException ex) {
                                    card.setFavorite(!selected);
                                    JOptionPane.showMessageDialog(CategoryPanel.this,
                                            "Nem sikerült frissíteni a kedvencek listáját.\n" + ex.getMessage(),
                                            "Hiba", JOptionPane.ERROR_MESSAGE);
                                }
                            });
                        }
                        cards.add(card);
                    }
                    cards.revalidate();
                    cards.repaint();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(CategoryPanel.this,
                            "Hiba a termékek betöltése közben:\n" + ex.getMessage(),
                            "Hiba", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    // kényelmi overload (kereső gomb/Enter)
    private void loadCardsAsync(String search) {
        loadCardsAsync(search, activeCategoryKey);
    }

    public void refreshFavorites() {
        loadCardsAsync(textField.getText(), activeCategoryKey);
    }

 // Bal oldali listát feltölti: "Összes" + ABC szerint rendezett kategóriák
    private void populateCategoryList() {
        categoryList.removeAll();
        catNameToIds.clear();

        // Termékekből gyűjtjük: név -> összes category_id
        List<Product> all = inv.listAllProducts();
        java.util.Map<String,String> displayNameByKey = new java.util.LinkedHashMap<>();

        for (Product p : all) {
            if (p.getCategory() == null || p.getCategory().getName() == null) continue;
            String name = p.getCategory().getName().trim();
            if (name.isEmpty()) continue;

            String key = name.toLowerCase(java.util.Locale.ROOT);
            displayNameByKey.putIfAbsent(key, name);
            catNameToIds.computeIfAbsent(key, k -> new java.util.LinkedHashSet<>())
                        .add(p.getCategory().getId());
        }

        // ABC szerint
        java.util.List<String> keys = new java.util.ArrayList<>(displayNameByKey.keySet());
        keys.sort(java.util.Comparator.comparing(displayNameByKey::get, String.CASE_INSENSITIVE_ORDER));

        addCategoryButton("Összes", null);
        for (String key : keys) {
            addCategoryButton(displayNameByKey.get(key), key);
        }

        categoryList.revalidate();
        categoryList.repaint();
    }

    private void initializePriceFilter() {
        if (priceSlider == null || priceRangeLabel == null) {
            return;
        }

        List<Supply> supplies = inv.listAllSupply();
        if (supplies.isEmpty()) {
            priceDataAvailable = false;
            priceSlider.setRange(0, 0);
            priceSlider.setEnabled(false);
            priceRangeLabel.setText("Nincs ár adat");
            return;
        }

        priceDataAvailable = true;
        int min = supplies.stream().mapToInt(Supply::getSellPrice).min().orElse(0);
        int max = supplies.stream().mapToInt(Supply::getSellPrice).max().orElse(min);
        priceSlider.setRange(min, max);
        priceSlider.setEnabled(min != max);
        updatePriceRangeLabel();

        if (priceSlider.getChangeListeners().length == 0) {
            priceSlider.addChangeListener(e -> {
                updatePriceRangeLabel();
                if (!priceSlider.getValueIsAdjusting()) {
                    loadCardsAsync(textField.getText(), activeCategoryKey);
                }
            });
        }
    }

    private void updatePriceRangeLabel() {
        if (!priceDataAvailable) {
            priceRangeLabel.setText("Nincs ár adat");
            return;
        }

        int lower = priceSlider.getValue();
        int upper = priceSlider.getUpperValue();
        if (!priceSlider.isEnabled() || lower == upper) {
            priceRangeLabel.setText(formatFt(lower));
        } else {
            priceRangeLabel.setText(formatFt(lower) + " - " + formatFt(upper));
        }
    }


    private void addCategoryButton(String text, String nameKey) {
        JButton b = new JButton(text);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setBorder(new EmptyBorder(8, 12, 8, 12));
        b.setAlignmentX(0f);

        b.addActionListener(ev -> {
            activeCategoryKey = nameKey;                   // lehet null = Összes
            highlightSelected(b);
            loadCardsAsync(textField.getText(), activeCategoryKey);
        });

        if (nameKey == null && lastSelectedBtn == null) highlightSelected(b);
        categoryList.add(b);
    }

    
    private void highlightSelected(JButton btn) {
        if (lastSelectedBtn != null) {
            lastSelectedBtn.setOpaque(false);
            lastSelectedBtn.setBackground(null);
            lastSelectedBtn.setForeground(Color.BLACK);
        }
        lastSelectedBtn = btn;
        btn.setOpaque(true);
        btn.setBackground(new Color(230,230,230));
        btn.setForeground(Color.BLACK);
    }

    private void updateFavoriteFilterIcon() {
        if (favoriteFilterButton == null) {
            return;
        }
        if (!favoriteFilterButton.isEnabled()) {
            favoriteFilterButton.setText("☆");
            favoriteFilterButton.setForeground(new Color(0xCCCCCC));
            return;
        }
        if (favoriteFilterButton.isSelected()) {
            favoriteFilterButton.setText("★");
            favoriteFilterButton.setForeground(new Color(0xE0A000));
            favoriteFilterButton.setToolTipText("Csak a kedvencek mutatása");
        } else {
            favoriteFilterButton.setText("☆");
            favoriteFilterButton.setForeground(new Color(0x999999));
            favoriteFilterButton.setToolTipText("Kedvencek mutatása");
        }
    }

    private String formatFt(int value) {
        NumberFormat nf = NumberFormat.getIntegerInstance(new Locale("hu", "HU"));
        nf.setGroupingUsed(true);
        return nf.format(value) + " Ft";
    }
    private String formatDate(LocalDate value) {
        return value.format(DATE_FORMATTER);
    }
}