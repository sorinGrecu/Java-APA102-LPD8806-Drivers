package com.sorin.grecu.ledstrip.ledStrip.Effects;

import com.sorin.grecu.ledstrip.ledStrip.Effects.Tools.EncoderIncrementController;
import com.sorin.grecu.ledstrip.ledStrip.LedStrip;
import com.sorin.grecu.mqtt.handlers.StateUpdater;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Sorin on 6/13/2016.
 */

public abstract class GenericLedEffect implements LedEffect {

    @Autowired
    protected StateUpdater stateUpdater;

    protected static final String STRING_SEPARATOR = ";";
    protected LedStrip ledStrip;
    protected boolean running = false;
    protected int ledCount;

    public GenericLedEffect(LedStrip ledStrip) {
        this.ledStrip = ledStrip;
        ledCount = ledStrip.getnumberOfLeds();
        LedEffectRegistry.putEffect(this);
    }

    @Getter
    protected int refreshRate = 30;

    @Override
    public void stop() {
        ledStrip.fill(0, 0, 0);
        ledStrip.update();
        running = false;

    }

    @Override
    public boolean isRunning() {
        return running;
    }


    public Map<String, String> toStringContentToMap(String toStringContent) {
        String[] properties = StringUtils.split(toStringContent, STRING_SEPARATOR);
        HashMap<String, String> propertiesMap = new HashMap<>();
        for (String property : properties) {
            String[] propKV = StringUtils.split(property, "=");
            String propName = propKV[0];
            String propValue = propKV[1];
            propertiesMap.put(propName, propValue);
        }
        return propertiesMap;
    }

    public abstract void reportLedStripState(EncoderIncrementController.ControllerType controllerType);

    }
