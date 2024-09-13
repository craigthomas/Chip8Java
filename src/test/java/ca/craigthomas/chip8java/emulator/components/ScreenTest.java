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
                assertFalse(screen.getPixel(xCoord, yCoord, 1));
            }
        }
    }

    @Test
    public void testScreenTurningPixelsOnSetsGetPixel() {
        for (int x = 0; x < screen.getWidth(); x++) {
            for (int y = 0; y < screen.getHeight(); y++) {
                screen.drawPixel(x, y, true, 1);
                assertTrue(screen.getPixel(x, y, 1));
            }
        }
    }

    @Test
    public void testGetPixelOnBitplane0ReturnsFalse() {
        for (int x = 0; x < 64; x++) {
            for (int y = 0; y < 32; y++) {
                screen.drawPixel(x, y, true, 1);
                screen.drawPixel(x, y, true, 2);
                assertFalse(screen.getPixel(x, y, 0));
            }
        }
    }

    @Test
    public void testDrawPixelOnBitplane0DoesNothing() {
        for (int x = 0; x < 64; x++) {
            for (int y = 0; y < 32; y++) {
                screen.drawPixel(x, y, true, 0);
                assertFalse(screen.getPixel(x, y, 1));
                assertFalse(screen.getPixel(x, y, 2));
            }
        }
    }

    @Test
    public void testScreenTurningPixelsOffSetsPixelOff() {
        for (int x = 0; x < screen.getWidth(); x++) {
            for (int y = 0; y < screen.getHeight(); y++) {
                screen.drawPixel(x, y, true, 1);
                assertTrue(screen.getPixel(x, y, 1));
                screen.drawPixel(x, y, false, 1);
                assertFalse(screen.getPixel(x, y, 1));
            }
        }
    }

    @Test
    public void testWritePixelTurnsOnPixelOnBitplane1Only() {
        for (int x = 0; x < 64; x++) {
            for (int y = 0; y < 32; y++) {
                screen.drawPixel(x, y, true, 1);
                assertTrue(screen.getPixel(x, y, 1));
                assertFalse(screen.getPixel(x, y, 2));
            }
        }
    }

    @Test
    public void testWritePixelTurnsOnPixelOnBitplane2Only() {
        for (int x = 0; x < 64; x++) {
            for (int y = 0; y < 32; y++) {
                screen.drawPixel(x, y, true, 2);
                assertTrue(screen.getPixel(x, y, 2));
                assertFalse(screen.getPixel(x, y, 1));
            }
        }
    }

    @Test
    public void testWritePixelOnBitplane3TurnsOnPixelOnBitplane1And2() {
        for (int x = 0; x < 64; x++) {
            for (int y = 0; y < 32; y++) {
                screen.drawPixel(x, y, true, 3);
                assertTrue(screen.getPixel(x, y, 2));
                assertTrue(screen.getPixel(x, y, 1));
            }
        }
    }

    @Test
    public void testClearScreenOnBitplane0DoesNothingToBitplane1And2() {
        for (int x = 0; x < 64; x++) {
            for (int y = 0; y < 32; y++) {
                screen.drawPixel(x, y, true, 1);
                screen.drawPixel(x, y, true, 2);
                assertTrue(screen.getPixel(x, y, 1));
                assertTrue(screen.getPixel(x, y, 2));
            }
        }
        screen.clearScreen(0);
        for (int x = 0; x < 64; x++) {
            for (int y = 0; y < screen.getHeight(); y++) {
                assertTrue(screen.getPixel(x, y, 1));
                assertTrue(screen.getPixel(x, y, 2));
            }
        }
    }

    @Test
    public void testClearScreenSetsAllPixelsOffOnBitplane1() {
        for (int x = 0; x < 64; x++) {
            for (int y = 0; y < 32; y++) {
                screen.drawPixel(x, y, true, 1);
                assertTrue(screen.getPixel(x, y, 1));
                assertFalse(screen.getPixel(x, y, 2));
            }
        }
        screen.clearScreen(1);
        for (int x = 0; x < 64; x++) {
            for (int y = 0; y < screen.getHeight(); y++) {
                assertFalse(screen.getPixel(x, y, 1));
            }
        }
    }

    @Test
    public void testClearScreenSetsAllPixelsOffOnBitplane1OnlyWhenBothSet() {
        for (int x = 0; x < 64; x++) {
            for (int y = 0; y < 32; y++) {
                screen.drawPixel(x, y, true, 1);
                screen.drawPixel(x, y, true, 2);
                assertTrue(screen.getPixel(x, y, 1));
                assertTrue(screen.getPixel(x, y, 2));
            }
        }
        screen.clearScreen(1);
        for (int x = 0; x < 64; x++) {
            for (int y = 0; y < screen.getHeight(); y++) {
                assertFalse(screen.getPixel(x, y, 1));
                assertTrue(screen.getPixel(x, y, 2));
            }
        }
    }

    @Test
    public void testClearScreenSetsAllPixelsOffOnBitplane3WhenBothSet() {
        for (int x = 0; x < 64; x++) {
            for (int y = 0; y < 32; y++) {
                screen.drawPixel(x, y, true, 1);
                screen.drawPixel(x, y, true, 2);
                assertTrue(screen.getPixel(x, y, 1));
                assertTrue(screen.getPixel(x, y, 2));
            }
        }
        screen.clearScreen(3);
        for (int x = 0; x < 64; x++) {
            for (int y = 0; y < screen.getHeight(); y++) {
                assertFalse(screen.getPixel(x, y, 1));
                assertFalse(screen.getPixel(x, y, 2));
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
    public void testScrollRightOnBitplane0DoesNothing() throws IOException {
        screen = new Screen(2);
        screen.drawPixel(0, 0, true, 1);
        screen.drawPixel(0, 0, true, 2);
        assertTrue(screen.getPixel(0, 0, 1));
        assertTrue(screen.getPixel(0, 0, 2));
        screen.scrollRight(0);
        assertTrue(screen.getPixel(0, 0, 1));
        assertFalse(screen.getPixel(1, 0, 1));
        assertFalse(screen.getPixel(2, 0, 1));
        assertFalse(screen.getPixel(3, 0, 1));
        assertFalse(screen.getPixel(4, 0, 1));
        assertTrue(screen.getPixel(0, 0, 2));
        assertFalse(screen.getPixel(1, 0, 2));
        assertFalse(screen.getPixel(2, 0, 2));
        assertFalse(screen.getPixel(3, 0, 2));
        assertFalse(screen.getPixel(4, 0, 2));
    }

    @Test
    public void testScrollRightBitplane1Only() throws IOException {
        screen = new Screen(2);
        screen.drawPixel(0, 0, true, 1);
        screen.drawPixel(0, 0, true, 2);
        screen.scrollRight(1);
        assertFalse(screen.getPixel(0, 0, 1));
        assertFalse(screen.getPixel(1, 0, 1));
        assertFalse(screen.getPixel(2, 0, 1));
        assertFalse(screen.getPixel(3, 0, 1));
        assertTrue(screen.getPixel(4, 0, 1));
        assertTrue(screen.getPixel(0, 0, 2));
        assertFalse(screen.getPixel(1, 0, 2));
        assertFalse(screen.getPixel(2, 0, 2));
        assertFalse(screen.getPixel(3, 0, 2));
        assertFalse(screen.getPixel(4, 0, 2));
    }

    @Test
    public void testScrollRightBitplane3() throws IOException {
        screen = new Screen(2);
        screen.drawPixel(0, 0, true, 1);
        screen.drawPixel(0, 0, true, 2);
        screen.scrollRight(3);
        assertFalse(screen.getPixel(0, 0, 1));
        assertFalse(screen.getPixel(1, 0, 1));
        assertFalse(screen.getPixel(2, 0, 1));
        assertFalse(screen.getPixel(3, 0, 1));
        assertTrue(screen.getPixel(4, 0, 1));
        assertFalse(screen.getPixel(0, 0, 2));
        assertFalse(screen.getPixel(1, 0, 2));
        assertFalse(screen.getPixel(2, 0, 2));
        assertFalse(screen.getPixel(3, 0, 2));
        assertTrue(screen.getPixel(4, 0, 2));
    }

    @Test
    public void testScrollLeft() throws IOException {
        screen = new Screen(2);
        screen.drawPixel(4, 0, true, 1);
        screen.scrollLeft(1);
        assertTrue(screen.getPixel(0, 0, 1));
        assertFalse(screen.getPixel(4, 0, 1));
    }

    @Test
    public void testScrollLeftOnBitplane0DoesNothing() throws IOException {
        screen = new Screen(2);
        screen.drawPixel(63, 0, true, 1);
        screen.drawPixel(63, 0, true, 2);
        assertTrue(screen.getPixel(63, 0, 1));
        assertTrue(screen.getPixel(63, 0, 2));
        screen.scrollLeft(0);
        assertTrue(screen.getPixel(63, 0, 1));
        assertFalse(screen.getPixel(62, 0, 1));
        assertFalse(screen.getPixel(61, 0, 1));
        assertFalse(screen.getPixel(60, 0, 1));
        assertFalse(screen.getPixel(59, 0, 1));
        assertTrue(screen.getPixel(63, 0, 2));
        assertFalse(screen.getPixel(62, 0, 2));
        assertFalse(screen.getPixel(61, 0, 2));
        assertFalse(screen.getPixel(60, 0, 2));
        assertFalse(screen.getPixel(59, 0, 2));
    }

    @Test
    public void testScrollLeftBitplane1Only() throws IOException {
        screen = new Screen(2);
        screen.drawPixel(63, 0, true, 1);
        screen.drawPixel(63, 0, true, 2);
        assertTrue(screen.getPixel(63, 0, 1));
        assertTrue(screen.getPixel(63, 0, 2));
        screen.scrollLeft(1);
        assertFalse(screen.getPixel(63, 0, 1));
        assertFalse(screen.getPixel(62, 0, 1));
        assertFalse(screen.getPixel(61, 0, 1));
        assertFalse(screen.getPixel(60, 0, 1));
        assertTrue(screen.getPixel(59, 0, 1));
        assertTrue(screen.getPixel(63, 0, 2));
        assertFalse(screen.getPixel(62, 0, 2));
        assertFalse(screen.getPixel(61, 0, 2));
        assertFalse(screen.getPixel(60, 0, 2));
        assertFalse(screen.getPixel(59, 0, 2));
    }

    @Test
    public void testScrollLeftBitplane3() throws IOException {
        screen = new Screen(2);
        screen.drawPixel(63, 0, true, 1);
        screen.drawPixel(63, 0, true, 2);
        assertTrue(screen.getPixel(63, 0, 1));
        assertTrue(screen.getPixel(63, 0, 2));
        screen.scrollLeft(3);
        assertFalse(screen.getPixel(63, 0, 1));
        assertFalse(screen.getPixel(62, 0, 1));
        assertFalse(screen.getPixel(61, 0, 1));
        assertFalse(screen.getPixel(60, 0, 1));
        assertTrue(screen.getPixel(59, 0, 1));
        assertFalse(screen.getPixel(63, 0, 2));
        assertFalse(screen.getPixel(62, 0, 2));
        assertFalse(screen.getPixel(61, 0, 2));
        assertFalse(screen.getPixel(60, 0, 2));
        assertTrue(screen.getPixel(59, 0, 2));
    }

    @Test
    public void testScrollDown() throws IOException {
        screen = new Screen(2);
        screen.drawPixel(0, 0, true, 1);
        screen.scrollDown(4, 1);
        assertTrue(screen.getPixel(0, 4, 1));
        assertFalse(screen.getPixel(0, 0, 1));
    }

    @Test
    public void testScrollDownBitplane0DoesNothing() throws IOException {
        screen = new Screen(2);
        screen.drawPixel(0, 0, true, 1);
        screen.drawPixel(0, 0, true, 2);
        screen.scrollDown(4, 0);
        assertTrue(screen.getPixel(0, 0, 1));
        assertTrue(screen.getPixel(0, 0, 2));
    }

    @Test
    public void testScrollDownBitplane1() throws IOException {
        screen = new Screen(2);
        screen.drawPixel(0, 0, true, 1);
        assertTrue(screen.getPixel(0, 0, 1));
        assertFalse(screen.getPixel(0, 0, 2));
        screen.scrollDown(1, 1);
        assertFalse(screen.getPixel(0, 0, 1));
        assertFalse(screen.getPixel(0, 0, 2));
        assertTrue(screen.getPixel(0, 1, 1));
        assertFalse(screen.getPixel(0, 1, 2));
    }

    @Test
    public void testScrollDownBitplane1BothPixelsActive() throws IOException {
        screen = new Screen(2);
        screen.drawPixel(0, 0, true, 1);
        screen.drawPixel(0, 0, true, 2);
        assertTrue(screen.getPixel(0, 0, 1));
        assertTrue(screen.getPixel(0, 0, 2));
        screen.scrollDown(1, 1);
        assertFalse(screen.getPixel(0, 0, 1));
        assertTrue(screen.getPixel(0, 0, 2));
        assertTrue(screen.getPixel(0, 1, 1));
        assertFalse(screen.getPixel(0, 1, 2));
    }

    @Test
    public void testScrollDownBitplane3BothPixelsActive() throws IOException {
        screen = new Screen(2);
        screen.drawPixel(0, 0, true, 1);
        screen.drawPixel(0, 0, true, 2);
        assertTrue(screen.getPixel(0, 0, 1));
        assertTrue(screen.getPixel(0, 0, 2));
        screen.scrollDown(1, 3);
        assertFalse(screen.getPixel(0, 0, 1));
        assertFalse(screen.getPixel(0, 0, 2));
        assertTrue(screen.getPixel(0, 1, 1));
        assertTrue(screen.getPixel(0, 1, 2));
    }

    @Test
    public void testGetBackBufferWorksCorrectly() {
        screen = new Screen(2);
        assertEquals(screen.backBuffer, screen.getBuffer());
    }
}
