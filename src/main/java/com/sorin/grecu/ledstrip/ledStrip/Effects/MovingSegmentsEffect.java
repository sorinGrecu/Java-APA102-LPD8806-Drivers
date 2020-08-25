package com.sorin.grecu.ledstrip.ledStrip.Effects;

import com.sorin.grecu.ledstrip.ledStrip.Effects.RainbowEffect.ColorUtils;
import com.sorin.grecu.ledstrip.ledStrip.Effects.Tools.EncoderIncrementController;
import com.sorin.grecu.ledstrip.ledStrip.GenericLedStrip;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;

import static com.sorin.grecu.ledstrip.ledStrip.Effects.RainbowEffect.PositionUtils.validateStepValue;
import static com.sorin.grecu.ledstrip.ledStrip.Effects.Tools.EncoderIncrementController.ControllerType.*;


@Slf4j
@Component
public class MovingSegmentsEffect extends SegmentsEffect {

    private EncoderIncrementController encoderIncrementController = new EncoderIncrementController(4,POSITION,SPEED,SPACE,BRIGHTNESS);

    private Timer timer;
    private int minColorDuration, maxColorDuration, speed = 65;
    private double colorDecimal, speedIncrement;


    public MovingSegmentsEffect(@Autowired @Qualifier("${ledStrip.name}") GenericLedStrip ledStrip,
                                @Value("${ledStrip.minColorDuration}") final int minColorDuration,
                                @Value("${ledStrip.maxColorDuration}") final int maxColorDuration) {
        super(ledStrip);
        this.minColorDuration = minColorDuration;
        this.maxColorDuration = maxColorDuration;
        init();
    }

    public void init() {
        setSpeedIncrement(speed);
        timer = new Timer(1000 / refreshRate, ae -> {
            if (running) {
                update();
            }
        });
    }

    @Override
    public void start() {
        running = true;
        if (!timer.isRunning()) {
            timer.start();
            log.info("Started moving segments effect");
        }
    }

    @Override
    public void stop() {
        running = false;
        if (timer != null) {
            timer.stop();
            ledStrip.fill(0, 0, 0);
            ledStrip.update();
            log.info("Stopped moving segments  effect");

        }
    }


    public void update() {
        if (colorDecimal > 1) {
            colorDecimal = 0;
            moveColorsToLeft();
        }
        // we will start painting from the set position of the first color, in order for it to be able to vary
        int currentPoint = startingPosition;
        int blackCount = 0;

        // we will iterate over all the leds, even if starting painting from another position
        for (int i = 1; i <= ledCount; i++) {
            // we want to be able to isolate colors so we will paint black between them to create that effect
            if (blackCount == 0) {
                ledStrip.setLed(currentPoint,
                        colorPauseSize > 0
                                ?
                                ColorUtils.getColorFromSpectrum(
                                        colorContinuum.peekPreviousColor(),
                                        Color.BLACK,
                                        colorDecimal)
                                :
                                ColorUtils.getColorFromSpectrum(
                                        colorContinuum.peekPreviousColor(),
                                        colorContinuum.getCurrentColor(),
                                        colorDecimal));
            } else if (blackCount < colorPauseSize) {
                ledStrip.setLed(currentPoint, Color.BLACK);
            } else if (blackCount == colorPauseSize) {
                ledStrip.setLed(currentPoint,
                        colorPauseSize > 0
                                ?
                                ColorUtils.getColorFromSpectrum(
                                        Color.BLACK,
                                        colorContinuum.getCurrentColor(),
                                        colorDecimal)
                                :
                                ColorUtils.getColorFromSpectrum(
                                        colorContinuum.getCurrentColor(),
                                        colorContinuum.peekNextColor(),
                                        colorDecimal));
            } else {
                ledStrip.setLed(currentPoint, colorContinuum.getCurrentColor());
            }

            blackCount++;

            // if we've painted enough leds for this color, we will move on and restart the black counter
            // to further isolate colors
            if (i % extent == 0) {
                incrementColor();
                blackCount = 0;
            }

            currentPoint++;
            // if current position is off the led count but we still have iterations, start over from the strip beginning
            currentPoint = currentPoint % ledCount;


        }
        colorDecimal += speedIncrement;


        ledStrip.update();
    }

    private void setSpeedIncrement(int speedStep) {
        //There will be a range of 100 steps. Computing how many ms one step means based on
        //the range between max and min duration
        double oneStepInMs = Math.abs(maxColorDuration - minColorDuration) / 100D;
        //The higher the speed is set, the smaller this speed divider will be
        double speedModifierInMs = maxColorDuration - (speedStep * oneStepInMs);
        double speedModifierInS = speedModifierInMs / 1000D;
        double increment = 1 / Double.valueOf(refreshRate * speedModifierInS);
        this.speedIncrement = increment;
    }

    /**
     * @param speed integer from 0 to 100
     */
    public void setSpeed(int speed) {
        this.speed = validateStepValue(speed);
        setSpeedIncrement(this.speed);
    }


    @Override
    public void handleCustomCommand(GenericLedStrip.LedStripCommands command) {
        switch (command) {
            case UP:
                switch (encoderIncrementController.getCurrentController()) {
                    case POSITION:
                        moveColorsToRight();
                        break;
                    case SPACE:
                        decreaseColorPause();
                        break;
                    case BRIGHTNESS:
                        ledStrip.setGlobalBrightness(ledStrip.getGlobalBrightness() + 2);
                        break;
                    case SPEED:
                        setSpeed(speed+2);
                        break;
                }
                break;
            case DOWN:
                switch (encoderIncrementController.getCurrentController()) {
                    case POSITION:
                        moveColorsToLeft();
                        break;
                    case SPACE:
                        increaseColorPause();
                        break;
                    case BRIGHTNESS:
                        ledStrip.setGlobalBrightness(ledStrip.getGlobalBrightness() - 2);
                        break;
                    case SPEED:
                        setSpeed(speed-2);
                        break;
                }

                break;
            case PUSH:
                encoderIncrementController.incrementController();
                break;
            case LONG_PUSH:
                ledStrip.setRunningLedEffect(LedEffectRegistry.getNext());
                break;
        }
        reportLedStripState(encoderIncrementController.getCurrentController());
    }

    @Override
    public void reportLedStripState(EncoderIncrementController.ControllerType controllerType) {
        LedStripState ledStripState=new LedStripState();
        ledStripState.setControllerType(controllerType);
        ledStripState.setLedEffect(LedEffectRegistry.getCurrent().getClass().getSimpleName().replace("Effect",""));


        switch (encoderIncrementController.getCurrentController()) {
            case POSITION:
                ledStripState.setValue(String.valueOf(startingPosition));
                break;
            case SPACE:
                ledStripState.setValue(String.valueOf(colorPauseSize));
                break;
            case BRIGHTNESS:
                ledStripState.setValue(String.valueOf(ledStrip.getGlobalBrightness()));
                break;
            case SPEED:
                ledStripState.setValue(String.valueOf(speed));
                break;
        }
        stateUpdater.sendEncoderState(ledStripState);
    }}
