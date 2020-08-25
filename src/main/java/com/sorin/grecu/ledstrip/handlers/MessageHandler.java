package com.sorin.grecu.ledstrip.handlers;

import com.sorin.grecu.ledstrip.ledStrip.Effects.*;
import com.sorin.grecu.ledstrip.ledStrip.Effects.RainbowEffect.RainbowEffect;
import com.sorin.grecu.ledstrip.ledStrip.GenericLedStrip;
import com.sorin.grecu.ledstrip.ledStrip.LedStrip;
import com.sorin.grecu.ledstrip.ledStrip.NetworkUtils;
import com.sorin.grecu.mqtt.domain.ButtonAction;
import com.sorin.grecu.mqtt.domain.HomeAssistantAction;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

/**
 * Created by Sorin on 7/2/2017.
 */
@Component
@Slf4j
public class MessageHandler {

    @Getter
    @Autowired
    @Qualifier("${ledStrip.name}")
    private LedStrip ledStrip;

    @Getter
    @Autowired
    private RainbowEffect rainbowEffect;

    @Getter
    @Autowired
    private ManualEffect manualEffect;

    @Getter
    @Autowired
    private SegmentsEffect segmentsEffect;

    @Getter
    @Autowired
    private MovingSegmentsEffect movingSegmentsEffect;

    public void handleHomeAssistantAction(HomeAssistantAction homeAssistantAction) {
        log.info("Handling home assistant action {}", homeAssistantAction.toString());
        switch (homeAssistantAction.getTargetedDevice().toUpperCase()) {
            case ("LEDSTRIP"): {
                handleLedStripAction(homeAssistantAction);
                break;
            }
            case ("AC"): {
                handleACAction(homeAssistantAction);
                break;
            }
        }
    }

    public void handleRemoteRotaryEncoder(String rotaryAction) {
        switch (rotaryAction.toUpperCase()) {
            case ("LEFT"): {
                LedEffectRegistry.getCurrent().handleCustomCommand(GenericLedStrip.LedStripCommands.DOWN);
                break;
            }
            case ("RIGHT"): {
                LedEffectRegistry.getCurrent().handleCustomCommand(GenericLedStrip.LedStripCommands.UP);
                break;
            }
        }
    }

    public void handleRemoteRotaryButton(String buttonAction) {
        switch (buttonAction.toUpperCase()) {
            case ("SINGLE"):
            case ("DOUBLE"):
            case ("TRIPLE"): {
                LedEffectRegistry.getCurrent().handleCustomCommand(GenericLedStrip.LedStripCommands.PUSH);
                break;
            }
            case ("LONG"): {
                LedEffectRegistry.getCurrent().handleCustomCommand(GenericLedStrip.LedStripCommands.LONG_PUSH);
                break;
            }
        }
    }

    public void handleRemoteRedButton(String buttonAction) {
        ledStrip.toggle();
    }

    public void handleRemoteGreenButton(String buttonAction) {
    }

    private void handleACAction(HomeAssistantAction homeAssistantAction) {
        try {
            NetworkUtils.executeSystemCommand("");
        } catch (IOException e) {
            log.error("Could not execute system command");
        }
    }

    private void handleLedStripAction(HomeAssistantAction homeAssistantAction) {
        String ledEffectName = homeAssistantAction.getLedEffect();
        Optional<LedEffect> ledEffect = LedEffectRegistry.getByName(ledEffectName);

        ledStrip.setGlobalBrightness(homeAssistantAction.getBrightness());

        if (ledEffect.isPresent()) {
            ledStrip.setRunningLedEffect(ledEffect.get());
        } else {
            log.error("Could not translate led effect name {} to a led effect", ledEffectName);
        }
        log.info("Led effect: {} with state {}", ledEffectName, homeAssistantAction.getState());
        switch (ledEffectName.toUpperCase()) {
            case ("RAINBOW"): {
                rainbowEffect.setLength(homeAssistantAction.getLength());
                rainbowEffect.setSpeed(homeAssistantAction.getAux());
                break;
            }
            case ("MANUAL"): {
                manualEffect.setColorBasedOnPercentage(homeAssistantAction.getAux());
                manualEffect.setLength(homeAssistantAction.getLength());
                break;
            }
            case ("SEGMENTS"): {
                segmentsEffect.setLength(homeAssistantAction.getLength());
                segmentsEffect.setPosition(homeAssistantAction.getAux());
                break;
            }
            case ("MOVINGSEGMENTS"): {
                movingSegmentsEffect.setLength(homeAssistantAction.getLength());
                movingSegmentsEffect.setSpeed(homeAssistantAction.getAux());
                break;
            }
        }

        switch (homeAssistantAction.getState().toUpperCase()) {
            case ("ON"): {
                ledStrip.setOn();
                break;
            }
            case ("OFF"): {
                ledStrip.setOff();
                break;
            }
            case ("TOOGLE"): {
                ledStrip.toggle();
                break;
            }
        }

        if (homeAssistantAction.getMiddle() != null) {
            switch (homeAssistantAction.getMiddle().toUpperCase()) {
                case ("ON"): {
                    rainbowEffect.setMiddle(true);
                    break;
                }
                case ("OFF"): {
                    rainbowEffect.setMiddle(false);
                    break;
                }
            }
        }

        if (homeAssistantAction.getSoundEffect() != null) {
            switch (homeAssistantAction.getSoundEffect().toUpperCase()) {
                case ("ON"): {
                    rainbowEffect.setSoundEffect(true);
                    rainbowEffect.setInitialSoundEffect(true);
                    break;
                }
                case ("OFF"): {
                    rainbowEffect.setSoundEffect(false);
                    rainbowEffect.setInitialSoundEffect(false);
                    break;
                }
            }
        }

        if (homeAssistantAction.getOutwards() != null) {
            switch (homeAssistantAction.getOutwards().toUpperCase()) {
                case ("ON"): {
                    rainbowEffect.setOutwards(true);
                    break;
                }
                case ("OFF"): {
                    rainbowEffect.setOutwards(false);
                    break;
                }
            }
        }

        if (homeAssistantAction.getUsePeak() != null) {
            switch (homeAssistantAction.getUsePeak().toUpperCase()) {
                case ("ON"): {
                    rainbowEffect.setUsePeak(true);
                    break;
                }
                case ("OFF"): {
                    rainbowEffect.setUsePeak(false);
                    break;
                }
            }
        }

        if (homeAssistantAction.getReactiveSpeed() != null) {
            switch (homeAssistantAction.getReactiveSpeed().toUpperCase()) {
                case ("ON"): {
                    rainbowEffect.setReactiveSpeed(true);
                    break;
                }
                case ("OFF"): {
                    rainbowEffect.setReactiveSpeed(false);
                    break;
                }
            }
        }

    }

}
