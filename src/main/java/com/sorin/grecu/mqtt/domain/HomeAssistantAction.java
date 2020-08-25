package com.sorin.grecu.mqtt.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

/**
 * Created by Sorin on 7/2/2017.
 */

@Data
@ToString
@Builder(setterPrefix = "with")
@JsonIgnoreProperties(ignoreUnknown = true)
public class HomeAssistantAction {

    private String machineName, targetedDevice, ledEffect, state,
            soundEffect, usePeak, outwards, middle, reactiveSpeed;

    private Integer length, brightness, aux;
}
