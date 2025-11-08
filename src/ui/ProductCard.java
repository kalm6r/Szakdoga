package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
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
    private final JLabel middleLabel = new JLabel("<html><div style='width:150px;color:#666'>Rövid leírás…</div></html>");
    private final JLabel purchasePriceTitleLabel = new JLabel("Beszerzési ár:");
    private final JLabel purchasePriceValueLabel = new JLabel("-");
    private final JLabel purchaseDateTitleLabel = new JLabel("Beszerzés éve:");
    private final JLabel purchaseDateValueLabel = new JLabel("-");
    private final JLabel price = new JLabel("9 999 Ft");
    private final JToggleButton favoriteButton = new JToggleButton("☆");
    private static final String DISABLED_FAVORITE_TOOLTIP = "Bejelentkezéssel jelölheted kedvencnek";

    public ProductCard() {
        setBackground(Color.WHITE);
        setBorder(new CompoundBorder(new LineBorder(new Color(0xDDDDDD)), new EmptyBorder(8,8,8,8)));
        setPreferredSize(new Dimension(170, 320));

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
        name.setFont(name.getFont().deriveFont(Font.BOLD, 14f));
        int maxNameLines = 2;
        int lineHeight = name.getFontMetrics(name.getFont()).getHeight();
        int maxNameHeight = lineHeight * maxNameLines;
        name.setMaximumSize(new Dimension(Integer.MAX_VALUE, maxNameHeight));
        name.setPreferredSize(new Dimension(0, maxNameHeight));
        name.setMinimumSize(new Dimension(0, maxNameHeight));
        price.setFont(price.getFont().deriveFont(Font.BOLD));

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.add(name);
        center.add(Box.createVerticalStrut(2));
        middleLabel.setAlignmentX(LEFT_ALIGNMENT);
        middleLabel.setForeground(new Color(0x666666));
        center.add(middleLabel);
        center.add(Box.createVerticalStrut(10));

        styleInfoLabel(purchasePriceTitleLabel);
        styleInfoValue(purchasePriceValueLabel);
        center.add(purchasePriceTitleLabel);
        center.add(Box.createVerticalStrut(2));
        center.add(purchasePriceValueLabel);

        center.add(Box.createVerticalStrut(8));

        styleInfoLabel(purchaseDateTitleLabel);
        styleInfoValue(purchaseDateValueLabel);
        center.add(purchaseDateTitleLabel);
        center.add(Box.createVerticalStrut(2));
        center.add(purchaseDateValueLabel);

        favoriteButton.setFocusPainted(false);
        favoriteButton.setBorderPainted(false);
        favoriteButton.setContentAreaFilled(false);
        favoriteButton.setOpaque(false);
        favoriteButton.setMargin(new Insets(0, 0, 0, 0));
        favoriteButton.setFont(favoriteButton.getFont().deriveFont(Font.PLAIN, 20f));
        favoriteButton.setToolTipText("Kedvencek hozzáadása");
        favoriteButton.addChangeListener(e -> updateFavoriteIcon());

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(img, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        price.setBorder(new EmptyBorder(0, 0, 0, 0));
        bottom.add(price, BorderLayout.WEST);
        bottom.add(favoriteButton, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
        updateFavoriteIcon();
    }

    public void setData(String nameText, String middleText, String purchasePriceText,String purchaseDateText,String sellPriceText,Image image) {
        name.setText(nameText != null ? nameText : "");
        String safeMiddle = (middleText == null || middleText.isBlank()) ? "" : middleText;
        middleLabel.setText("<html><div style='width:150px;color:#666'>" + safeMiddle + "</div></html>");
        purchasePriceValueLabel.setText(purchasePriceText == null || purchasePriceText.isBlank() ? "-" : purchasePriceText);
        purchaseDateValueLabel.setText(purchaseDateText == null || purchaseDateText.isBlank() ? "-" : purchaseDateText);
        price.setText("Eladási ár: " + (sellPriceText == null || sellPriceText.isBlank() ? "-" : sellPriceText));
        img.setIcon(image != null ? new ImageIcon(image) : null);
    }

    private void styleInfoLabel(JLabel label) {
        label.setAlignmentX(LEFT_ALIGNMENT);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 12f));
        label.setForeground(new Color(0x333333));
    }

    private void styleInfoValue(JLabel label) {
        label.setAlignmentX(LEFT_ALIGNMENT);
        label.setFont(label.getFont().deriveFont(Font.PLAIN, 12f));
        label.setForeground(new Color(0x333333));
    }
    
    public void setFavorite(boolean favorite) {
        favoriteButton.setSelected(favorite);
        updateFavoriteIcon();
    }

    public boolean isFavoriteSelected() {
        return favoriteButton.isSelected();
    }

    public void addFavoriteToggleListener(java.awt.event.ActionListener listener) {
        favoriteButton.addActionListener(listener);
    }

    public void setFavoriteButtonVisible(boolean visible) {
        favoriteButton.setVisible(visible);
    }

    public void setFavoriteButtonEnabled(boolean enabled) {
        favoriteButton.setEnabled(enabled);
        updateFavoriteIcon();
    }

    private void updateFavoriteIcon() {
        if (!favoriteButton.isEnabled()) {
            favoriteButton.setText("☆");
            favoriteButton.setForeground(new Color(0xCCCCCC));
            favoriteButton.setToolTipText(DISABLED_FAVORITE_TOOLTIP);
            return;
        }

        if (favoriteButton.isSelected()) {
            favoriteButton.setText("★");
            favoriteButton.setForeground(new Color(0xE0A000));
            favoriteButton.setToolTipText("Kedvenc eltávolítása");
        } else {
            favoriteButton.setText("☆");
            favoriteButton.setForeground(new Color(0x999999));
            favoriteButton.setToolTipText("Kedvencek hozzáadása");
        }
    }
}