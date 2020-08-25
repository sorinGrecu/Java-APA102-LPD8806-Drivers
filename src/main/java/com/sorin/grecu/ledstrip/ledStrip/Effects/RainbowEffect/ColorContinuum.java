package com.sorin.grecu.ledstrip.ledStrip.Effects.RainbowEffect;


import lombok.NoArgsConstructor;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

@NoArgsConstructor
public class ColorContinuum {

    protected static java.util.List<Color> colors =
            new ArrayList<>(Arrays.asList(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW));

    private int currentColorIndex;

    public ColorContinuum(ArrayList<Color> colors) {
        this.colors = colors;
    }

    public Color getCurrentColor() {
        return colors.get(currentColorIndex);
    }

    public Color getNextColor() {
        if (++currentColorIndex >= colors.size()) {
            currentColorIndex %= colors.size();
        }
        return getCurrentColor();
    }

    public Color getPreviousColor() {
        if (--currentColorIndex < 0) {
            currentColorIndex = colors.size()-1;
        }
        return getCurrentColor();
    }

    public Color peekPreviousColor() {
        if (currentColorIndex == 0) {
            return colors.get(colors.size() - 1);
        }
        return colors.get(currentColorIndex - 1);
    }

    public Color peekNextColor() {
        if (currentColorIndex == colors.size() - 1) {
            return colors.get(0);
        }
        return colors.get(currentColorIndex + 1);
    }

    public int getColorCount() {
        return colors.size();
    }
}
