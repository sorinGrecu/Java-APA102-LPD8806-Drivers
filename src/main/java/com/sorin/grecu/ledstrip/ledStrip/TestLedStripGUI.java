package com.sorin.grecu.ledstrip.ledStrip;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@Qualifier("LOCAL")
public class TestLedStripGUI extends GenericLedStrip {

    private final List<JButton> list = new ArrayList<>();

    private JButton getGridButton(int r, int c) {
        int index = r * numberOfLeds + c;
        return list.get(index);
    }

    private JButton createGridButton(final int row, final int col) {
        final JButton b = new JButton();
        return b;
    }

    private JPanel createGridPanel() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (ClassNotFoundException | UnsupportedLookAndFeelException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
        JPanel p = new JPanel(new GridLayout(numberOfLeds, 1));
        for (int i = 0; i < numberOfLeds; i++) {
            int row = i / numberOfLeds;
            int col = 1;
            JButton gb = createGridButton(row, 1);
            list.add(gb);
            p.add(gb);
        }
        return p;
    }

    public void display() {
        JFrame f = new JFrame("Led Strip GUI Test");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.add(createGridPanel());
        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    @Override
    public void displayGraphics() {
        EventQueue.invokeLater(() -> {
            setOn();
            display();
        });
    }

    @Override
    public void setLed(int ledPosition, int red, int green, int blue) {
        setLed(ledPosition, red, green, blue, 0);
    }

    @Override
    public void setLed(int ledPosition, Color color) {
        setLed(ledPosition, color.getRed(), color.getGreen(), color.getBlue());
    }

    @Override
    public void setLed(int ledPosition, int red, int green, int blue, int brightness) {
        SwingUtilities.invokeLater(() -> {
            if (list.size() > 0) {
                list.get(ledPosition).setBackground(new Color(red, green, blue));
                list.get(ledPosition).getParent().repaint();
            }
        });
    }

    @Override
    public void update() {

    }

    @Override
    public int getRed(int ledPosition) {
        return 0;
    }

    @Override
    public int getGreen(int ledPosition) {
        return 0;
    }

    @Override
    public int getBlue(int ledPosition) {
        return 0;
    }

    @Override
    public int getBrightness(int ledPosition) {
        return 0;
    }

    @Override
    public int getGlobalBrightness() {
        return 0;
    }

    @Override
    public void setGlobalBrightness(int brightness) {

    }
}