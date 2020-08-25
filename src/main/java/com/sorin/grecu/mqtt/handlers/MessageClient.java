package com.sorin.grecu.mqtt.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sorin.grecu.ledstrip.handlers.MessageHandler;
import com.sorin.grecu.ledstrip.ledStrip.NetworkUtils;
import com.sorin.grecu.mqtt.domain.HomeAssistantAction;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 * Created by Sorin on 6/24/2017.
 */
@Component
@Slf4j
public class MessageClient implements MqttCallbackExtended {

    private final static String hostname = NetworkUtils.getLocalHostname();
    private static final Boolean SUBSCRIBER = true;
    private static final int SUB_QOS = 0;
    @Autowired
    MessageHandler messageHandler;
    @Value("${mqtt.broker.url}")
    private String BROKER_URL = "tcp://192.168.0.130:1883";
    private String ROOT_TOPIC_URI = String.format("%s/#", hostname).toLowerCase();
    private MqttAsyncClient mqttClient;
    private MqttConnectOptions connOpt;

    @PostConstruct
    public void init() {
        try {
            log.info("Initiating MQTT message client...");
            String clientID = hostname;
            connOpt = new MqttConnectOptions();

            connOpt.setCleanSession(true);
            connOpt.setMaxInflight(40);
            connOpt.setConnectionTimeout(60);
            connOpt.setAutomaticReconnect(true);

            try {
                mqttClient = new MqttAsyncClient(BROKER_URL, clientID);
                mqttClient.setCallback(this);

                mqttClient.connect(connOpt).waitForCompletion();
                log.info("MQTT connection is up!");
                if (SUBSCRIBER) {
                    mqttClient.subscribe(ROOT_TOPIC_URI, SUB_QOS);
                }
            } catch (MqttException e) {
                log.error("Connection to MQTT is down", e);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());
        }
    }

    @Override
    public void connectionLost(Throwable throwable) {
        try {
            throwable.printStackTrace();
            log.info("Attempting reconnect to the MQTT server");
            mqttClient.reconnect();
        } catch (MqttException e) {
            log.error("Failed to reconnect to mqtt", e);
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) {
        log.info("Received message from MQTT topic {} with payload {}", topic, mqttMessage.toString());
        try {
            if (topic.contains("home-assistant")) {
                HomeAssistantAction homeAssistantAction;
                homeAssistantAction = new ObjectMapper()
                        .readValue(mqttMessage.toString(), HomeAssistantAction.class);
                String hostname = NetworkUtils.getLocalHostname();
                if (hostname.equalsIgnoreCase(homeAssistantAction.getMachineName())) {
                    messageHandler.handleHomeAssistantAction(homeAssistantAction);
                } else {
                    log.info("This machine name {} does not match the destination {}", hostname, homeAssistantAction.getMachineName());
                }
            } else if (topic.contains("rotary/encod")) {
                messageHandler.handleRemoteRotaryEncoder(mqttMessage.toString());
            } else if (topic.contains("command")) {
                switch (mqttMessage.toString().toLowerCase()) {
                    case "on": {
                        messageHandler.getLedStrip().setOn();
                        break;
                    }
                    case "off": {
                        messageHandler.getLedStrip().setOff();
                        break;
                    }
                    default: {
                        log.error("Message should be on or off, received {}", mqttMessage.toString());
                    }
                }
            } else if (topic.contains("rotary/button")) {
                messageHandler.handleRemoteRotaryButton(mqttMessage.toString());
            } else if (topic.contains("red-button")) {
                messageHandler.handleRemoteRedButton(mqttMessage.toString());
            } else if (topic.contains("green-button")) {
                messageHandler.handleRemoteGreenButton(mqttMessage.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.error("COULDN'T DO SOME SHIT");
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }


    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        if (reconnect) {
            try {
                mqttClient.subscribe(ROOT_TOPIC_URI, SUB_QOS);
            } catch (MqttException e) {
                log.error("Could not reconnect to MQTT server");
            }
        }
    }

    public void sendMessageToTopic(String message, String topic) {
        try {
            mqttClient.publish(topic, new MqttMessage(message.getBytes()));
        } catch (MqttException e) {
            log.error("Could not send message {} to topic {} : {}", message, topic, e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        HomeAssistantAction homeAssistantAction = HomeAssistantAction.builder()
                .withTargetedDevice("LEDSTRIP")
                .withLedEffect("MOVINGSEGMENTS")
                .withAux(50)
                .withBrightness(50)
                .withLength(50)
                .withMachineName("raspberrypi4B")
                .withState("toggle")
                .build();

        try {
            System.out.println(new ObjectMapper().writeValueAsString(homeAssistantAction));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

}
