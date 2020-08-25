package com.sorin.grecu.ledstrip.ledStrip;


/**
 * Created by Sorin on 4/20/2016.
 */
public enum ActionType {

    ON("on"),
    OFF("off"),
    TOGGLE("toggle"),
    INFRARED("infrared"),
    ENCODER("ENCODER"),
    RAINBOW("rainbow"),
    LOGGING("logging"),
    @Deprecated
    TIMED_ON("timed_on", true),
    @Deprecated
    TIMED_OFF("timed_off", true);

    private boolean isTimed;
    private String name;

    ActionType(String name, boolean isTimed) {
        this.isTimed = isTimed;
    }

    ActionType(String name) {
        this.name = name;
    }

    public boolean isTimed() {
        return isTimed;
    }

}
