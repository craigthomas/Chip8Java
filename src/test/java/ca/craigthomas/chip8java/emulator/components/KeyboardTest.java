/*
 * Copyright (C) 2013-2024 Craig Thomas
 * This project uses an MIT style license - see LICENSE for details.
 */
package ca.craigthomas.chip8java.emulator.components;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.awt.event.KeyEvent;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Tests for the Chip8 Keyboard.
 */
public class KeyboardTest
{
    private Keyboard keyboard;
    private Emulator emulator;
    private KeyEvent event;
    private static final int KEY_NOT_IN_MAPPING = KeyEvent.VK_H;
    
    @Before
    public void setUp() {
        keyboard = new Keyboard();
        event = mock(KeyEvent.class);
    }
    
    @Test
    public void testMapKeycodeToChip8Key() {
        for (int index = 0; index < Keyboard.keycodeMap.length; index++) {
            assertEquals(index, keyboard.mapKeycodeToChip8Key(Keyboard.keycodeMap[index]));
        }
    }
    
    @Test
    public void testMapKeycodeToChip8KeyReturnsZeroOnInvalidKey() {
        assertEquals(-1, keyboard.mapKeycodeToChip8Key(KEY_NOT_IN_MAPPING));
    }
    
    @Test
    public void testCurrentKeyIsZeroWhenNoKeyPressed() {
        assertEquals(-1, keyboard.getCurrentKey());
    }

    @Test
    public void testGetDebugKey() {
        keyboard.rawKeyPressed = 1;
        assertEquals(1, keyboard.getRawKeyPressed());
    }

    @Test
    public void testKeyPressedWorksCorrectly() {
        when(event.getKeyCode()).thenReturn(KeyEvent.VK_2);
        keyboard.keyPressed(event);
        assertEquals(2, keyboard.currentKeyPressed);
    }

    @Test
    public void testKeyReleased() {
        when(event.getKeyCode()).thenReturn(KeyEvent.VK_2);
        keyboard.keyPressed(event);
        assertTrue(keyboard.isKeyPressed(2));

        keyboard.keyReleased(event);
        assertFalse(keyboard.isKeyPressed(2));
    }
}
