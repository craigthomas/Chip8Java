/*
 * Copyright (C) 2013-2017 Craig Thomas
 * This project uses an MIT style license - see LICENSE for details.
 */
package com.chip8java.emulator;

import static org.junit.Assert.*;

import java.awt.event.KeyEvent;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the Chip8 Keyboard.
 */
public class KeyboardTest {

    private Keyboard mKeyboard;
    private static final int KEY_NOT_IN_MAPPING = KeyEvent.VK_A;
    
    @Before
    public void setUp() {
        mKeyboard = new Keyboard();
    }
    
    @Test
    public void testMapKeycodeToChip8Key() {
        for (int index = 0; index < Keyboard.sKeycodeMap.length; index++) {
            assertEquals(index + 1, mKeyboard.mapKeycodeToChip8Key(Keyboard.sKeycodeMap[index]));
        }
    }
    
    @Test
    public void testMapKeycodeToChip8KeyReturnsZeroOnInvalidKey() {
        assertEquals(0, mKeyboard.mapKeycodeToChip8Key(KEY_NOT_IN_MAPPING));
    }
    
    @Test
    public void testCurrentKeyIsZeroWhenNoKeyPressed() {
        assertEquals(0, mKeyboard.getCurrentKey());
    }
}
