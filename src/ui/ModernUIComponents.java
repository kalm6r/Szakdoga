package ui;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Modern UI komponensek a letisztult megjelenéshez
 */
public class ModernUIComponents {
    
    // Színpaletta
    public static final Color PRIMARY_COLOR = new Color(70, 130, 180);      // Acélkék
    public static final Color PRIMARY_HOVER = new Color(90, 150, 200);      // Világosabb acélkék
    public static final Color PRIMARY_PRESSED = new Color(50, 110, 160);    // Sötétebb acélkék
    
    public static final Color SECONDARY_COLOR = new Color(108, 117, 125);   // Szürke
    public static final Color SECONDARY_HOVER = new Color(128, 137, 145);
    public static final Color SECONDARY_PRESSED = new Color(88, 97, 105);
    
    public static final Color SUCCESS_COLOR = new Color(40, 167, 69);       // Zöld
    public static final Color SUCCESS_HOVER = new Color(60, 187, 89);
    public static final Color SUCCESS_PRESSED = new Color(20, 147, 49);
    
    public static final Color DANGER_COLOR = new Color(220, 53, 69);        // Piros
    public static final Color DANGER_HOVER = new Color(240, 73, 89);
    public static final Color DANGER_PRESSED = new Color(200, 33, 49);
    
    public static final Color SCROLLBAR_THUMB = new Color(180, 180, 180);
    public static final Color SCROLLBAR_THUMB_HOVER = new Color(140, 140, 140);
    public static final Color SCROLLBAR_TRACK = new Color(245, 245, 245);
    
    /**
     * Modern gomb stílus alkalmazása
     */
    public static JButton createPrimaryButton(String text) {
        return createStyledButton(text, PRIMARY_COLOR, PRIMARY_HOVER, PRIMARY_PRESSED, Color.WHITE);
    }
    
    public static JButton createSecondaryButton(String text) {
        return createStyledButton(text, SECONDARY_COLOR, SECONDARY_HOVER, SECONDARY_PRESSED, Color.WHITE);
    }
    
    public static JButton createSuccessButton(String text) {
        return createStyledButton(text, SUCCESS_COLOR, SUCCESS_HOVER, SUCCESS_PRESSED, Color.WHITE);
    }
    
    public static JButton createDangerButton(String text) {
        return createStyledButton(text, DANGER_COLOR, DANGER_HOVER, DANGER_PRESSED, Color.WHITE);
    }
    
    /**
     * Kompakt méretű gombok (kisebb padding)
     */
    public static JButton createCompactPrimaryButton(String text) {
        return createCompactButton(text, PRIMARY_COLOR, PRIMARY_HOVER, PRIMARY_PRESSED, Color.WHITE);
    }
    
    public static JButton createCompactSecondaryButton(String text) {
        return createCompactButton(text, SECONDARY_COLOR, SECONDARY_HOVER, SECONDARY_PRESSED, Color.WHITE);
    }
    
    public static JButton createCompactSuccessButton(String text) {
        return createCompactButton(text, SUCCESS_COLOR, SUCCESS_HOVER, SUCCESS_PRESSED, Color.WHITE);
    }
    
    public static JButton createCompactDangerButton(String text) {
        return createCompactButton(text, DANGER_COLOR, DANGER_HOVER, DANGER_PRESSED, Color.WHITE);
    }
    
    private static JButton createStyledButton(String text, Color baseColor, Color hoverColor, Color pressedColor, Color textColor) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setBackground(baseColor);
        button.setForeground(textColor);
        button.setFont(button.getFont().deriveFont(Font.PLAIN, 14f));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        // Lekerekített sarkok
        button.putClientProperty("JButton.buttonType", "roundRect");
        
        // Hover és pressed effektek
        addButtonHoverEffects(button, baseColor, hoverColor, pressedColor);
        
        return button;
    }
    
    private static JButton createCompactButton(String text, Color baseColor, Color hoverColor, Color pressedColor, Color textColor) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setBackground(baseColor);
        button.setForeground(textColor);
        button.setFont(button.getFont().deriveFont(Font.PLAIN, 13f));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(5, 14, 5, 14)); // Kisebb padding
        
        // Lekerekített sarkok
        button.putClientProperty("JButton.buttonType", "roundRect");
        
        // Hover és pressed effektek
        addButtonHoverEffects(button, baseColor, hoverColor, pressedColor);
        
        return button;
    }
    
    private static void addButtonHoverEffects(JButton button, Color baseColor, Color hoverColor, Color pressedColor) {
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(hoverColor);
                }
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(baseColor);
                }
            }
            
            @Override
            public void mousePressed(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(pressedColor);
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                if (button.isEnabled()) {
                    if (button.contains(e.getPoint())) {
                        button.setBackground(hoverColor);
                    } else {
                        button.setBackground(baseColor);
                    }
                }
            }
        });
    }
    
    /**
     * Modern scrollbar stílus alkalmazása egy JScrollPane-re
     */
    public static void applyModernScrollbarStyle(JScrollPane scrollPane) {
        scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scrollPane.getHorizontalScrollBar().setUI(new ModernScrollBarUI());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);
    }
    
    /**
     * Modern ScrollBar UI implementáció
     */
    private static class ModernScrollBarUI extends BasicScrollBarUI {
        
        private static final int THUMB_SIZE = 8;
        private static final int THUMB_RADIUS = 4;
        
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = SCROLLBAR_THUMB;
            this.thumbHighlightColor = SCROLLBAR_THUMB_HOVER;
            this.thumbDarkShadowColor = SCROLLBAR_THUMB;
            this.trackColor = SCROLLBAR_TRACK;
        }
        
        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createInvisibleButton();
        }
        
        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createInvisibleButton();
        }
        
        private JButton createInvisibleButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            button.setMinimumSize(new Dimension(0, 0));
            button.setMaximumSize(new Dimension(0, 0));
            return button;
        }
        
        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(trackColor);
            g2.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
            g2.dispose();
        }
        
        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) {
                return;
            }
            
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Hover effekt
            if (isDragging || isThumbRollover()) {
                g2.setColor(thumbHighlightColor);
            } else {
                g2.setColor(thumbColor);
            }
            
            // Vékonyabb, lekerekített thumb
            if (scrollbar.getOrientation() == JScrollBar.VERTICAL) {
                int x = thumbBounds.x + (thumbBounds.width - THUMB_SIZE) / 2;
                g2.fillRoundRect(x, thumbBounds.y, THUMB_SIZE, thumbBounds.height, THUMB_RADIUS, THUMB_RADIUS);
            } else {
                int y = thumbBounds.y + (thumbBounds.height - THUMB_SIZE) / 2;
                g2.fillRoundRect(thumbBounds.x, y, thumbBounds.width, THUMB_SIZE, THUMB_RADIUS, THUMB_RADIUS);
            }
            
            g2.dispose();
        }
        
        @Override
        protected Dimension getMinimumThumbSize() {
            return new Dimension(THUMB_SIZE, 48);
        }
        
        @Override
        protected Dimension getMaximumThumbSize() {
            return new Dimension(THUMB_SIZE, 1000);
        }
    }
    
    /**
     * Modern text field stílus
     */
    public static void applyModernTextFieldStyle(JTextField textField) {
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        textField.setFont(textField.getFont().deriveFont(Font.PLAIN, 13f));
        
        // Focus effekt
        textField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                textField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
                    BorderFactory.createEmptyBorder(3, 7, 3, 7)
                ));
            }
            
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                textField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
                    BorderFactory.createEmptyBorder(4, 8, 4, 8)
                ));
            }
        });
    }
    
    /**
     * Kompakt text field stílus (még kisebb padding)
     */
    public static void applyCompactTextFieldStyle(JTextField textField) {
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
            BorderFactory.createEmptyBorder(2, 6, 2, 6)  // Minimális padding: 2px felső/alsó, 6px oldalsó
        ));
        textField.setFont(textField.getFont().deriveFont(Font.PLAIN, 13f));
        
        // Focus effekt
        textField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                textField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
                    BorderFactory.createEmptyBorder(1, 5, 1, 5)
                ));
            }
            
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                textField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
                    BorderFactory.createEmptyBorder(2, 6, 2, 6)
                ));
            }
        });
    }
    
    /**
     * Modern combo box stílus
     */
    public static void applyModernComboBoxStyle(JComboBox<?> comboBox) {
        comboBox.setFont(comboBox.getFont().deriveFont(Font.PLAIN, 13f));  // Kisebb betűméret
        comboBox.setBackground(Color.WHITE);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
            BorderFactory.createEmptyBorder(1, 2, 1, 2)  // Csökkentett padding
        ));
        
        // Focus effekt
        comboBox.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                comboBox.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
                    BorderFactory.createEmptyBorder(1, 3, 1, 3)
                ));
            }
            
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                comboBox.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
                    BorderFactory.createEmptyBorder(2, 4, 2, 4)
                ));
            }
        });
        
        // Renderer a dropdown elemekhez
        comboBox.setRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(
                    javax.swing.JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));  // Kisebb padding a listában
                
                if (isSelected) {
                    setBackground(PRIMARY_COLOR);
                    setForeground(Color.WHITE);
                } else {
                    setBackground(Color.WHITE);
                    setForeground(Color.BLACK);
                }
                
                return this;
            }
        });
    }
}