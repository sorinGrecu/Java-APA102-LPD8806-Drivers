package com.sorin.grecu.ledstrip.ledStrip;


import com.sorin.grecu.ledstrip.ledStrip.Effects.LedEffect;
import com.sorin.grecu.ledstrip.ledStrip.Effects.OnOffListener;

import java.awt.*;

public interface LedStrip extends Toggleable {

    void setLed(int ledPosition, int red, int green, int blue);

    void setLed(int ledPosition, Color color);

    void setLed(int ledPosition, int red, int green, int blue, int brightness);

    void fill(int red, int green, int blue);

    void fill(int red, int green, int blue, int brightness);

    void fill(int startPosition, int endPosition, int red, int green, int blue, int brightness);

    void fill(int startPosition, int endPosition, Color color);

    void update();

    int getRed(int ledPosition);

    int getGreen(int ledPosition);

    int getBlue(int ledPosition);

    int getBrightness(int ledPosition);

    void setGlobalBrightness(int brightness);

    int getGlobalBrightness();

    int getnumberOfLeds();

    void handleCustomCommand(String customPayload);

    void setRunningLedEffect(LedEffect ledEffect);

    LedEffect getLedEffect();

    void setLedEffect(LedEffect ledEffect);

    void addOnOffListener(OnOffListener onOffListener);

    default void displayGraphics(){
        System.out.println("GRAPHICS ARE NOT AVAILABLE");
    }
}