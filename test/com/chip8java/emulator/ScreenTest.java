/*
 * Copyright (C) 2013-2017 Craig Thomas
 * This project uses an MIT style license - see LICENSE for details.
 */
package com.chip8java.emulator;

import static org.junit.Assert.*;

import java.awt.FontFormatException;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the Chip8 Screen.
 */
public class ScreenTest {

    private Screen mScreen;

    @Before
    public void setUp() throws IOException {
        mScreen = new Screen();
    }

    @Test
    public void testDefaultConstructorSetsCorrectWidth() {
        assertEquals(Screen.normalScreenMode.getWidth(), mScreen.getWidth());
    }

    @Test
    public void testDefaultConstructorSetsCorrectHeight() {
        assertEquals(Screen.normalScreenMode.getHeight(), mScreen.getHeight());
    }

    @Test
    public void testScaleFactorSetCorrectlyOnDefault() {
        assertEquals(Screen.normalScreenMode.getScale(), mScreen.getScale());
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
    public void testScaleFactorSetCorrectlyWithScaleConstructor() throws IOException {
        mScreen = new Screen(2);
        assertEquals(2, mScreen.getScale());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeScaleFactorThrowsIllegalArgument()
            throws FontFormatException, IOException {
        mScreen = new Screen(-1);
    }

    @Test
    public void testScrollRight() throws IOException {
        mScreen = new Screen(2);
        mScreen.drawPixel(0, 0, true);
        mScreen.scrollRight();
        assertTrue(mScreen.pixelOn(4,0));
        assertFalse(mScreen.pixelOn(0,0));
    }

    @Test
    public void testScrollLeft() throws IOException {
        mScreen = new Screen(2);
        mScreen.drawPixel(4, 0, true);
        mScreen.scrollLeft();
        assertTrue(mScreen.pixelOn(0, 0));
        assertFalse(mScreen.pixelOn(4, 0));
    }

    @Test
    public void testScrollDown() throws IOException {
        mScreen = new Screen(2);
        mScreen.drawPixel(0, 0, true);
        mScreen.scrollDown(4);
        assertTrue(mScreen.pixelOn(0, 4));
        assertFalse(mScreen.pixelOn(0, 0));
    }
}
