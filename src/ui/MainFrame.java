package ui;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import dao.UserDao;
import service.InventoryService;

public class MainFrame extends JFrame {
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel content = new JPanel(cardLayout);
    private final UserDao.UserRecord currentUser;

    public MainFrame() {
        this(null);
    }

    public MainFrame(UserDao.UserRecord user) {
        super("Szakdolgozat — Katalógus");
        this.currentUser = user;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        setContentPane(root);

        // --- BAL OLDALI IKON SÁV (változatlan kinézet) ---
        JPanel panel = new JPanel(null);
        panel.setBorder(null);
        panel.setPreferredSize(new Dimension(50, 0));
        root.add(panel, BorderLayout.WEST);
        panel.setLayout(null);

        JPanel panel_1 = new JPanel();
        panel_1.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
        panel_1.setBounds(0, 155, 50, 194);
        panel.add(panel_1);

        JButton btnCategory = new JButton("");
        btnCategory.setBorderPainted(false);
        btnCategory.setIcon(new ImageIcon("C:\\Users\\ASUS\\eclipse-workspace\\Szakdolgozat\\src\\resources\\category.png"));
        btnCategory.setBounds(10, 7, 30, 30);
        panel_1.add(btnCategory);
        btnCategory.setPreferredSize(new Dimension(30, 30));
        
        JButton btnBrand = new JButton("");
        panel_1.add(btnBrand);
        btnBrand.setBorderPainted(false);
        btnBrand.setIcon(new ImageIcon("C:\\Users\\ASUS\\eclipse-workspace\\Szakdolgozat\\src\\resources\\brand.png"));
        btnBrand.setPreferredSize(new Dimension(30, 30));
                
        JButton btnTime = new JButton("");
        panel_1.add(btnTime);
        btnTime.setBorderPainted(false);
        btnTime.setIcon(new ImageIcon("C:\\Users\\ASUS\\eclipse-workspace\\Szakdolgozat\\src\\resources\\time.png"));
        btnTime.setPreferredSize(new Dimension(30, 30));
        
        JButton btnStat = new JButton("");
        panel_1.add(btnStat);
        btnStat.setBorderPainted(false);
        btnStat.setIcon(new ImageIcon("C:\\Users\\ASUS\\eclipse-workspace\\Szakdolgozat\\src\\resources\\stat.png"));
        btnStat.setPreferredSize(new Dimension(30, 30));
                        
        JButton btnTop = new JButton("");
        panel_1.add(btnTop);
        btnTop.setBorderPainted(false);
        btnTop.setIcon(new ImageIcon("C:\\Users\\ASUS\\eclipse-workspace\\Szakdolgozat\\src\\resources\\top.png"));
        btnTop.setPreferredSize(new Dimension(30, 30));
        


        // --- KÖZÉPSŐ TARTALOM ---
        root.add(content, BorderLayout.CENTER);

        // Valódi nézetek:
        Integer userId = currentUser != null ? currentUser.id() : null;

        InventoryService favoriteInventory = new InventoryService();
        FavoritesManager favoritesManager = new FavoritesManager(userId, favoriteInventory);

        CategoryPanel categoryPanel = new CategoryPanel(userId, favoritesManager);   // ez a most elkészült, kártyás panel
        content.add(categoryPanel, "category");

        ManufacturerPanel manufacturerPanel = new ManufacturerPanel(userId, favoritesManager);
        content.add(manufacturerPanel, "manufacturer");

        PurchaseDatePanel purchaseDatePanel = new PurchaseDatePanel(userId, favoritesManager);
        content.add(purchaseDatePanel, "time");

        StatsPanel statsPanel = new StatsPanel();
        content.add(statsPanel, "stat");
        
        TopProductsPanel topProductsPanel = new TopProductsPanel();
        content.add(topProductsPanel, "top");


        // Kezdő nézet:
        cardLayout.show(content, "category");
        
        pack();             // a frame mérete = tartalom preferredSize
        setResizable(false); // opcionális: ne lehessen nagyobbra húzni


        // Navigáció:
        btnCategory.addActionListener(e -> cardLayout.show(content, "category"));
        btnBrand.addActionListener(e -> cardLayout.show(content, "manufacturer"));
        btnTime.addActionListener(e -> cardLayout.show(content, "time"));
        btnStat.addActionListener(e -> cardLayout.show(content, "stat"));
        btnTop.addActionListener(e -> cardLayout.show(content, "top"));
    }

    private JPanel centerLabel(String text) {
        JPanel p = new JPanel(new BorderLayout());
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(l.getFont().deriveFont(Font.PLAIN, 18f));
        p.add(l, BorderLayout.CENTER);
        return p;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
