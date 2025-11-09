package ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.plaf.basic.BasicSliderUI;

/**
 * UI osztály kétfogantyús csúszkához.
 */
class RangeSliderUI extends BasicSliderUI {
    private final Rectangle upperThumbRect = new Rectangle();
    private boolean upperThumbSelected;

    RangeSliderUI(RangeSlider slider) {
        super(slider);
    }

    private RangeSlider getRangeSlider() {
        return (RangeSlider) slider;
    }

    @Override
    protected TrackListener createTrackListener(JSlider slider) {
        return new RangeTrackListener();
    }

    @Override
    protected void calculateThumbSize() {
        super.calculateThumbSize();
        upperThumbRect.setSize(thumbRect.width, thumbRect.height);
    }

    @Override
    protected void calculateThumbLocation() {
        super.calculateThumbLocation();
        if (slider.getOrientation() == JSlider.HORIZONTAL) {
            int upperPos = xPositionForValue(getRangeSlider().getUpperValue());
            upperThumbRect.x = upperPos - upperThumbRect.width / 2;
            upperThumbRect.y = trackRect.y + (trackRect.height - upperThumbRect.height) / 2;
        } else {
            int upperPos = yPositionForValue(getRangeSlider().getUpperValue());
            upperThumbRect.x = trackRect.x + (trackRect.width - upperThumbRect.width) / 2;
            upperThumbRect.y = upperPos - upperThumbRect.height / 2;
        }
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        super.paint(g, c);
    }

    @Override
    public void paintTrack(Graphics g) {
        super.paintTrack(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(0x4A90E2));
        if (slider.getOrientation() == JSlider.HORIZONTAL) {
            int lowerX = thumbRect.x + thumbRect.width / 2;
            int upperX = upperThumbRect.x + upperThumbRect.width / 2;
            int y = trackRect.y + (trackRect.height - 4) / 2;
            int start = Math.min(lowerX, upperX);
            int width = Math.abs(upperX - lowerX);
            g2.fillRoundRect(start, y, width, 4, 4, 4);
        } else {
            int lowerY = thumbRect.y + thumbRect.height / 2;
            int upperY = upperThumbRect.y + upperThumbRect.height / 2;
            int x = trackRect.x + (trackRect.width - 4) / 2;
            int start = Math.min(lowerY, upperY);
            int height = Math.abs(upperY - lowerY);
            g2.fillRoundRect(x, start, 4, height, 4, 4);
        }
        g2.dispose();
    }

    @Override
    public void paintThumb(Graphics g) {
        Rectangle lower = new Rectangle(thumbRect);
        super.paintThumb(g);

        Rectangle saved = new Rectangle(thumbRect);
        thumbRect.setBounds(upperThumbRect);
        super.paintThumb(g);
        thumbRect.setBounds(saved);
        thumbRect.setBounds(lower);
    }

    private int distanceTo(Rectangle rect, int x, int y) {
        int cx = rect.x + rect.width / 2;
        int cy = rect.y + rect.height / 2;
        int dx = x - cx;
        int dy = y - cy;
        return dx * dx + dy * dy;
    }

    protected class RangeTrackListener extends TrackListener {
        @Override
        public void mousePressed(MouseEvent e) {
            if (!slider.isEnabled()) {
                return;
            }
            currentMouseX = e.getX();
            currentMouseY = e.getY();

            if (upperThumbRect.contains(currentMouseX, currentMouseY)) {
                upperThumbSelected = true;
            } else if (thumbRect.contains(currentMouseX, currentMouseY)) {
                upperThumbSelected = false;
            } else {
                int distLower = distanceTo(thumbRect, currentMouseX, currentMouseY);
                int distUpper = distanceTo(upperThumbRect, currentMouseX, currentMouseY);
                upperThumbSelected = distUpper < distLower;
            }

            if (slider.getOrientation() == JSlider.HORIZONTAL) {
                offset = upperThumbSelected
                        ? currentMouseX - upperThumbRect.x
                        : currentMouseX - thumbRect.x;
            } else {
                offset = upperThumbSelected
                        ? currentMouseY - upperThumbRect.y
                        : currentMouseY - thumbRect.y;
            }
            slider.setValueIsAdjusting(true);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            slider.setValueIsAdjusting(false);
            upperThumbSelected = false;
            slider.repaint();
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (!slider.isEnabled()) {
                return;
            }
            currentMouseX = e.getX();
            currentMouseY = e.getY();
            int pos = slider.getOrientation() == JSlider.HORIZONTAL ? currentMouseX : currentMouseY;
            moveThumb(pos);
        }

        private void moveThumb(int pos) {
            if (slider.getOrientation() == JSlider.HORIZONTAL) {
                int thumbLeft = pos - offset;
                int center = thumbLeft + thumbRect.width / 2;
                int value = valueForXPosition(center);
                if (upperThumbSelected) {
                    getRangeSlider().setUpperValue(value);
                } else {
                    slider.setValue(value);
                }
            } else {
                int thumbTop = pos - offset;
                int center = thumbTop + thumbRect.height / 2;
                int value = valueForYPosition(center);
                if (upperThumbSelected) {
                    getRangeSlider().setUpperValue(value);
                } else {
                    slider.setValue(value);
                }
            }
        }
    }
}
