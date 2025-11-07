package ui;

import service.InventoryService;
import model.Product;
import model.Supply;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TopProductsPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final InventoryService inv = new InventoryService();

    private final JPanel topBar = new JPanel(null);
    private final JLabel title = new JLabel("Legdrágább termékek rangsora");

    private final JPanel chartBox = new JPanel(new BorderLayout());
    private final HBarChart chart = new HBarChart();

    public TopProductsPanel() {
        setPreferredSize(new Dimension(896, 504));
        setLayout(null);

        // Felső sáv (mint a többi panelnél)
        topBar.setBounds(61, 43, 825, 38);
        add(topBar);
        title.setFont(new Font("Segoe UI", Font.PLAIN, 23));
        title.setBounds(0, 0, 500, 36);
        topBar.add(title);

        // Diagram doboz
        chartBox.setBackground(Color.WHITE);
        chartBox.setBorder(new LineBorder(Color.BLACK, 1, true));
        chartBox.add(chart, BorderLayout.CENTER);
        chartBox.setBounds(77, 110, 800, 350);
        add(chartBox);

        reloadData();
    }

    /** Újratöltés a DB-ből: legutóbbi ár termékenként → Top 10. */
    private void reloadData() {
        List<Product> products = inv.listAllProducts();

        // Legutóbbi Supply termékenként
        Map<Integer, Supply> latest = inv.listAllSupply().stream()
                .collect(Collectors.toMap(
                        Supply::getProductId,
                        Function.identity(),
                        (a, b) -> isAfter(a.getBought(), b.getBought()) ? a : b
                ));

        // Terméknév → ár (csak ahol van ár)
        List<Map.Entry<String, Integer>> priceList = new ArrayList<>();
        for (Product p : products) {
            Supply s = latest.get(p.getId());
            if (s != null) {
                priceList.add(Map.entry(safe(p.getName()), s.getSellPrice()));
            }
        }

        // Rendezés csökkenő sorrend, Top 10
        priceList.sort((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()));
        int TOP_N = 10;
        LinkedHashMap<String, Integer> top = new LinkedHashMap<>();
        for (int i = 0; i < Math.min(TOP_N, priceList.size()); i++) {
            Map.Entry<String, Integer> e = priceList.get(i);
            top.put(e.getKey(), e.getValue());
        }

        chart.setData(top);
    }

    private static boolean isAfter(LocalDate a, LocalDate b) {
        if (a == null) return false;
        if (b == null) return true;
        return a.isAfter(b);
    }

    private static String safe(String s) { return s == null ? "" : s; }

    /** Egyszerű vízszintes oszlopdiagram (külső lib nélkül). */
    static class HBarChart extends JPanel {
        private static final int PADDING_LEFT = 180;   // hely a névcímkéknek
        private static final int PADDING_RIGHT = 24;
        private static final int PADDING_TOP = 20;
        private static final int PADDING_BOTTOM = 24;
        private static final int ROW_GAP = 10;

        private final NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("hu", "HU"));
        private LinkedHashMap<String, Integer> data = new LinkedHashMap<>();

        public HBarChart() {
            setOpaque(true);
            setBackground(Color.WHITE);
        }

        public void setData(LinkedHashMap<String, Integer> data) {
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

            // Plot terület
            int x0 = PADDING_LEFT;
            int y0 = PADDING_TOP;
            int pw = w - PADDING_LEFT - PADDING_RIGHT;
            int ph = h - PADDING_TOP - PADDING_BOTTOM;

            // Háttér rács
            g2.setColor(new Color(235, 235, 235));
            for (int i = 0; i <= 5; i++) {
                int x = x0 + (int) Math.round(pw * i / 5.0);
                g2.drawLine(x, y0, x, y0 + ph);
            }

            // Max érték
            int max = Math.max(1, data.values().stream().mapToInt(v -> v).max().orElse(1));

            // Sorok méretezése
            int n = Math.max(1, data.size());
            int rowH = Math.max(14, (ph - (n - 1) * ROW_GAP) / n);

            // Betűk
            Font labelFont = getFont().deriveFont(Font.PLAIN, 14f);
            Font valueFont = getFont().deriveFont(Font.PLAIN, 12f);
            g2.setFont(labelFont);
            FontMetrics fmLabel = g2.getFontMetrics(labelFont);
            FontMetrics fmValue = g2.getFontMetrics(valueFont);

            int i = 0;
            for (Map.Entry<String, Integer> e : data.entrySet()) {
                String name = e.getKey();
                int price = e.getValue();

                int y = y0 + i * (rowH + ROW_GAP);
                double ratio = price / (double) max;
                int barW = (int) Math.round(pw * ratio);

                // Név bal oldalt
                g2.setColor(new Color(60, 60, 60));
                String label = ellipsize(name, PADDING_LEFT - 20, fmLabel);
                g2.drawString(label, 16, y + rowH - 4);

                // Oszlop
                g2.setColor(new Color(160, 160, 160));
                g2.fillRoundRect(x0, y, barW, rowH, 8, 8);

                // Érték a végén
                g2.setFont(valueFont);
                g2.setColor(new Color(50, 50, 50));
                String vs = nf.format(price);
                int vx = x0 + barW + 8;
                int vy = y + rowH - 4;
                if (vx + fmValue.stringWidth(vs) > x0 + pw) {
                    // ha nem fér ki, a sáv elejére írjuk
                    vx = x0 + 4;
                    vy = y + rowH - 4;
                    g2.setColor(new Color(30, 30, 30));
                }
                g2.drawString(vs, vx, vy);

                i++;
            }

            g2.dispose();
        }

        private static String ellipsize(String s, int maxWidthPx, FontMetrics fm) {
            if (s == null) return "";
            if (fm.stringWidth(s) <= maxWidthPx) return s;
            String dots = "…";
            int dotsW = fm.stringWidth(dots);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < s.length(); i++) {
                if (fm.stringWidth(sb.toString()) + dotsW > maxWidthPx) break;
                sb.append(s.charAt(i));
            }
            return sb.append(dots).toString();
        }
    }
}
