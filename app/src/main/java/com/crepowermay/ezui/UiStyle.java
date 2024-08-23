package com.crepowermay.ezui;

import android.graphics.Color;

public class UiStyle {

    public int normalBackgroundColor;
    public int normalTextColor;
    public int pressedBackgroundColor;
    public int pressedTextColor;
    public int disabledBackgroundColor;
    public int disabledTextColor;

    public UiStyle(
        int normalBackgroundColor,
        int normalTextColor,
        int pressedBackgroundColor,
        int pressedTextColor,
        int disabledBackgroundColor,
        int disabledTextColor
    ) {
        this.normalBackgroundColor = normalBackgroundColor;
        this.normalTextColor = normalTextColor;
        this.pressedBackgroundColor = pressedBackgroundColor;
        this.pressedTextColor = pressedTextColor;
        this.disabledBackgroundColor = disabledBackgroundColor;
        this.disabledTextColor = disabledTextColor;
    }

    public UiStyle() {
        this(
                Color.GRAY,
                Color.WHITE,
                Color.DKGRAY,
                Color.LTGRAY,
                Color.LTGRAY,
                Color.DKGRAY
        );
    }

    public void applyNormalStyle(StatefulButton button) {
        button.setBackgroundColor(normalBackgroundColor);
    }
}
