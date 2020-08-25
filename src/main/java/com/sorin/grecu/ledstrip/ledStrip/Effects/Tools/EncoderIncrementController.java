package com.sorin.grecu.ledstrip.ledStrip.Effects.Tools;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EncoderIncrementController {

    @Getter
    private int numberOfControls;

    private int currentController;

    @Getter
    private ControllerType[] controllerTypes;

    public EncoderIncrementController(int numberOfControls, ControllerType... controllerTypes) {
        this.numberOfControls = numberOfControls;
        this.controllerTypes = controllerTypes;
    }

    public ControllerType incrementController() {
        currentController = ++currentController % numberOfControls;
        log.info("Now controlling: {}", controllerTypes[currentController].toString());
        return controllerTypes[currentController];
    }

    public ControllerType getCurrentController() {
        return controllerTypes[currentController];
    }

    public enum ControllerType {
        BRIGHTNESS, SPEED, LENGTH, POSITION, COLOR, SPACE
    }
}
