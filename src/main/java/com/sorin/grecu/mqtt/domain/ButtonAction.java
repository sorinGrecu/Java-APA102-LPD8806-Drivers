package com.sorin.grecu.mqtt.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ButtonAction {

    @Getter
    @Setter
    private String type;

}
