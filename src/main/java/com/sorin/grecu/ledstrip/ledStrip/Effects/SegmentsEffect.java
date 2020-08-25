package com.sorin.grecu.ledstrip.ledStrip.Effects;

import com.sorin.grecu.ledstrip.ledStrip.Effects.RainbowEffect.ColorContinuum;
import com.sorin.grecu.ledstrip.ledStrip.Effects.RainbowEffect.PositionUtils;
import com.sorin.grecu.ledstrip.ledStrip.Effects.Tools.EncoderIncrementController;
import com.sorin.grecu.ledstrip.ledStrip.GenericLedStrip;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.awt.*;

import static com.sorin.grecu.ledstrip.ledStrip.Effects.Tools.EncoderIncrementController.ControllerType.*;

/**
 * Created by Sorin on 7/30/2016.
 */

@Slf4j
@Component
public class SegmentsEffect extends GenericLedEffect {

    protected static final ColorContinuum colorContinuum = new ColorContinuum();
    protected final int extent = ledCount / colorContinuum.getColorCount();
    protected int colorPauseSize;
    protected int startingPosition;
    private EncoderIncrementController encoderIncrementController = new EncoderIncrementController(3, POSITION, SPACE, BRIGHTNESS);

    public SegmentsEffect(@Autowired @Qualifier("${ledStrip.name}") GenericLedStrip ledStrip) {
        super(ledStrip);
        setLength(50);
    }

    @Override
    public void start() {
        running = true;
        update();
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

    public void update() {
        // we will start painting from the set position of the first color, in order for it to be able to vary
        int currentPoint = startingPosition;
        int blackCount = 0;
        // we will iterate over all the leds, even if starting painting from another position
        for (int i = 1; i <= ledCount; i++) {
            // we want to be able to isolate colors so we will paint black between them to create that effect
            if (blackCount < colorPauseSize) {
                ledStrip.setLed(currentPoint, Color.BLACK);
                blackCount++;
            } else {
                ledStrip.setLed(currentPoint, colorContinuum.getCurrentColor());
            }
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
        ledStrip.update();
    }

    @Override
    public void stop() {
        super.stop();
        ledStrip.fill(0, ledCount, Color.black);
        //  colorPauseSize = extent;
        update();
        ledStrip.update();
    }

    @Override
    public void reportLedStripState(EncoderIncrementController.ControllerType controllerType) {
        LedStripState ledStripState = new LedStripState();
        ledStripState.setControllerType(controllerType);
        ledStripState.setLedEffect(LedEffectRegistry.getCurrent().getClass().getSimpleName().replace("Effect", ""));

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
        }
        stateUpdater.sendEncoderState(ledStripState);
    }

    public void incrementColor() {
        colorContinuum.getNextColor();
    }

    public boolean increaseColorPause() {
        if (++colorPauseSize < extent) {
            update();
            return true;
        } else {
            colorPauseSize--;
            return false;
        }
    }

    public boolean decreaseColorPause() {
        if (--colorPauseSize >= 0) {
            update();
            return true;
        } else {
            colorPauseSize++;
            return false;
        }
    }

    public void moveColorsToLeft() {
        startingPosition--;
        if (startingPosition < 0) {
            startingPosition = ledCount - 1;
        } else {
            update();
        }
    }

    public void moveColorsToRight() {
        startingPosition++;
        if (startingPosition >= ledCount) {
            startingPosition = startingPosition % ledCount;
        } else {
            update();
        }
    }

    public void setLength(int lengthPercentange) {
        lengthPercentange = PositionUtils.fitIntoPercentageRange(lengthPercentange);
        colorPauseSize = (int) Math.round(
                PositionUtils.getValueFromPercentageAndValue(extent + 0.0, lengthPercentange));
        update();
    }

    public void setPosition(int positionPercentage) {
        positionPercentage = PositionUtils.fitIntoPercentageRange(positionPercentage);
        startingPosition = (int) Math.round(
                PositionUtils.getValueFromPercentageAndValue(ledCount + 0.0, positionPercentage)
        );
        update();
    }

}
