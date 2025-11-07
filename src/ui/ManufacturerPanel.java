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

import java.text.NumberFormat;
import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import model.Product;
import model.Supply;
import model.Manufacturer;
import service.InventoryService;

import util.UrlImageLoader;
import java.awt.Image;

public class ManufacturerPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private JTextField textField;
    
    private static final class CardVM {
        final String name, middle, price; 
        final Image image;
        CardVM(String n, String m, String p, Image i) { name=n; middle=m; price=p; image=i; }
    }

    // --- HOZZÁADOTT mezők a kártyákhoz/DB-hez ---
    private JPanel cards;
    private JScrollPane cardsScroll;

    private JPanel manufacturerList;            // panel_1-en belül, Y irányú lista
    private String activeManufacturerKey = null; // null = Összes
    private java.util.Map<String, java.util.Set<Integer>> manNameToIds = new java.util.HashMap<>();
    private JButton lastSelectedBtn = null;

    private final InventoryService inv = new InventoryService();

    /**
     * Create the panel.
     */
    public ManufacturerPanel() {
        setPreferredSize(new Dimension(896, 504));
        setLayout(null);

        JPanel panel_3 = new JPanel();
        panel_3.setBounds(61, 43, 825, 38);
        add(panel_3);
        panel_3.setLayout(null);

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

        JLabel lblNewLabel = new JLabel("Gyártó szerint");
        lblNewLabel.setBounds(0, 0, 171, 36);
        panel_3.add(lblNewLabel);
        lblNewLabel.setFont(new Font("Segoe UI", Font.PLAIN, 23));

        JPanel panel_1 = new JPanel();
        panel_1.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
        panel_1.setBackground(Color.WHITE);
        panel_1.setBounds(77, 110, 200, 350);
        panel_1.setLayout(new BorderLayout());
        add(panel_1);

        manufacturerList = new JPanel();
        manufacturerList.setBackground(Color.WHITE);
        manufacturerList.setLayout(new BoxLayout(manufacturerList, BoxLayout.Y_AXIS));
        JScrollPane mfgScroll = new JScrollPane(manufacturerList,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        mfgScroll.setBorder(null);
        panel_1.add(mfgScroll, BorderLayout.CENTER);

        // --- KÁRTYÁK + GÖRGETÉS ---
        cards = new JPanel(new GridLayout(0, 3, 16, 16));
        cards.setBorder(new EmptyBorder(16, 16, 16, 16));

        cardsScroll = new JScrollPane(
                cards,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );
        cardsScroll.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
        cardsScroll.setBounds(310, 110, 576, 350);
        cardsScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        cardsScroll.getVerticalScrollBar().setUnitIncrement(16);

        add(cardsScroll);

        Runnable recalcPadding = () -> {
            int viewportW = cardsScroll.getViewport().getWidth();

            int sbw = cardsScroll.getVerticalScrollBar().getWidth();
            if (sbw <= 0) sbw = cardsScroll.getVerticalScrollBar().getPreferredSize().width;

            final int COLS = 3, GAP = 16, CARD_W = 170, MIN_PAD = 16;

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

        // --- KERESÉS esemény: név szerinti szűrés ---
        btnNewButton_5.addActionListener(e -> loadCardsByManufacturerAsync(textField.getText()));
        textField.addActionListener(e -> loadCardsByManufacturerAsync(textField.getText())); // Enterre is keres

        populateManufacturerList();
        loadCardsByManufacturerAsync(null); // Összes + nincs névszűrés
    }

    // ------------------ SEGÉD: Kártyák betöltése háttérszálon ------------------
    // FŐ metódus: név + gyártó szerinti szűrés
    private void loadCardsByManufacturerAsync(String search, String manufacturerNameKey) {
        new SwingWorker<java.util.List<CardVM>, Void>() {
            @Override protected java.util.List<CardVM> doInBackground() {
                // 1) Alap: összes termék
                java.util.List<Product> products = inv.listAllProducts();

                // 2) Gyártó-név szerinti szűrés: a névhez tartozó ÖSSZES manufacturer_id engedélyezett
                if (manufacturerNameKey != null) {
                    java.util.Set<Integer> allowed = manNameToIds.getOrDefault(manufacturerNameKey, java.util.Set.of());
                    products = products.stream()
                            .filter(p -> {
                                Manufacturer m = extractManufacturer(p);
                                return m != null && allowed.contains(m.getId());
                            })
                            .collect(Collectors.toList());
                }

                // 3) Név szerinti szűrés (kliens oldalon)
                if (search != null && !search.isBlank()) {
                    final String q = search.toLowerCase();
                    products = products.stream()
                            .filter(p -> p.getName() != null && p.getName().toLowerCase().contains(q))
                            .collect(Collectors.toList());
                }

                // 4) Legutóbbi supply termékenként (árhoz)
                java.util.Map<Integer, Supply> latestSupplyByProductId = inv.listAllSupply().stream()
                        .collect(Collectors.toMap(
                                Supply::getProductId,
                                Function.identity(),
                                (a, b) -> a.getBought().isAfter(b.getBought()) ? a : b
                        ));

                // 5) VM-ek összeállítása HÁTTÉRSZÁLON (itt töltjük le a képet is!)
                java.util.List<CardVM> vms = products.stream().map(p -> {
                    String middle = "";
                    if (p.getCategory() != null) {
                        if (p.getCategory().getSubcategory() != null && p.getCategory().getSubcategory().getName() != null) {
                            middle = p.getCategory().getSubcategory().getName();
                        } else if (p.getCategory().getName() != null) {
                            middle = p.getCategory().getName();
                        }
                    }
                    Supply s = latestSupplyByProductId.get(p.getId());
                    String priceText = (s != null) ? formatFt(s.getSellPrice()) : "";

                    double scale = 1.0;
                    java.awt.GraphicsConfiguration gc = ManufacturerPanel.this.getGraphicsConfiguration();
                    if (gc != null) scale = gc.getDefaultTransform().getScaleX();
                    else scale = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment()
                            .getDefaultScreenDevice().getDefaultConfiguration().getDefaultTransform().getScaleX();
                    int pxW = (int)Math.round(ProductCard.IMG_W * scale);
                    int pxH = (int)Math.round(ProductCard.IMG_H * scale);
                    
                    Image hi =  UrlImageLoader.get(p.getImageUrl(), pxW, pxH);
                    Image img = hi.getScaledInstance(ProductCard.IMG_W, ProductCard.IMG_H, Image.SCALE_SMOOTH);


                    return new CardVM(p.getName(), middle, priceText, img);
                }).collect(Collectors.toList());

                return vms;
            }

            @Override protected void done() {
                try {
                    java.util.List<CardVM> vms = get();
                    cards.removeAll();
                    for (CardVM vm : vms) {
                        ProductCard card = new ProductCard();
                        // <<< MÁR NEM null a kép >>>
                        card.setData(vm.name, vm.middle, vm.price, vm.image);
                        cards.add(card);
                    }
                    cards.revalidate();
                    cards.repaint();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(ManufacturerPanel.this,
                            "Hiba a termékek betöltése közben:\n" + ex.getMessage(),
                            "Hiba", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
    // kényelmi overload (kereső gomb/Enter)
    private void loadCardsByManufacturerAsync(String search) {
        loadCardsByManufacturerAsync(search, activeManufacturerKey);
    }

    // Bal oldali listát feltölti: "Összes" + ABC szerint rendezett gyártók
    private void populateManufacturerList() {
        manufacturerList.removeAll();
        manNameToIds.clear();

        // Termékekből gyűjtjük: gyártónév -> összes manufacturer_id
        List<Product> all = inv.listAllProducts();
        java.util.Map<String,String> displayNameByKey = new java.util.LinkedHashMap<>();

        for (Product p : all) {
            Manufacturer m = extractManufacturer(p);
            if (m == null || m.getName() == null) continue;

            String name = m.getName().trim();
            if (name.isEmpty()) continue;

            String key = name.toLowerCase(java.util.Locale.ROOT);
            displayNameByKey.putIfAbsent(key, name);
            manNameToIds.computeIfAbsent(key, k -> new java.util.LinkedHashSet<>())
                        .add(m.getId());
        }

        // ABC szerint
        java.util.List<String> keys = new java.util.ArrayList<>(displayNameByKey.keySet());
        keys.sort(java.util.Comparator.comparing(displayNameByKey::get, String.CASE_INSENSITIVE_ORDER));

        addManufacturerButton("Összes", null);
        for (String key : keys) {
            addManufacturerButton(displayNameByKey.get(key), key);
        }

        manufacturerList.revalidate();
        manufacturerList.repaint();
    }

    private Manufacturer extractManufacturer(Product p) {
        if (p == null || p.getCategory() == null) return null;
        if (p.getCategory().getSubcategory() == null) return null;
        return p.getCategory().getSubcategory().getManufacturer();
    }

    private void addManufacturerButton(String text, String nameKey) {
        JButton b = new JButton(text);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setBorder(new EmptyBorder(8, 12, 8, 12));
        b.setAlignmentX(0f);

        b.addActionListener(ev -> {
            activeManufacturerKey = nameKey;            // lehet null = Összes
            highlightSelected(b);
            loadCardsByManufacturerAsync(textField.getText(), activeManufacturerKey);
        });

        if (nameKey == null && lastSelectedBtn == null) highlightSelected(b);
        manufacturerList.add(b);
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

    private String formatFt(int value) {
        NumberFormat nf = NumberFormat.getIntegerInstance(new Locale("hu", "HU"));
        nf.setGroupingUsed(true);
        return nf.format(value) + " Ft";
    }
}
