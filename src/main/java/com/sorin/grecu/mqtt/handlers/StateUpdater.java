package com.sorin.grecu.mqtt.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sorin.grecu.ledstrip.ledStrip.Effects.LedStripState;
import com.sorin.grecu.ledstrip.ledStrip.LedStrip;
import com.sorin.grecu.ledstrip.ledStrip.NetworkUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Slf4j
@Component
public class StateUpdater {

    private final static String hostname = NetworkUtils.getLocalHostname();
    private String LEDSTRIP_STATE_TOPIC = String.format("home-assistant/state/%s", hostname).toLowerCase();
    private String ENCODER_STATE_TOPIC = String.format("ledstrip/encoder/state", hostname).toLowerCase();


    @Autowired
    @Qualifier("${ledStrip.name}")
    private LedStrip ledStrip;

    @Autowired
    private MessageClient messageClient;

    @PostConstruct
    public void init() {
        ledStrip.addOnOffListener(actionType -> {
            messageClient.sendMessageToTopic(actionType.toString().toLowerCase(), LEDSTRIP_STATE_TOPIC);
        });
    }

    public void sendEncoderState(LedStripState ledStripState){
        try {
            messageClient.sendMessageToTopic(new ObjectMapper().writeValueAsString(ledStripState), ENCODER_STATE_TOPIC);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

    }

}
