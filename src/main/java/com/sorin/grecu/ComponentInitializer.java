package com.sorin.grecu;

import com.pi4j.wiringpi.Spi;
import com.sorin.grecu.ledstrip.ledStrip.Effects.MovingSegmentsEffect;
import com.sorin.grecu.ledstrip.ledStrip.LedStrip;
import com.sorin.grecu.ledstrip.ledStrip.NetworkUtils;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by Sorin on 11/10/2015.
 */
@Slf4j
@Component
@NoArgsConstructor
public class ComponentInitializer {

    @Autowired
    @Qualifier("${ledStrip.name}")
    LedStrip ledStrip;

    @Autowired
    MovingSegmentsEffect movingSegmentsEffect;

    @PostConstruct
    public void initComponents() {
        // gpio works only on RPI, avoiding this on local Windows machine
        if (NetworkUtils.isMachineRpi()) {// TODO: this gets logged twice. investigate!
            initSpi();
            testLedStrip(ledStrip);
        }
        ledStrip.setLedEffect(movingSegmentsEffect);
    }

    private void initSpi() {
        int fd = Spi.wiringPiSPISetup(0, 6_000_000);
        if (fd <= -1) {
            log.error("SPI initialization FAILED");
            return;
        }
        log.info("SPI has been initialized");
    }

    private void testLedStrip(LedStrip ledStrip) {
        try {
            log.info("Running led strip test");
            ledStrip.fill(255, 0, 0, 100);
            ledStrip.update();
            Thread.sleep(100);
            ledStrip.fill(0, 255, 0);
            ledStrip.update();
            Thread.sleep(100);
            ledStrip.fill(0, 0, 255);
            ledStrip.update();
            Thread.sleep(100);
            ledStrip.setOff();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
