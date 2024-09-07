/*
 * Copyright (C) 2013-2018 Craig Thomas
 * This project uses an MIT style license - see LICENSE for details.
 */
package ca.craigthomas.chip8java.emulator.components;

import static org.junit.Assert.*;

import java.awt.FontFormatException;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the Chip8 Screen.
 */
public class ScreenTest
{
    private Screen screen;

    @Before
    public void setUp() throws IOException {
        screen = new Screen();
    }

    @Test
    public void testDefaultConstructorSetsCorrectWidth() {
        assertEquals(64, screen.getWidth());
    }

    @Test
    public void testDefaultConstructorSetsCorrectHeight() {
        assertEquals(32, screen.getHeight());
    }

    @Test
    public void testExtendedModeSetsCorrectWidth() {
        screen.setExtendedScreenMode();
        assertEquals(128, screen.getWidth());
    }

    @Test
    public void testExtendedModeSetsCorrectHeight() {
        screen.setExtendedScreenMode();
        assertEquals(64, screen.getHeight());
    }

    @Test
    public void testScaleFactorSetCorrectlyOnDefault() {
        assertEquals(1, screen.getScale());
    }

    @Test
    public void testScreenNoPixelsOnAtInit() {
        for (int xCoord = 0; xCoord < screen.getWidth(); xCoord++) {
            for (int yCoord = 0; yCoord < screen.getHeight(); yCoord++) {
                assertFalse(screen.getPixel(xCoord, yCoord));
            }
        }
    }

    @Test
    public void testScreenTurningPixelsOnSetsGetPixel() {
        for (int xCoord = 0; xCoord < screen.getWidth(); xCoord++) {
            for (int yCoord = 0; yCoord < screen.getHeight(); yCoord++) {
                screen.drawPixel(xCoord, yCoord, true);
                assertTrue(screen.getPixel(xCoord, yCoord));
            }
        }
    }

    @Test
    public void testScreenTurningPixelsOffSetsPixelOff() {
        for (int xCoord = 0; xCoord < screen.getWidth(); xCoord++) {
            for (int yCoord = 0; yCoord < screen.getHeight(); yCoord++) {
                screen.drawPixel(xCoord, yCoord, true);
                screen.drawPixel(xCoord, yCoord, false);
                assertFalse(screen.getPixel(xCoord, yCoord));
            }
        }
    }

    @Test
    public void testClearScreenSetsAllPixelsOff() {
        for (int xCoord = 0; xCoord < 64; xCoord++) {
            for (int yCoord = 0; yCoord < 32; yCoord++) {
                screen.drawPixel(xCoord, yCoord, true);
            }
        }
        screen.clearScreen();
        for (int xCoord = 0; xCoord < 64; xCoord++) {
            for (int yCoord = 0; yCoord < screen.getHeight(); yCoord++) {
                assertFalse(screen.getPixel(xCoord, yCoord));
            }
        }
    }

    @Test
    public void testScaleFactorSetCorrectlyWithScaleConstructor() throws IOException {
        screen = new Screen(2);
        assertEquals(2, screen.getScale());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeScaleFactorThrowsIllegalArgument()
            throws FontFormatException, IOException {
        screen = new Screen(-1);
    }

    @Test
    public void testScrollRight() throws IOException {
        screen = new Screen(2);
        screen.drawPixel(0, 0, true);
        screen.scrollRight();
        assertTrue(screen.getPixel(4,0));
        assertFalse(screen.getPixel(0,0));
    }

    @Test
    public void testScrollLeft() throws IOException {
        screen = new Screen(2);
        screen.drawPixel(4, 0, true);
        screen.scrollLeft();
        assertTrue(screen.getPixel(0, 0));
        assertFalse(screen.getPixel(4, 0));
    }

    @Test
    public void testScrollDown() throws IOException {
        screen = new Screen(2);
        screen.drawPixel(0, 0, true);
        screen.scrollDown(4);
        assertTrue(screen.getPixel(0, 4));
        assertFalse(screen.getPixel(0, 0));
    }

    @Test
    public void testGetBackBufferWorksCorrectly() {
        screen = new Screen(2);
        assertEquals(screen.backBuffer, screen.getBuffer());
    }
}
