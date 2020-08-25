package com.sorin.grecu.ledstrip.ledStrip;

import com.pi4j.wiringpi.Spi;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.awt.*;

@Slf4j
@Component
@Qualifier("LPD8806")
public class LPD8806 extends GenericLedStrip {

    private final int GAMMA_LENGTH = 256;
    private final byte[] GAMMA = new byte[GAMMA_LENGTH];
    private RGBLed[] ledBuffer;
    private Color[] ledColors;

    @Setter
    @Getter
    private int brightness = 100;
    private boolean suspendUpdates = false;

    public LPD8806() {
        numberOfLeds = 60;
        for (int i = 0; i < GAMMA_LENGTH; i++) {
            int j = (int) (Math.pow(((float) i) / 255.0, 2.5) * 127.0 + 0.5);
            GAMMA[i] = (byte) (0x80 | j);
        }

        if (brightness < 0 || brightness > 100) {
            log.warn("Brighness must be between 0 and 100. Attempted to set brightness to " + brightness);
        }
        this.ledBuffer = new RGBLed[numberOfLeds];
        this.ledColors = new Color[numberOfLeds];
        for (int i = 0; i < numberOfLeds; i++) {
            ledBuffer[i] = new RGBLed();
        }
        log.info("Initialized LPD8806 driver");
    }

    /**
     * @param suspendUpdates if true, the trip wil ignore updates
     */
    public void setSuspendUpdates(boolean suspendUpdates) {
        this.suspendUpdates = suspendUpdates;
    }

    public void fill(final Color color) {
        log.debug("TRYING TO FILL WITH COLOR " + color.toString());
        fill(color.getRed(), color.getGreen(), color.getBlue());
    }

    /**
     * Fill a part of the led strip with a specified color and set the
     * brightness.
     *
     * @param red        value between 0 and 255 for the red led
     * @param green      value between 0 and 255 for the green led
     * @param blue       value between 0 and 255 for the blue led
     * @param start      the start led position in the led strip
     * @param end        the end led position in the led strip
     * @param brightness value between 0 and 1 for the brightness
     * @throws IllegalArgumentException
     */
    @Override
    public void fill(final int start, final int end, final int red, final int green, final int blue,
                     final int brightness) throws IllegalArgumentException {

        if (red < 0 || green < 0 || blue < 0 || red > 255 || green > 255 || blue > 255) {
            throw new IllegalArgumentException("Red, green and blue values must be between 0 and 255.");
        }

        if (start < 0 || end > numberOfLeds) {
            throw new IllegalArgumentException("Led start must be greater then 0, end must be smaller then " + (numberOfLeds) + ".");
        }

        if (end < start) {
            throw new IllegalArgumentException("End must be greater then or equal as start.");
        }

        for (int i = start; i < end; i++) {
            setLed(i, red, green, blue, brightness);
        }
    }

    /**
     * Set the color of an individual led.
     *
     * @param number the number of the led in the led strip
     * @param red    value between 0 and 255 for the red led
     * @param green  value between 0 and 255 for the green led
     * @param blue   value between 0 and 255 for the blue led
     */
    public void setLed(final int number, final int red, final int green, final int blue) {
        setLed(number, red, green, blue, brightness);
    }

    public void setLed(final int number, final Color color) {
        setLed(number, color.getRed(), color.getGreen(), color.getBlue(), brightness);
    }

    /**
     * Switch a led off.
     *
     * @param number the number of the led in the led strip
     */
    public void setLedOff(final int number) {
        setLed(number, 0, 0, 0, 0);
    }

    /**
     * Set the color and brightness of an individual led.
     *
     * @param number     the number of the led in the led strip
     * @param red        value between 0 and 255 for the red led
     * @param green      value between 0 and 255 for the green led
     * @param blue       value between 0 and 255 for the blue led
     * @param brightness value between 0 and 1 for the brightness
     */
    public void setLed(final int number, int red, int green, int blue, int brightness) {
        if (number < 0 || number > numberOfLeds) {
            log.error("Invalid value {}.Led number must be between {} and {}", number, 0, numberOfLeds);
        }
        ledColors[number] = new Color(red, green, blue);
        ledBuffer[number].set(red, green, blue, brightness);
    }

    /**
     * Update the strip in order to show its new settings.
     */
    public void update() {
        if (suspendUpdates) {
            return;
        }

        final byte packet[] = new byte[numberOfLeds * 3];

        for (int i = 0; i < numberOfLeds; i++) {
            packet[i * 3] = ledBuffer[i].getBlue();
            packet[(i * 3) + 1] = ledBuffer[i].getRed();
            packet[(i * 3) + 2] = ledBuffer[i].getGreen();
        }

        // Update the strand
        Spi.wiringPiSPIDataRW(0, packet, this.numberOfLeds * 3);

        byte endPacket[] = {(byte) 0x00};

        // Flush the update
        Spi.wiringPiSPIDataRW(0, endPacket, 1);
    }

    @Override
    public int getRed(int ledPosition) {
        return ledBuffer[ledPosition].getRed();
    }

    @Override
    public int getGreen(int ledPosition) {
        return ledBuffer[ledPosition].getGreen();
    }

    @Override
    public int getBlue(int ledPosition) {
        return ledBuffer[ledPosition].getBlue();
    }

    @Override
    public int getBrightness(int ledPosition) {
        return brightness;
    }

    @Override
    public void setGlobalBrightness(int brightness) {
        this.brightness = brightness;
    }

    @Override
    public int getGlobalBrightness() {
        return brightness;
    }

    /**
     * RGBLed represents a 'single' led on a led strip. In reality these
     * 'single' leds consist out of 3 leds, a red, a green and a blue one.
     *
     * @author Gert Leenders
     */
    private class RGBLed {

        private byte red;
        private byte green;
        private byte blue;

        /**
         * Initiate a single led in a led strip.
         *
         * @param red        value between 0 and 255 for the red led
         * @param green      value between 0 and 255 for the green led
         * @param blue       value between 0 and 255 for the blue led
         * @param brightness overall brightness for the led combination
         */
        public void set(final int red, final int green, final int blue, final int brightness) {
            if (brightness < 0 || brightness > 100) {
                throw new IllegalArgumentException("Brightness must be between 0 and 100");
            }
            double brightnessDouble = Double.valueOf(brightness) / 100;
            this.red = GAMMA[(int) (red * brightnessDouble)];
            this.green = GAMMA[(int) (green * brightnessDouble)];
            this.blue = GAMMA[(int) (blue * brightnessDouble)];
        }

        /**
         * @return the value for the green led (between 0 and 255)
         */
        public byte getGreen() {
            return green;
        }

        /**
         * @return the value for the blue led (between 0 and 255)
         */
        public byte getBlue() {
            return blue;
        }

        /**
         * @return the value for the red led (between 0 and 255)
         */
        public byte getRed() {
            return red;
        }
    }
}
