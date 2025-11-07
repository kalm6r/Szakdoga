package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class ProductCard extends JPanel {
    public static final int IMG_W = 180;
    public static final int IMG_H = 110;

    private final JLabel img = new JLabel();
    private final JTextArea name = new JTextArea("Termék neve");
    private final JLabel desc = new JLabel("<html><div style='width:150px;color:#666'>Rövid leírás…</div></html>");
    private final JLabel price = new JLabel("9 999 Ft");

    public ProductCard() {
        setBackground(Color.WHITE);
        setBorder(new CompoundBorder(new LineBorder(new Color(0xDDDDDD)), new EmptyBorder(8,8,8,8)));
        setPreferredSize(new Dimension(170, 230));

        setLayout(new BorderLayout(8, 8));
        img.setHorizontalAlignment(SwingConstants.CENTER);
        img.setPreferredSize(new Dimension(IMG_W, IMG_H));
        img.setBorder(new LineBorder(new Color(0xCCCCCC)));

        name.setEditable(false);
        name.setWrapStyleWord(true);
        name.setLineWrap(true);
        name.setOpaque(false);
        name.setBorder(null);
        name.setAlignmentX(LEFT_ALIGNMENT);
        name.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        name.setFont(name.getFont().deriveFont(Font.BOLD, 14f));
        price.setFont(price.getFont().deriveFont(Font.BOLD));

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.add(name);
        center.add(Box.createVerticalStrut(4));
        center.add(desc);

        add(img, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
        add(price, BorderLayout.SOUTH);
    }

    public void setData(String nameText, String descText, String priceText, Image image) {
        name.setText(nameText);
        desc.setText("<html><div style='width:150px;color:#666'>" + descText + "</div></html>");
        price.setText(priceText);
        img.setIcon(image != null ? new ImageIcon(image) : null);
    }
}


