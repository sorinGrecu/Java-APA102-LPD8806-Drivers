package com.sorin.grecu.ledstrip.ledStrip.Effects.RainbowEffect;

import java.awt.*;
import java.util.ArrayList;

/**
 * Created by Sorin on 11/7/2015.
 */
public class ColorUtils {

    public static ArrayList<Color> intArrayToColorArray(ArrayList<Integer> intAL) {
        ArrayList<Color> culori = new ArrayList<>();
        for (Integer integ : intAL) {
            culori.add(HSVtoColor(integ));
        }
        return culori;
    }

    public static Color HSVtoColor(int rgb) {
        int red = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = rgb & 0xFF;
        return new Color(red, green, blue);
    }


    public static Color getColorFromSpectrum(Color startColor, Color endColor, Double decimals) {
        decimals = decimals > 1 ? 1 : decimals;
        int red = (int) (decimals * endColor.getRed()
                + (1 - decimals) * startColor.getRed());
        int green = (int) (decimals * endColor.getGreen()
                + (1 - decimals) * startColor.getGreen());
        int blue = (int) (decimals * endColor.getBlue()
                + (1 - decimals) * startColor.getBlue());

        return new Color(validateColorRange(red), validateColorRange(green), validateColorRange(blue));
    }

    private static int validateColorRange(int color) {
        color = Math.min(250, color);
        color = Math.max(0, color);
        return color;
    }

}
