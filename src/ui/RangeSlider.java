package ui;

import javax.swing.JSlider;

/**
 * Egyszerű kétfogantyús csúszka, ahol az alsó érték a {@link #getValue()},
 * a felső pedig a {@link #getUpperValue()}.
 */
public class RangeSlider extends JSlider {
    private static final long serialVersionUID = 1L;

    private int upperValue;

    public RangeSlider() {
        this(0, 100);
    }

    public RangeSlider(int min, int max) {
        super(min, max);
        setOrientation(JSlider.HORIZONTAL);
        super.setValue(min);
        upperValue = Math.max(min, max);
        updateUI();
    }

    @Override
    public void updateUI() {
        setUI(new RangeSliderUI(this));
        updateLabelUIs();
    }

    public int getUpperValue() {
        return upperValue;
    }

    public void setUpperValue(int value) {
        int newValue = Math.min(getMaximum(), Math.max(value, getValue()));
        if (newValue == upperValue) {
            return;
        }
        int old = upperValue;
        upperValue = newValue;
        if (upperValue < getValue()) {
            super.setValue(upperValue);
        }
        repaint();
        firePropertyChange("upperValue", old, upperValue);
        fireStateChanged();
    }

    @Override
    public void setValue(int value) {
        int newValue = Math.max(getMinimum(), Math.min(value, getMaximum()));
        super.setValue(newValue);
        if (upperValue < newValue) {
            int old = upperValue;
            upperValue = newValue;
            firePropertyChange("upperValue", old, upperValue);
        }
    }

    public void setRange(int lower, int upper) {
        setMinimum(lower);
        setMaximum(upper);
        setValue(lower);
        setUpperValue(upper);
    }

    @Override
    public void setMinimum(int minimum) {
        super.setMinimum(minimum);
        if (getValue() < minimum) {
            setValue(minimum);
        }
        if (upperValue < minimum) {
            setUpperValue(minimum);
        }
    }

    @Override
    public void setMaximum(int maximum) {
        super.setMaximum(maximum);
        if (upperValue > maximum) {
            setUpperValue(maximum);
        }
        if (getValue() > maximum) {
            setValue(maximum);
        }
    }
}
