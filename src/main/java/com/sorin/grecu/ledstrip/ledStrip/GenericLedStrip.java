package com.sorin.grecu.ledstrip.ledStrip;

import com.jogamp.common.util.ArrayHashSet;
import com.sorin.grecu.ledstrip.ledStrip.Effects.LedEffect;
import com.sorin.grecu.ledstrip.ledStrip.Effects.OnOffListener;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.awt.*;
import java.util.List;

/**
 * Created by Sorin on 6/12/2016.
 */
@Slf4j
public abstract class GenericLedStrip implements LedStrip {

    @Setter
    @Getter
    protected LedEffect ledEffect;

    @Getter
    @Value("${ledStrip.led.count}")
    protected int numberOfLeds;

    private List<OnOffListener> onOffListeners = new ArrayHashSet<>();

    public GenericLedStrip() {
        /* Adding a dummy component to be able to follow its status when creating the status map @{OutputRegistry}
         */
    }

    @Override
    public void fill(int red, int green, int blue) {
        for (int i = 0; i < numberOfLeds; i++) {
            setLed(i, red, green, blue);
        }
    }

    @Override
    public void fill(int red, int green, int blue, int brightness) {
        fill(0, numberOfLeds, red, green, blue, brightness);
    }

    @Override
    public void fill(int startPosition, int endPosition, int red, int green, int blue, int brightness) {
        for (int i = startPosition; i < endPosition; i++) {
            setLed(i, red, green, blue, brightness);
        }
    }

    @Override
    public void fill(int startPosition, int endPosition, Color color) {
        fill(startPosition, endPosition, color.getRed(), color.getGreen(), color.getBlue(), getGlobalBrightness());
    }

    @Override
    public boolean isOn() {
        return ledEffect.isRunning();
    }

    @Override
    public boolean isOff() {
        return !ledEffect.isRunning();
    }

    @Override
    public void setOn() {
        if (ledEffect != null && !ledEffect.isRunning()) {
            ledEffect.start();
            triggerOnOffListeners(ActionType.ON);
        }
    }

    @Override
    public void setOff() {
        if (ledEffect != null && ledEffect.isRunning()) {
            ledEffect.stop();
        }
        // Quick fix for the latent green led
        // setLed(getnumberOfLeds() - 1, Color.red);
        update();
        fill(0, 0, 0);
        update();
        triggerOnOffListeners(ActionType.OFF);
    }

    @Override
    public void setRunningLedEffect(LedEffect ledEffect) {
        if (this.ledEffect != null && !this.ledEffect.getClass().getSimpleName().
                equals(ledEffect.getClass().getSimpleName())) {
            this.ledEffect.stop();
        }
        this.ledEffect = ledEffect;
        this.ledEffect.start();
    }

    @Override
    public int getnumberOfLeds() {
        return numberOfLeds;
    }

    @Override
    public void toggle() {
        if (isOn()) {
            setOff();
        } else {
            setOn();
        }
    }

    @Override
    public void handleCustomCommand(String customPayload) {
        try {
            LedStripCommands command = LedStripCommands.valueOf(customPayload);
            ledEffect.handleCustomCommand(command);
        } catch (IllegalArgumentException e) {
            log.error("No led strip command named {} exists", customPayload);
        }
    }

    public enum LedStripCommands {
        UP, DOWN, PUSH, DOUBLE, LONG_PUSH;
    }

    public void addOnOffListener(OnOffListener onOffListener) {
        onOffListeners.add(onOffListener);
    }

    private void triggerOnOffListeners(ActionType actionType) {
        onOffListeners.forEach(onOffListener -> onOffListener.stateChanged(actionType));
    }

}


