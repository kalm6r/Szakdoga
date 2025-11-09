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
import java.util.HashSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.Function;
import java.util.stream.Collectors;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import model.Product;
import model.Supply;
import service.InventoryService;

import util.UrlImageLoader;
import java.awt.Image;
import java.awt.Insets;

public class PurchaseDatePanel extends JPanel {

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
	
    private static final long serialVersionUID = 1L;
    private JTextField textField;
    private JToggleButton favoriteFilterButton;
    private boolean favoritesOnly = false;

    // --- Kártyák + görgetés ---
    private JPanel cards;
    private JScrollPane cardsScroll;

    private RangeSlider priceSlider;
    private JLabel priceRangeLabel;
    private boolean priceDataAvailable = false;

    // --- Bal oldali dátumlista (év → lenyíló hónapok) ---
    private JPanel dateList;         // panel_1-en belül, Y irányú lista
    private JButton lastSelectedBtn = null;

    private Integer activeYear = null;      // null = Összes
    private Integer activeMonth = null;     // null = csak év (vagy Összes), 1..12 = hónap
    private final Set<Integer> expandedYears = new HashSet<>();

    // Melyik évhez milyen hónapok érhetők el (az adatok alapján)
    private final Map<Integer, Set<Integer>> monthsByYear = new HashMap<>();

    private final InventoryService inv = new InventoryService();
    private final Integer userId;
    private final FavoritesManager favoritesManager;

    public PurchaseDatePanel() {
        this(null, null);
    }

    public PurchaseDatePanel(Integer userId) {
        this(userId, null);
    }

    public PurchaseDatePanel(Integer userId, FavoritesManager favoritesManager) {
        this.userId = userId;
        this.favoritesManager = favoritesManager;
        if (this.favoritesManager != null) {
            this.favoritesManager.addChangeListener(this::refreshFavorites);
        }
        setPreferredSize(new Dimension(896, 504));
        setLayout(null);

        // --- Felső sor (kereső + cím) ---
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
            loadCardsByPurchaseDateAsync(textField.getText(), activeYear, activeMonth);
        });
        favoriteFilterButton.addChangeListener(e -> updateFavoriteFilterIcon());
        panel_3.add(favoriteFilterButton);

        JButton btnSearch = new JButton("");
        btnSearch.setContentAreaFilled(false);
        btnSearch.setBorderPainted(false);
        btnSearch.setIcon(new ImageIcon("C:\\Users\\ASUS\\eclipse-workspace\\Szakdolgozat\\src\\resources\\search.png"));
        btnSearch.setBounds(788, 7, 30, 30);
        panel_3.add(btnSearch);
        panel_3.setComponentZOrder(btnSearch, 0);

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

        JLabel lblNewLabel = new JLabel("Vásárlás dátuma szerint");
        lblNewLabel.setBounds(16, 0, 300, 36);
        panel_3.add(lblNewLabel);
        lblNewLabel.setFont(new Font("Segoe UI", Font.PLAIN, 23));

        // --- Bal oldali doboz (év/hónap lista) ---
        JPanel panel_1 = new JPanel();
        panel_1.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
        panel_1.setBackground(Color.WHITE);
        panel_1.setBounds(77, 110, 200, 350);
        panel_1.setLayout(new BorderLayout());
        add(panel_1);

        dateList = new JPanel();
        dateList.setBackground(Color.WHITE);
        dateList.setLayout(new BoxLayout(dateList, BoxLayout.Y_AXIS));
        JScrollPane dateScroll = new JScrollPane(dateList,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        dateScroll.setBorder(null);
        panel_1.add(dateScroll, BorderLayout.CENTER);

        // --- Kártyák + görgetés (3 oszlop) ---
        cards = new JPanel(new GridLayout(0, 3, 16, 16));
        cards.setBorder(new EmptyBorder(16, 16, 16, 16));

        final int cardsX = 310;
        final int cardsY = 110;
        final int cardsWidth = 576;
        final int cardsHeight = 350;

        cardsScroll = new JScrollPane(
                cards,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );
        cardsScroll.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
        cardsScroll.setBounds(cardsX, cardsY, cardsWidth, cardsHeight);
        cardsScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        cardsScroll.getVerticalScrollBar().setUnitIncrement(16);

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

        // Középre igazító belső padding (scrollbar kompenzáció) — változatlan logika
        Runnable recalcPadding = () -> {
            int viewportW = cardsScroll.getViewport().getWidth();
            int sbw = cardsScroll.getVerticalScrollBar().getWidth();
            if (sbw <= 0) sbw = cardsScroll.getVerticalScrollBar().getPreferredSize().width;
            final int COLS = 3, GAP = 16, CARD_W = ProductCard.IMG_W, MIN_PAD = 16;
            int used = COLS * CARD_W + (COLS - 1) * GAP;
            int padLeft = Math.max(MIN_PAD, (viewportW - used) / 2);
            int padRight = padLeft + sbw + GAP;
            cards.setBorder(new EmptyBorder(16, padLeft, 16, padRight));
            cards.revalidate();
            cards.repaint();
        };
        recalcPadding.run();
        cardsScroll.getViewport().addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent e) { recalcPadding.run(); }
        });
        cardsScroll.getVerticalScrollBar().addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentShown(java.awt.event.ComponentEvent e) { recalcPadding.run(); }
            @Override public void componentHidden(java.awt.event.ComponentEvent e) { recalcPadding.run(); }
        });

        // Keresés: név szerinti szűrés
        btnSearch.addActionListener(e -> loadCardsByPurchaseDateAsync(textField.getText(), activeYear, activeMonth));
        textField.addActionListener(e -> loadCardsByPurchaseDateAsync(textField.getText(), activeYear, activeMonth));

        // Bal oldali év/hónap lista feltöltése és induló betöltés
        populateDateList();
        initializePriceFilter();
        loadCardsByPurchaseDateAsync(null, null, null); // Összes
    }

    /** Feltölti a bal oldali év → hónap listát.
     *  2025-től lefele listázunk a legkorábbi évig, ami az adatokban szerepel.
     *  A hónapok csak azok, amelyek ténylegesen előfordulnak (a legutóbbi Supply alapján).
     */
    private void populateDateList() {
        dateList.removeAll();
        monthsByYear.clear();

        // A legutóbbi Supply minden termékhez, hogy a hónap-listát releváns adatok alapján képezzük
        Map<Integer, Supply> latestSupply = inv.listAllSupply().stream()
                .collect(Collectors.toMap(
                        Supply::getProductId,
                        Function.identity(),
                        (a, b) -> a.getBought().isAfter(b.getBought()) ? a : b
                ));

        // Év → hónapok halmaza
        for (Supply s : latestSupply.values()) {
            if (s == null || s.getBought() == null) continue;
            LocalDate d = s.getBought();
            monthsByYear.computeIfAbsent(d.getYear(), y -> new HashSet<>()).add(d.getMonthValue());
        }

        // Kezdő év: 2025, utolsó év: a legkorábbi előforduló (ha nincs, akkor 2025)
        int startYear = 2025;
        int minYear = monthsByYear.keySet().stream().min(Integer::compareTo).orElse(2025);

        // "Összes" gomb
        addDateButton("Összes", null, null);

        // 2025 → minYear (lefelé)
        for (int y = startYear; y >= minYear; y--) {
            addYearWithOptionalMonths(y);
        }

        dateList.revalidate();
        dateList.repaint();
    }

    private void addYearWithOptionalMonths(int year) {
        // Év gomb: kattintás = kiválaszt + lenyit/összecsuk
        JButton yearBtn = makeFilterButton(yearLabel(year), 0);
        yearBtn.addActionListener(ev -> {
            // kiválasztás évre (hónap null)
            activeYear = year;
            activeMonth = null;
            toggleExpand(year);
            highlightSelected(yearBtn);
            // újratölt
            loadCardsByPurchaseDateAsync(textField.getText(), activeYear, activeMonth);
            // újraépítjük a listát, hogy a lenyitás/összecsukás látszódjon
            rebuildDateListUI();
        });
        dateList.add(yearBtn);

        // Ha épp nyitva kell lennie, megjelenítjük a hónapokat (csak a létezőket)
        if (expandedYears.contains(year)) {
            Set<Integer> months = monthsByYear.getOrDefault(year, Set.of());
            List<Integer> sorted = new ArrayList<>(months);
            sorted.sort(Comparator.naturalOrder());

            for (Integer m : sorted) {
                JButton monthBtn = makeFilterButton("  - " + monthLabel(m), 24);
                monthBtn.addActionListener(ev -> {
                    activeYear = year;
                    activeMonth = m;
                    highlightSelected(monthBtn);
                    loadCardsByPurchaseDateAsync(textField.getText(), activeYear, activeMonth);
                });
                dateList.add(monthBtn);
            }
        }
    }

    private void rebuildDateListUI() {
        dateList.removeAll();
        addDateButton("Összes", null, null);

        int startYear = 2025;
        int minYear = monthsByYear.keySet().stream().min(Integer::compareTo).orElse(2025);
        for (int y = startYear; y >= minYear; y--) {
            addYearWithOptionalMonths(y);
        }
        dateList.revalidate();
        dateList.repaint();
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
                    loadCardsByPurchaseDateAsync(textField.getText(), activeYear, activeMonth);
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

    private void toggleExpand(int year) {
        if (expandedYears.contains(year)) expandedYears.remove(year);
        else expandedYears.add(year);
    }

    private void addDateButton(String text, Integer year, Integer month) {
        JButton b = makeFilterButton(text, 0);
        b.addActionListener(ev -> {
            activeYear = year;
            activeMonth = month;
            highlightSelected(b);
            loadCardsByPurchaseDateAsync(textField.getText(), activeYear, activeMonth);
        });
        if (year == null && month == null && lastSelectedBtn == null) highlightSelected(b);
        dateList.add(b);
    }

    private JButton makeFilterButton(String text, int leftPadding) {
        JButton b = new JButton(text);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setBorder(new EmptyBorder(8, 12 + leftPadding, 8, 12));
        b.setAlignmentX(0f);
        return b;
    }

    private String yearLabel(int y) {
        // nyitott/összecsukott jelzés az expandedYears alapján
        String prefix = expandedYears.contains(y) ? "▼ " : "► ";
        return prefix + y;
    }
    
    // Magyar hónapnevek (1..12);
    private static final String[] HUN_MONTHS = {
        "", "Január", "Február", "Március", "Április", "Május", "Június",
        "Július", "Augusztus", "Szeptember", "Október", "November", "December"
    };


    private String monthLabel(int m) {
        if (m >= 1 && m <= 12) return HUN_MONTHS[m];
        // Fallback, ha valamiért nincs 1..12 között
        return String.valueOf(m);
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

    // ------------------ Kártyák betöltése háttérszálon (év/hónap szűréssel) ------------------
    private void loadCardsByPurchaseDateAsync(String search, Integer yearFilter, Integer monthFilter) {
        final boolean filterFavorites = favoritesOnly;
        final RangeSlider slider = priceSlider;
        final boolean priceAvailable = priceDataAvailable && slider != null;
        final int sliderMin = priceAvailable ? slider.getMinimum() : Integer.MIN_VALUE;
        final int sliderMax = priceAvailable ? slider.getMaximum() : Integer.MAX_VALUE;
        final int minPriceFilter = priceAvailable ? slider.getValue() : Integer.MIN_VALUE;
        final int maxPriceFilter = priceAvailable ? slider.getUpperValue() : Integer.MAX_VALUE;
        final boolean priceFilterActive = priceAvailable && slider.isEnabled()
                && (minPriceFilter > sliderMin || maxPriceFilter < sliderMax);
        new SwingWorker<java.util.List<CardVM>, Void>() {
            @Override protected java.util.List<CardVM> doInBackground() {
                // 1) Összes termék
                java.util.List<Product> products = inv.listAllProducts();

                // 2) Legutóbbi supply termékenként (árhoz és dátumszűréshez)
                java.util.Map<Integer, Supply> latestSupplyByProductId = inv.listAllSupply().stream()
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

                // 3) Dátum szűrés a legutóbbi Supply alapján
                if (yearFilter != null) {
                    products = products.stream()
                        .filter(p -> {
                            Supply s = latestSupplyByProductId.get(p.getId());
                            if (s == null || s.getBought() == null) return false;
                            LocalDate d = s.getBought();
                            if (d.getYear() != yearFilter) return false;
                            if (monthFilter != null && d.getMonthValue() != monthFilter) return false;
                            return true;
                        })
                        .collect(Collectors.toList());
                }

                // 4) Név szerinti szűrés
                if (search != null && !search.isBlank()) {
                    final String q = search.toLowerCase();
                    products = products.stream()
                        .filter(p -> p.getName() != null && p.getName().toLowerCase().contains(q))
                        .collect(Collectors.toList());
                }

                final Set<Integer> favoriteIds = favoritesManager != null
                        ? favoritesManager.getFavoritesSnapshot()
                        : (userId != null ? inv.listFavoriteProductIds(userId) : Set.of());

                if (filterFavorites) {
                    products = products.stream()
                            .filter(p -> favoriteIds.contains(p.getId()))
                            .collect(Collectors.toList());
                }

                // 5) ViewModel összeállítása HÁTTÉRSZÁLON (itt töltjük le a képet is!)
                java.util.List<CardVM> vms = products.stream().map(p -> {
                    // középső sor: alkategória -> kategória
                    String middle = "";
                    if (p.getCategory() != null) {
                        if (p.getCategory().getSubcategory() != null && p.getCategory().getSubcategory().getName() != null) {
                            middle = p.getCategory().getSubcategory().getName();
                        } else if (p.getCategory().getName() != null) {
                            middle = p.getCategory().getName();
                        }
                    }

                    // ár a legutóbbi supply-ból
                    Supply s = latestSupplyByProductId.get(p.getId());
                    String purchasePrice = (s != null) ? formatFt(s.getPurchasePrice()) : "";
                    String purchaseDate = (s != null && s.getBought() != null) ? formatDate(s.getBought()) : "";
                    String sellPrice = (s != null) ? formatFt(s.getSellPrice()) : "";

                    double scale = 1.0;
                    java.awt.GraphicsConfiguration gc = PurchaseDatePanel.this.getGraphicsConfiguration();
                    if (gc != null) scale = gc.getDefaultTransform().getScaleX();
                    else scale = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment()
                            .getDefaultScreenDevice().getDefaultConfiguration().getDefaultTransform().getScaleX();
                    int pxW = (int)Math.round(ProductCard.IMG_W * scale);
                    int pxH = (int)Math.round(ProductCard.IMG_H * scale);
                    
                    Image hi =  UrlImageLoader.get(p.getImageUrl(), pxW, pxH);
                    Image img = hi.getScaledInstance(ProductCard.IMG_W, ProductCard.IMG_H, Image.SCALE_SMOOTH);


                    boolean favorite = favoriteIds.contains(p.getId());
                    return new CardVM(p.getId(), p.getName(), middle, purchasePrice, purchaseDate, sellPrice, img, favorite);
                }).collect(Collectors.toList());

                return vms;
            }

            @Override protected void done() {
                try {
                    java.util.List<CardVM> vms = get();
                    cards.removeAll();
                    for (CardVM vm : vms) {
                        ProductCard card = new ProductCard();
                        // <<< már NEM null a kép >>>
                        card.setData(vm.name, vm.middle, vm.purchasePrice, vm.purchaseDate, vm.sellPrice, vm.image);
                        configureFavoriteToggle(card, vm);
                        cards.add(card);
                    }
                    cards.revalidate();
                    cards.repaint();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(PurchaseDatePanel.this,
                            "Hiba a termékek betöltése közben:\n" + ex.getMessage(),
                            "Hiba", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    public void refreshFavorites() {
        loadCardsByPurchaseDateAsync(textField.getText(), activeYear, activeMonth);
    }

    private void configureFavoriteToggle(ProductCard card, CardVM vm) {
        boolean loggedIn = userId != null;
        card.setFavoriteButtonVisible(true);
        card.setFavoriteButtonEnabled(loggedIn);
        if (!loggedIn) {
            card.setFavorite(false);
            return;
        }

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
                JOptionPane.showMessageDialog(PurchaseDatePanel.this,
                        "Nem sikerült frissíteni a kedvencek listáját.\n" + ex.getMessage(),
                        "Hiba", JOptionPane.ERROR_MESSAGE);
            }
        });
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