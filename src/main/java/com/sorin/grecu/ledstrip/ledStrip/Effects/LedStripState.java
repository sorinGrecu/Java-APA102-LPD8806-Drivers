package com.sorin.grecu.ledstrip.ledStrip.Effects;

import com.sorin.grecu.ledstrip.ledStrip.Effects.Tools.EncoderIncrementController;
import lombok.Getter;
import lombok.Setter;

public class LedStripState {

    @Setter
    @Getter
    private EncoderIncrementController.ControllerType controllerType;

    @Setter
    @Getter
    private String ledEffect;

    @Setter
    @Getter
    private String value;

}
