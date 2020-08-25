package com.sorin.grecu.ledstrip.ledStrip.Effects;


import com.sorin.grecu.ledstrip.ledStrip.Effects.Tools.EncoderIncrementController;
import com.sorin.grecu.ledstrip.ledStrip.GenericLedStrip;

/**
 * Created by Sorin on 6/12/2016.
 */
public interface LedEffect {

    boolean isRunning();

    void start();

    void stop();

    void handleCustomCommand(GenericLedStrip.LedStripCommands command);

    default <T extends GenericLedEffect> void redo(T effect) {

    }

    void reportLedStripState(EncoderIncrementController.ControllerType controllerType);
}
