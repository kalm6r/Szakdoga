package ui;

import service.InventoryService;
import model.Product;
import model.Category;
import model.Subcategory;
import model.Manufacturer;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class StatsPanel extends JPanel {

    private final InventoryService inv = new InventoryService();

    private final JPanel topBar = new JPanel(null);
    private final JLabel title = new JLabel("Statisztikák");

    private final SimpleBarChart catChart = new SimpleBarChart("Darabszám kategóriák szerint");
    private final SimpleBarChart manChart = new SimpleBarChart("Darabszám gyártók szerint");

    public StatsPanel() {
        setPreferredSize(new Dimension(896, 504));
        setLayout(null);

        // Felső sáv (egységes a többi panellel)
        topBar.setBounds(61, 43, 825, 38);
        add(topBar);

        title.setFont(new Font("Segoe UI", Font.PLAIN, 23));
        title.setBounds(0, 0, 300, 36);
        topBar.add(title);

        // Diagramok – két egymás alatti doboz
        JPanel catBox = boxed(catChart);
        catBox.setBounds(77, 110, 800, 150);
        add(catBox);

        JPanel manBox = boxed(manChart);
        manBox.setBounds(77, 280, 800, 150);
        add(manBox);

        reloadData();
    }

    private JPanel boxed(JComponent inner) {
        JPanel box = new JPanel(new BorderLayout());
        box.setBackground(Color.WHITE);
        box.setBorder(new LineBorder(Color.BLACK, 1, true));
        box.add(inner, BorderLayout.CENTER);
        return box;
    }

    /** Adatok újratöltése a service-ből, majd kirajzolás. */
    private void reloadData() {
        List<Product> products = new ArrayList<>(inv.listAllProducts());

        // Kategória → darab
        Map<String, Long> byCategory = products.stream()
            .collect(Collectors.groupingBy(
                p -> safeCategoryName(p), Collectors.counting()
            ));

        // Gyártó → darab
        Map<String, Long> byManufacturer = products.stream()
            .collect(Collectors.groupingBy(
                p -> safeManufacturerName(p), Collectors.counting()
            ));

        // Rendezés érték szerint csökkenő (szebb az ábra), legfeljebb ~12 oszlop hogy olvasható maradjon
        catChart.setData(limitSorted(byCategory, 12));
        manChart.setData(limitSorted(byManufacturer, 12));
    }

    private static Map<String, Integer> limitSorted(Map<String, Long> src, int limit) {
        return src.entrySet().stream()
            .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
            .limit(limit)
            .collect(Collectors.toMap(
                Map.Entry::getKey, e -> e.getValue().intValue(),
                (a, b) -> a, LinkedHashMap::new
            ));
    }

    private static String safeCategoryName(Product p) {
        Category c = p.getCategory();
        if (c != null && c.getName() != null && !c.getName().isBlank()) return c.getName();
        return "Ismeretlen kategória";
    }

    private static String safeManufacturerName(Product p) {
        Category c = p.getCategory();
        if (c != null) {
            Subcategory s = c.getSubcategory();
            if (s != null) {
                Manufacturer m = s.getManufacturer();
                if (m != null && m.getName() != null && !m.getName().isBlank()) return m.getName();
            }
        }
        return "Ismeretlen gyártó";
    }

    /** Egyszerű, standard Swing oszlopdiagram (külső lib nélkül). */
    static class SimpleBarChart extends JPanel {
        private Map<String, Integer> data = new LinkedHashMap<>();
        private final String title;

        // Margók
        private static final int TOP = 24;
        private static final int RIGHT = 16;
        private static final int BOTTOM = 40;
        private static final int LEFT = 40;

        public SimpleBarChart(String title) {
            this.title = title;
            setOpaque(true);
            setBackground(Color.WHITE);
        }

        public void setData(Map<String, Integer> data) {
            this.data = (data == null) ? new LinkedHashMap<>() : data;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Cím
            g2.setColor(Color.DARK_GRAY);
            g2.setFont(getFont().deriveFont(Font.PLAIN, 16f));
            FontMetrics fmTitle = g2.getFontMetrics();
            int tw = fmTitle.stringWidth(title);
            g2.drawString(title, (w - tw) / 2, 18);

            // Plot area
            int x0 = LEFT, y0 = TOP + 8;
            int pw = w - LEFT - RIGHT;
            int ph = h - (TOP + 8) - BOTTOM;

            // Rácsvonalak és max érték
            int max = Math.max(1, data.values().stream().mapToInt(i -> i).max().orElse(1));
            g2.setColor(new Color(235, 235, 235));
            int gridLines = 5;
            for (int i = 0; i <= gridLines; i++) {
                int y = y0 + (int) Math.round(ph * i / (double) gridLines);
                g2.drawLine(x0, y, x0 + pw, y);
            }

            // Tengely
            g2.setColor(new Color(90, 90, 90));
            g2.drawLine(x0, y0 + ph, x0 + pw, y0 + ph);
            g2.drawLine(x0, y0, x0, y0 + ph);

            // Oszlopok
            int n = Math.max(1, data.size());
            int gap = 8;
            int barW = Math.max(6, (pw - (n + 1) * gap) / n);

            g2.setFont(getFont().deriveFont(Font.PLAIN, 11f));
            FontMetrics fm = g2.getFontMetrics();

            int i = 0;
            for (Map.Entry<String, Integer> e : data.entrySet()) {
                int val = e.getValue();
                double ratio = val / (double) max;
                int barH = (int) Math.round(ph * ratio);

                int bx = x0 + gap + i * (barW + gap);
                int by = y0 + ph - barH;

                // oszlop
                g2.setColor(new Color(160, 160, 160));
                g2.fillRect(bx, by, barW, barH);

                // érték (kisebb értéknél a tetejére írjuk)
                String vs = String.valueOf(val);
                int vW = fm.stringWidth(vs);
                int vx = bx + (barW - vW) / 2;
                int vy = by - 4;
                if (barH < fm.getAscent() + 6) {
                    vy = y0 + ph - 4;
                }
                g2.setColor(new Color(70, 70, 70));
                g2.drawString(vs, vx, vy);

                // címke
                String label = ellipsize(e.getKey(), 12);
                int lw = fm.stringWidth(label);
                int lx = bx + (barW - lw) / 2;
                int ly = y0 + ph + fm.getAscent() + 12;
                g2.drawString(label, lx, ly);

                i++;
            }

            g2.dispose();
        }

        private static String ellipsize(String s, int max) {
            if (s == null) return "";
            if (s.length() <= max) return s;
            return s.substring(0, Math.max(0, max - 1)) + "…";
        }
    }
}
