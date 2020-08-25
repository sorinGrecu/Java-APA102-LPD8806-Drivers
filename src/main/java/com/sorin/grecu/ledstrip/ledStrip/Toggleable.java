package com.sorin.grecu.ledstrip.ledStrip;

/**
 * Created by Sorin on 11/7/2015.
 */
public interface Toggleable {

    boolean isOn();

    boolean isOff();

    void setOn();

    void setOff();

    void toggle();
}
