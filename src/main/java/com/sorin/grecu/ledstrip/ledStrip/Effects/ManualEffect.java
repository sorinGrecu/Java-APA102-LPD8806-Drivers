package com.sorin.grecu.ledstrip.ledStrip.Effects;

import com.sorin.grecu.ledstrip.ledStrip.Effects.RainbowEffect.ColorUtils;
import com.sorin.grecu.ledstrip.ledStrip.Effects.RainbowEffect.PositionUtils;
import com.sorin.grecu.ledstrip.ledStrip.Effects.Tools.EncoderIncrementController;
import com.sorin.grecu.ledstrip.ledStrip.GenericLedStrip;
import com.sorin.grecu.ledstrip.ledStrip.LedStrip;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.sorin.grecu.ledstrip.ledStrip.Effects.Tools.EncoderIncrementController.ControllerType.*;

/**
 * Created by Sorin on 7/22/2016.
 */
@Slf4j
@Component
public class ManualEffect extends GenericLedEffect {

    private EncoderIncrementController encoderIncrementController = new EncoderIncrementController(3, LENGTH, COLOR, BRIGHTNESS);
    private static final List<Color> colors =
            new ArrayList<>(Arrays.asList(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.ORANGE, Color.WHITE));
    private int filledLedCount=5;
    private double currentColorPosition;

    @Value("${effects.manual.speed}")
    private double speedIncrement = 0.05;


    public ManualEffect(@Autowired @Qualifier("${ledStrip.name}") LedStrip ledStrip) {
        super(ledStrip);
    }


    @Override
    public void start() {
        running = true;
        update();
    }

    @Override
    public void stop() {
        filledLedCount = 0;
        super.stop();
        ledStrip.fill(0, ledCount, Color.black);
        ledStrip.update();

    }

    private Color getContinuousColor(double id) {
        if (id < 0) {
            id = id + colors.size();
        } else if (id >= colors.size()) {
            id = id % colors.size();
        }
        int intreg = (int) id;
        double decimals = id - intreg;
        Color startColor = colors.get(intreg);
        if (intreg + 1 >= colors.size()) {
            intreg = -1;
        }
        Color endColor = colors.get(intreg + 1);

        return ColorUtils.getColorFromSpectrum(startColor, endColor, decimals);
    }

    @Override
    public void handleCustomCommand(GenericLedStrip.LedStripCommands command) {
        switch (command) {
            case UP:
                switch (encoderIncrementController.getCurrentController()) {
                    case LENGTH:
                        incrementLedFilledCount();
                        break;
                    case COLOR:
                        incrementColor();
                        break;
                    case BRIGHTNESS:
                        ledStrip.setGlobalBrightness(ledStrip.getGlobalBrightness() + 2);
                        break;
                }
                break;
            case DOWN:
                switch (encoderIncrementController.getCurrentController()) {
                    case LENGTH:
                        decrementLedFilledCount();
                        break;
                    case COLOR:
                        decrementColor();
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
        LedStripState ledStripState = new LedStripState();
        ledStripState.setControllerType(controllerType);
        ledStripState.setLedEffect(LedEffectRegistry.getCurrent().getClass().getSimpleName().replace("Effect", ""));

        switch (encoderIncrementController.getCurrentController()) {
            case LENGTH:
                ledStripState.setValue(String.valueOf(filledLedCount));
                break;
            case COLOR:
                ledStripState.setValue(getContinuousColor(currentColorPosition).getRed() + "-" +
                        getContinuousColor(currentColorPosition).getGreen() + "-" +
                        getContinuousColor(currentColorPosition).getBlue());
                break;
            case BRIGHTNESS:
                ledStripState.setValue(String.valueOf(ledStrip.getGlobalBrightness()));
                break;
        }
        stateUpdater.sendEncoderState(ledStripState);
    }

    public void incrementLedFilledCount() {
        running = true;
        if ((filledLedCount + 1) > ledCount) {
            stop();
        } else {
            filledLedCount = validateLedPosition(filledLedCount + 1);
            running = true;
            update();
        }
    }

    protected int validateLedPosition(int ledPosition) {
        ledPosition = (ledPosition < 0 ? 0 : ledPosition);
        ledPosition = (ledPosition > ledCount ? ledCount : ledPosition);
        return ledPosition;
    }


    /**
     * If this goes one step below the first LED (0) or one above the last one we will simply turn off the led strip.
     */
    public void decrementLedFilledCount() { // TODO: idea: rotation intensity: accelerate based on how often a command is received
        if ((filledLedCount - 1) < 0) {
            running = true;
            filledLedCount = ledCount;
            update();
        } else {
            filledLedCount = validateLedPosition(filledLedCount - 1);
            update();
        }
    }

    public void incrementColor() {
        currentColorPosition = currentColorPosition + speedIncrement;
        update();
    }

    public void decrementColor() {
        currentColorPosition = currentColorPosition - speedIncrement;
        update();
    }

    private void update() {
        if (!running) {
            this.filledLedCount = 0;
        }
        Math.max(0, filledLedCount);
        Math.min(ledCount, filledLedCount);
        ledStrip.fill(0, ledCount, Color.black);
        if (currentColorPosition >= colors.size()) {
            currentColorPosition = 0;
        } else if (currentColorPosition < 0) {
            currentColorPosition += colors.size();
        }
        ledStrip.fill(0, filledLedCount, getContinuousColor(currentColorPosition));

        if (filledLedCount + 1 < ledCount) {
            ledStrip.fill(filledLedCount + 1, ledCount - 1, Color.BLACK);
        }
        ledStrip.update();
    }

    public void setLength(int lengthPercentage) {
        lengthPercentage = PositionUtils.fitIntoPercentageRange(lengthPercentage);
        filledLedCount = (int) Math.round(
                PositionUtils.getValueFromPercentageAndValue(ledStrip.getnumberOfLeds() + 0.0, lengthPercentage)
        );
        update();
    }

    public void setColorBasedOnPercentage(int colorPercentage) {
        colorPercentage = PositionUtils.fitIntoPercentageRange(colorPercentage);
        currentColorPosition = (int) Math.round(
                PositionUtils.getValueFromPercentageAndValue(colors.size() - 1.0, colorPercentage)
        );
        update();
    }

}
