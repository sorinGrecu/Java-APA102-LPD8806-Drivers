package com.sorin.grecu.ledstrip.ledStrip.Effects;/*
package com.sorin.grecu.Components.ledStrip.Effects;

import com.sorin.grecu.Components.ledStrip.Effects.RainbowEffect.ColorUtils;
import com.sorin.grecu.Components.ledStrip.Effects.RainbowEffect.RainbowEffect;
import com.sorin.grecu.Components.ledStrip.GenericLedStrip;
import com.sorin.grecu.Components.ledStrip.LedStrip;
import com.sorin.grecu.SoundLevel;
import com.sorin.grecu.Utils.PositionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;

*/
/**
 * Created by Sorin on 12/25/2016.
 *//*


@Slf4j
public class SoundEffect extends GenericLedEffect {

    @Autowired
    private RainbowEffect rainbowEffect;

    private int mode;
    private boolean usePeak = false;
    private boolean startMiddle = false;
    private Color lowColor = Color.GREEN;
    private Color peakColor = Color.RED;
    private Color emptyColor = Color.BLACK;
    private float rms;
    private float peak;
    private Thread recorderThread;

    public SoundEffect(LedStrip ledStrip) {
        super(ledStrip);
    }

    @Override
    public void start() {
        if (recorderThread == null) {
            recorderThread = new Thread(new SoundLevel.Recorder(this));
            recorderThread.start();
        }
        running = true;
    }

    @Override
    public void stop() {
        running = false;
        ledStrip.fill(0, 0, 0);
        ledStrip.update();
    }

    @Override
    public void handleCustomCommand(GenericLedStrip.LedStripCommands command) {
        log.debug("Handling custom command {} for Sound LED Effect", command);

        switch (command) {
            case UP:

                break;
            case DOWN:
                startMiddle = startMiddle ? false : true;
                break;
            case PUSH:
                usePeak = usePeak ? false : true;
                break;
            case LONG_PUSH:
                ledStrip.setRunningLedEffect(LedEffectRegistry.getNext());
                break;
        }
    }

    public void setSettings(int mode, boolean usePeak) {
        int behaviourNumber = (int) Math.round(
                PositionUtils.getValueFromPercentageAndValue(4D, mode));
        this.mode = behaviourNumber;
        this.usePeak = usePeak;
        rainbowEffect.setSpeed(50);
        rainbowEffect.setLength(50);
        ledStrip.getLedEffect().stop();

        switch (behaviourNumber) {
            case (0):
                ledStrip.setRunningLedEffect(this);
                this.startMiddle = false;
                break;
            case (1):
                ledStrip.setRunningLedEffect(this);
                this.startMiddle = true;
                break;
            case (2):
                ledStrip.setRunningLedEffect(rainbowEffect);
                break;
            case (3):
                ledStrip.setRunningLedEffect(rainbowEffect);
                break;
            case (4):
                ledStrip.setRunningLedEffect(rainbowEffect);
                break;
        }
    }

    public void setLedsOnEDT(float rms, float peak) {
        if (running) {
            this.rms = rms;
            this.peak = peak;
            int level = usePeak ? Math.round(peak * 100) : Math.round(rms * 100);
            //   int level =Math.round((rms+peak) * 50);
            level = Math.min(level, 100);
            System.out.println(level);
            // paint value to LED Strip
            switch (mode) {
                case (0):
                    paintForScale(level);
                    break;
                case (1):
                    paintForScale(level);
                    break;
                case (2):
                    ledStrip.setGlobalBrightness(level);
                    break;
                case (3):
                    rainbowEffect.setLength(level);
                    break;
                case (4):
                    rainbowEffect.setSpeed(level);
                    break;
            }
        }
    }

    @Override
    public <T extends GenericLedEffect> void redo(T effect) {
        SoundEffect soundEffect = (SoundEffect) effect;
        this.emptyColor = soundEffect.emptyColor;
        this.lowColor = soundEffect.lowColor;
        this.peakColor = soundEffect.peakColor;
        this.peak = soundEffect.peak;
        this.rms = soundEffect.rms;
        this.startMiddle = soundEffect.startMiddle;
        this.usePeak = soundEffect.usePeak;
    }

    private void paintForScale(int level) {
        int ledCount = ledStrip.getnumberOfLeds();
        int numberOfLedsOn;
        if (startMiddle) {
            numberOfLedsOn = Math.round((ledCount * 1F / 2) * (level * 1F / 100));
            Double decimalIncrement = 1D / (ledCount * 0.5D);

            // Start painting from middle to extremities
            for (int i = (ledCount / 2); i < (ledCount * 1F / 2) + numberOfLedsOn; i++) {
                int step = i - (ledCount / 2);
                Color color = ColorUtils.getColorFromSpectrum(
                        lowColor,
                        peakColor,
                        step * decimalIncrement);
                ledStrip.setLed(i, color);
                ledStrip.setLed(i - 1 - (step * 2), color);
            }
            ledStrip.fill((ledCount / 2) - 1, (ledCount / 2), lowColor);
            ledStrip.fill((ledCount / 2) + numberOfLedsOn, ledCount, emptyColor);
            ledStrip.fill(0, (ledCount / 2) - numberOfLedsOn, emptyColor);
            ledStrip.update();
        } else {
            numberOfLedsOn = Math.round(ledCount * (level * 1F / 100));
            Double decimalIncrement = 1 / (ledCount * 1D);

            // Start painting from start to end
            for (int i = 0; i < numberOfLedsOn; i++) {
                Color color = ColorUtils.getColorFromSpectrum(
                        lowColor,
                        peakColor,
                        i * decimalIncrement);
                ledStrip.setLed(i, color);
            }
            ledStrip.fill(numberOfLedsOn, ledCount, Color.BLACK);
            ledStrip.update();
        }
    }

}
*/
