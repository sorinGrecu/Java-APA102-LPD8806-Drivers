package com.sorin.grecu.ledstrip.ledStrip.Effects.RainbowEffect;

import com.sorin.grecu.ledstrip.ledStrip.Effects.GenericLedEffect;
import com.sorin.grecu.ledstrip.ledStrip.Effects.LedEffectRegistry;
import com.sorin.grecu.ledstrip.ledStrip.Effects.LedStripState;
import com.sorin.grecu.ledstrip.ledStrip.Effects.SoundLevel;
import com.sorin.grecu.ledstrip.ledStrip.Effects.Tools.EncoderIncrementController;
import com.sorin.grecu.ledstrip.ledStrip.GenericLedStrip;
import com.sorin.grecu.ledstrip.ledStrip.LedStrip;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static com.sorin.grecu.ledstrip.ledStrip.Effects.Tools.EncoderIncrementController.ControllerType.*;

/**
 * Created by Sorin on 6/13/2016.
 */
@Slf4j
@Component
public class RainbowEffect extends GenericLedEffect {

    @Setter
    @Getter
    private boolean middle, outwards, soundEffect, initialSoundEffect, usePeak, reactiveSpeed;

    private EncoderIncrementController encoderIncrementController = new EncoderIncrementController(3, LENGTH, SPEED, BRIGHTNESS);

    private int minColorDuration, maxColorDuration;

    @Getter
    private int speed = 65;

    @Getter
    private int length = 65;
    private double leadColorReferencePoint, lengthIncrement, speedIncrement, maxLengthMultiplier;

    private Color startColor = gradient.get(0);         // where we start
    private Color endColor = gradient.get(1);           // where we end

    private Timer timer;
    private Thread recorderThread;
    private AtomicInteger soundLevel = new AtomicInteger();
    private long lastRegularActivity, lastRelativeSilence;
    private int initialSpeed = speed;

    @Setter
    private static java.util.List<Color> gradient = Arrays.asList(
            Color.GREEN,
            Color.YELLOW,
            Color.RED,
            Color.BLUE,
            Color.ORANGE,
            Color.MAGENTA);

    public RainbowEffect(@Autowired @Qualifier("${ledStrip.name}") LedStrip ledStrip,
                         @Value("${ledStrip.minColorDuration}") final int minColorDuration,
                         @Value("${ledStrip.maxColorDuration}") final int maxColorDuration,
                         @Value("${ledStrip.maxLengthMultiplier}") final double maxLengthMultiplier,
                         @Value("${ledStrip.startingBrigthness}") final int startingBrightness) {
        super(ledStrip);
        this.minColorDuration = minColorDuration;
        this.maxColorDuration = maxColorDuration;
        this.maxLengthMultiplier = maxLengthMultiplier;
        init();
        ledStrip.setGlobalBrightness(startingBrightness);
    }

    public void init() {
        /*soundEffect = false;
        usePeak = true;
        outwards = true;
        middle = true;*/
        setLengthIncrement(length);
        setSpeedIncrement(speed);
        if (recorderThread == null) {
            recorderThread = new Thread(new SoundLevel.Recorder(this));
            recorderThread.start();
        }
        timer = new Timer(1000 / refreshRate, ae -> {
            if (speed != 0 || length != 0) {
                for (int i = 0; i < ledCount; i++) {
                    if (running) {
                        Color color = getColor(i);
                        ledStrip.setLed(i, color);
                    } else {
                        ledStrip.setOff();
                    }
                }
                ledStrip.update();
            }
        });
        log.info("Initialized rainbow effect");
    }

    public void start() {
        setRunning(true);
        if (!timer.isRunning()) {
            timer.start();
            log.info("Started rainbow effect");
        }
    }

    public void stop() {
        if (timer != null) {
            setRunning(false);
            timer.stop();
            ledStrip.fill(0, 0, 0);
            ledStrip.update();
            log.info("Stopped rainbow effect");

        }
    }

    public void setSoundLevels(float rms, float peak) {
        int level = usePeak ? Math.round(peak * 100) : Math.round(rms * 100);
        level = Math.min(level, 100);
        soundLevel.set(level);
        if (reactiveSpeed && level >= initialSpeed) {
            speed = level;
        } else {
            speed = initialSpeed;
        }

        if (level < 5) {
            lastRelativeSilence = System.currentTimeMillis();
            if (lastRelativeSilence - lastRegularActivity > 1000 * 3) {
                this.setSoundEffect(false);
            }
        } else {
            if (initialSoundEffect) {
                this.setSoundEffect(true);
                lastRegularActivity = System.currentTimeMillis();
            }
        }
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    /**
     * @param speed integer from 0 to 100
     */
    public void setSpeed(int speed) {
        this.speed = validateStepValue(speed);
        setSpeedIncrement(this.speed);
        initialSpeed = speed;
    }

    /**
     * @param length integer from 0 to 100
     */
    public void setLength(int length) {
        this.length = validateStepValue(length);
        setLengthIncrement(this.length);
    }

    private Color getColor(int ledNr) {
        if (soundEffect) {
            int numberOfLedsToFill = Math.max(2, PositionUtils.getValueFromPercentageAndValue(
                    ledStrip.getnumberOfLeds(),
                    soundLevel.get()));
            int numberOfUnlitLeds = ledStrip.getnumberOfLeds() - numberOfLedsToFill;

            if ((middle && ledNr < (numberOfUnlitLeds / 2)) ||
                    (middle && ledNr > (ledStrip.getnumberOfLeds() - (numberOfUnlitLeds / 2)))) {
                return Color.black;
            } else if (!middle && ledNr > numberOfLedsToFill) {
                return Color.black;
            }
        }
        int distanceFromOrigin = middle
                ? Math.abs(ledNr - (ledStrip.getnumberOfLeds() / 2))
                : ledNr;
        if (distanceFromOrigin < 1) {
            Color toBeReturned = getContinuousColor(leadColorReferencePoint);
            if (leadColorReferencePoint >= gradient.size()) {
                leadColorReferencePoint %= gradient.size();

            } else if (leadColorReferencePoint < 0) {
                leadColorReferencePoint += gradient.size();
            }
            leadColorReferencePoint = outwards
                    ? leadColorReferencePoint - speedIncrement
                    : leadColorReferencePoint + speedIncrement;
            return toBeReturned;
        } else {
            return getContinuousColor(Math.abs(leadColorReferencePoint) + (distanceFromOrigin * lengthIncrement));
        }
    }

    private Color getContinuousColor(double id) {
        if (id < 0) {
            id = id + gradient.size();
        } else if (id >= gradient.size()) {
            id = id % gradient.size();
        }
        int intreg = (int) id;
        double decimals = id - intreg;
        startColor = gradient.get(intreg);
        if (intreg + 1 >= gradient.size()) {
            intreg = -1;
        }
        endColor = gradient.get(intreg + 1);

        return ColorUtils.getColorFromSpectrum(startColor, endColor, decimals);
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

    private void setLengthIncrement(int lengthStep) {
        int ledCounter = middle ? ledCount / 2 : ledCount;
        //This will equal to the led strip length having the full transition between two colors
        double maxLengthIncrement = maxLengthMultiplier / Double.valueOf(ledCounter);
        //This will divide the length of the led strip to contain the whole spectrum of the colors defined in the list
        double minLengthIncrement = (gradient.size() > 0 ? gradient.size() : 1) / Double.valueOf(ledCounter);
        double stepValue = Math.abs(minLengthIncrement - maxLengthIncrement) / 100D;
        double increment = minLengthIncrement - (stepValue * lengthStep);
        this.lengthIncrement = increment;
    }

    private int validateStepValue(int step) {
        return (step > 100) ? 100 : (step < 0) ? 0 : step;
    }

    @Override
    public void handleCustomCommand(GenericLedStrip.LedStripCommands command) {
        log.debug("Handling custom command {} for Rainbow LED Effect", command);

        switch (command) {
            case UP:
                switch (encoderIncrementController.getCurrentController()) {
                    case SPEED:
                        setSpeed(speed + 2);
                        break;
                    case LENGTH:
                        setLength(length + 2);
                        break;
                    case BRIGHTNESS:
                        ledStrip.setGlobalBrightness(ledStrip.getGlobalBrightness() + 2);
                        break;
                }
                break;
            case DOWN:
                switch (encoderIncrementController.getCurrentController()) {
                    case SPEED:
                        setSpeed(speed - 2);
                        break;
                    case LENGTH:
                        setLength(length - 2);
                        break;
                    case BRIGHTNESS:
                        ledStrip.setGlobalBrightness(ledStrip.getGlobalBrightness() - 2);
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
            case SPEED:
                ledStripState.setValue(String.valueOf(speed));
                break;
            case LENGTH:
                ledStripState.setValue(String.valueOf(length));
                break;
            case BRIGHTNESS:
                ledStripState.setValue(String.valueOf(ledStrip.getGlobalBrightness()));
                break;
        }
        stateUpdater.sendEncoderState(ledStripState);
    }
}


