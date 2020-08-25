package com.sorin.grecu.ledstrip.ledStrip.Effects.RainbowEffect;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by Sorin on 7/2/2017.
 */
@Slf4j
public class PositionUtils {

    public static Integer fitIntoPercentageRange(Integer number) {
        number = Math.max(0, number);
        number = Math.min(number, 100);
        return number;
    }

    public static Double getValueFromPercentageAndValue(Double value, Integer percentage) {
        return percentage * (value / 100);
    }

    public static Integer getValueFromPercentageAndValue(Integer value, Integer percentage) {
        return Math.toIntExact(Math.round((double) percentage * ((double) value / 100D)));
    }

    public static int validateStepValue(int step) {
        return (step > 100) ? 100 : (step < 0) ? 0 : step;
    }

}
