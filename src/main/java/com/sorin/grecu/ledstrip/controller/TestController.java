package com.sorin.grecu.ledstrip.controller;


import com.sorin.grecu.ledstrip.handlers.MessageHandler;
import com.sorin.grecu.ledstrip.ledStrip.Effects.LedEffectRegistry;
import com.sorin.grecu.ledstrip.ledStrip.Effects.MovingSegmentsEffect;
import com.sorin.grecu.ledstrip.ledStrip.Effects.RainbowEffect.RainbowEffect;
import com.sorin.grecu.ledstrip.ledStrip.LedStrip;
import com.sorin.grecu.mqtt.domain.HomeAssistantAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/")
public class TestController {

    @Autowired
    private MessageHandler messageHandler;

    @Autowired
    @Qualifier("${ledStrip.name}")
    private LedStrip ledStrip;

    @Autowired
    private RainbowEffect rainbowEffect;

    @Autowired
    private MovingSegmentsEffect movingSegmentsEffect;

    @GetMapping("/isup")
    public String isUp() {
        return "Oll Korrect";
    }

    @GetMapping("/toggle")
    public String toggle() {
        ledStrip.toggle();
        return "Oll Korrect";
    }

    @GetMapping("/on")
    public String setOn() {
        ledStrip.setOn();
        return "on";
    }

    @GetMapping("/off")
    public String setOff() {
        ledStrip.setOff();
        return "off";
    }

    @GetMapping("/speed/{speed}")
    public String speed(@PathVariable Integer speed) {
        rainbowEffect.setSpeed(speed);
        movingSegmentsEffect.setSpeed(speed);
        return "Oll Korrect";
    }

    @GetMapping("/length/{length}")
    public String length(@PathVariable Integer length) {
        rainbowEffect.setLength(length);
        movingSegmentsEffect.setLength(length);
        return "Oll Korrect";
    }

    @GetMapping("/brigtness/{brigtness}")
    public String brigtness(@PathVariable Integer brigtness) {
        ledStrip.setGlobalBrightness(brigtness);
        return "Oll Korrect";
    }

    @GetMapping("/effect")
    public String effect() {
        ledStrip.setRunningLedEffect(LedEffectRegistry.getNext());
        return "Oll Korrect";
    }


    @PostMapping(value = "/data", consumes = "application/json")
    public String data(@RequestBody HomeAssistantAction homeAssistantAction) {
        messageHandler.handleHomeAssistantAction(homeAssistantAction);
        return "Oll Korrect";
    }
}
