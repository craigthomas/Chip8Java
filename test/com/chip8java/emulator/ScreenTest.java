/*
 * Copyright (C) 2013-2015 Craig Thomas
 * This project uses an MIT style license - see LICENSE for details.
 */
package com.chip8java.emulator;

import static org.junit.Assert.*;

import java.awt.FontFormatException;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the Chip8 Screen.
 */
public class ScreenTest {

    private Screen mScreen;

    @Before
    public void setUp() throws FileNotFoundException, FontFormatException,
            IOException {
        mScreen = new Screen();
    }

    @Test
    public void testDefaultConstructorSetsCorrectWidth() {
        assertEquals(Screen.SCREEN_WIDTH, mScreen.getWidth());
    }

    @Test
    public void testDefaultConstructorSetsCorrectHeight() {
        assertEquals(Screen.SCREEN_HEIGHT, mScreen.getHeight());
    }

    @Test
    public void testScaleFactorSetCorrectlyOnDefault() {
        assertEquals(Screen.SCALE_FACTOR, mScreen.getScale());
    }

    @Test
    public void testScreenNoPixelsOnAtInit() {
        for (int xCoord = 0; xCoord < mScreen.getWidth(); xCoord++) {
            for (int yCoord = 0; yCoord < mScreen.getHeight(); yCoord++) {
                assertFalse(mScreen.pixelOn(xCoord, yCoord));
            }
        }
    }

    @Test
    public void testScreenTurningPixelsOnSetsPixelOn() {
        for (int xCoord = 0; xCoord < mScreen.getWidth(); xCoord++) {
            for (int yCoord = 0; yCoord < mScreen.getHeight(); yCoord++) {
                mScreen.drawPixel(xCoord, yCoord, true);
                assertTrue(mScreen.pixelOn(xCoord, yCoord));
            }
        }
    }

    @Test
    public void testScreenTurningPixelsOffSetsPixelOff() {
        for (int xCoord = 0; xCoord < mScreen.getWidth(); xCoord++) {
            for (int yCoord = 0; yCoord < mScreen.getHeight(); yCoord++) {
                mScreen.drawPixel(xCoord, yCoord, true);
                mScreen.drawPixel(xCoord, yCoord, false);
                assertFalse(mScreen.pixelOn(xCoord, yCoord));
            }
        }
    }

    @Test
    public void testClearScreenSetsAllPixelsOff() {
        for (int xCoord = 0; xCoord < mScreen.getWidth(); xCoord++) {
            for (int yCoord = 0; yCoord < mScreen.getHeight(); yCoord++) {
                mScreen.drawPixel(xCoord, yCoord, true);
            }
        }
        mScreen.clearScreen();
        for (int xCoord = 0; xCoord < mScreen.getWidth(); xCoord++) {
            for (int yCoord = 0; yCoord < mScreen.getHeight(); yCoord++) {
                assertFalse(mScreen.pixelOn(xCoord, yCoord));
            }
        }
    }

    @Test
    public void testScaleFactorSetCorrectlyWithScaleConstructor()
            throws FileNotFoundException, FontFormatException, IOException {
        mScreen = new Screen(2);
        assertEquals(2, mScreen.getScale());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeScaleFactorThrowsIllegalArgument()
            throws FileNotFoundException, FontFormatException, IOException {
        mScreen = new Screen(-1);
    }

    @Test
    public void testWidthHeightScaleTitleSetCorrectlyConstructor()
            throws FileNotFoundException, FontFormatException, IOException {
        mScreen = new Screen(4, 5, 6);
        assertEquals(4, mScreen.getWidth());
        assertEquals(5, mScreen.getHeight());
        assertEquals(6, mScreen.getScale());
    }
}
