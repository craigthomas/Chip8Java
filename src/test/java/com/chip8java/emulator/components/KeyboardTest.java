/*
 * Copyright (C) 2024 Craig Thomas
 * This project uses an MIT style license - see LICENSE for details.
 */
package com.chip8java.emulator.components;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.awt.event.KeyEvent;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the Chip8 Keyboard.
 */
public class KeyboardTest
{
    private Keyboard keyboard;
    private Emulator emulator;
    private KeyEvent event;
    private static final int KEY_NOT_IN_MAPPING = KeyEvent.VK_G;
    
    @Before
    public void setUp() {
        emulator = mock(Emulator.class);
        keyboard = new Keyboard(emulator);
        event = mock(KeyEvent.class);
    }
    
    @Test
    public void testMapKeycodeToChip8Key() {
        for (int index = 0; index < Keyboard.sKeycodeMap.length; index++) {
            assertEquals(index, keyboard.mapKeycodeToChip8Key(Keyboard.sKeycodeMap[index]));
        }
    }
    
    @Test
    public void testMapKeycodeToChip8KeyReturnsNegativeOneOnInvalidKey() {
        assertEquals(-1, keyboard.mapKeycodeToChip8Key(KEY_NOT_IN_MAPPING));
    }
    
    @Test
    public void testCurrentKeyIsNegativeOneWhenNoKeyPressed() {
        assertEquals(Keyboard.NO_KEY_PRESSED, keyboard.getCurrentKey());
    }

    @Test
    public void testGetDebugKey() {
        keyboard.rawKeypress = 1;
        assertEquals(1, keyboard.getDebugKey());
    }

    @Test
    public void testKeyReleased() {
        keyboard.currentKeyPressed = 1;
        keyboard.keyReleased(null);
        assertEquals(Keyboard.NO_KEY_PRESSED, keyboard.currentKeyPressed);
    }

    @Test
    public void testKeyPressedWorksCorrectly() {
        when(event.getKeyCode()).thenReturn(KeyEvent.VK_2);
        keyboard.keyPressed(event);
        assertEquals(2, keyboard.currentKeyPressed);
    }
}
