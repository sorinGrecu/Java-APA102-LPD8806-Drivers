package com.sorin.grecu.ledstrip.ledStrip;

import com.pi4j.wiringpi.Spi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Slf4j
@Component
@Qualifier("APA102")
public class APA102 extends GenericLedStrip {

    private final static byte startFrame[] = {0x00, 0x00, 0x00, 0x00};
    private byte[] ledFrame;
    private byte[] endFrame;
    private int globalBrightness = 100;

    public APA102(@Value("${ledStrip.led.count}") final int numberOfLeds) {
        this.numberOfLeds = numberOfLeds;
        endFrame = new byte[numberOfLeds / 16];
        ledFrame = new byte[numberOfLeds * 4];
        for (int i = 0; i < endFrame.length; i++) {
            endFrame[i] = (byte) 0xFF;
        }
        log.info("Initialized APA102 driver");
        fill(250, 250, 250);
    }

    public void setLed(int ledPosition, int red, int green, int blue) {
        setLed(ledPosition, red, green, blue, globalBrightness);
    }

    @Override
    public void setLed(int ledPosition, Color color) {
        setLed(ledPosition, color.getRed(), color.getGreen(), color.getBlue());
    }

    /**
     * set individual led color and brightness
     *
     * @param ledPosition the position of the led on the Led Strip
     * @param red         the value of the red, from 0 to 255
     * @param green       the value of the green, from 0 to 255
     * @param blue        the value of the blue, from 0 to 255
     * @param brightness  the brightness of the led, from 0 to 100
     */
    @Override
    public void setLed(int ledPosition, int red, int green, int blue, int brightness) {
        ledPosition = ledPosition * 4;
        if (brightness < 0 || brightness > 100) {
            brightness = globalBrightness;
        }
        if (red < 0 || red > 255) {
            throw new IllegalArgumentException("Invalid red level, must be between 0 and 255");
        }
        if (green < 0 || green > 255) {
            throw new IllegalArgumentException("Invalid green level, must be between 0 and 255");
        }
        if (blue < 0 || blue > 255) {
            throw new IllegalArgumentException("Invalid blue level, must be between 0 and 255");
        }
        // convert from 0-100 brightness to 0-31
        brightness = (int) (brightness * 0.31);

        ledFrame[ledPosition] = (byte) (224 + brightness);
        ledFrame[ledPosition + 1] = (byte) blue;
        ledFrame[ledPosition + 2] = (byte) green;
        ledFrame[ledPosition + 3] = (byte) red;
    }

    @Override
    public int getRed(int ledPosition) {
        ledPosition = ledPosition * 4;
        return Byte.toUnsignedInt(ledFrame[ledPosition + 3]);
    }

    @Override
    public int getGreen(int ledPosition) {
        ledPosition = ledPosition * 4;
        return Byte.toUnsignedInt(ledFrame[ledPosition + 2]);
    }

    @Override
    public int getBlue(int ledPosition) {
        ledPosition = ledPosition * 4;
        return Byte.toUnsignedInt(ledFrame[ledPosition + 1]);
    }

    /**
     * get brightness for an individual led
     *
     * @param ledNumber the position of the led on the led strip
     * @return an int with the value of the brightness, between 0 and 100
     */
    @Override
    public int getBrightness(int ledNumber) {
        int ledPosition = ledNumber * 4;
        int brightnessIntValue = Byte.toUnsignedInt(ledFrame[ledPosition]) - 224;
        return (int) (brightnessIntValue / 0.31);
    }

    //TODO: investigate inner workings of brightness bits
    @Override
    public void setGlobalBrightness(int brightness) {
        if (brightness < 5 || brightness > 100) {
            log.warn("Invalid global brightness level {}, must be between 5 and 100", brightness);
            brightness = Math.max(5, brightness);
            brightness = Math.min(100, brightness);
        }
        globalBrightness = brightness;
        for (int i = 0; i < numberOfLeds; i++) {
            setLed(i, getRed(i), getGreen(i), getBlue(i), brightness);
        }
        update();

    }

    @Override
    public int getGlobalBrightness() {
        return globalBrightness;
    }


    @Override
    public void update() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(startFrame);
            outputStream.write(ledFrame);
            outputStream.write(endFrame);
            if (NetworkUtils.isMachineRpi()) {
                Spi.wiringPiSPIDataRW(0, outputStream.toByteArray());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] getApaColorData(Color color, byte brightness) {
        int rgb = color.getRGB();
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        return new byte[]{brightness, (byte) r, (byte) g, (byte) b};
    }
}
