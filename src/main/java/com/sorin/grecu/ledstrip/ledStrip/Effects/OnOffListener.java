package com.sorin.grecu.ledstrip.ledStrip.Effects;


import com.sorin.grecu.ledstrip.ledStrip.ActionType;

@FunctionalInterface
public interface OnOffListener {

    void stateChanged(ActionType actionType);

}
