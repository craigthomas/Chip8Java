/*
 * Copyright (C) 2013-2018 Craig Thomas
 * This project uses an MIT style license - see LICENSE for details.
 */
package com.chip8java.emulator.components;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * The Keyboard class listens for keypress events and translates them into their
 * equivalent Chip8 key values, or will flag that a debug key was pressed.
 */
public class Keyboard extends KeyAdapter
{
    public static final int NO_KEY_PRESSED = -1;

    // Map from a keypress event to key values
    public static final int[] sKeycodeMap = {
            KeyEvent.VK_X, // Key 0
            KeyEvent.VK_1, // Key 1
            KeyEvent.VK_2, // Key 2
            KeyEvent.VK_3, // Key 3
            KeyEvent.VK_Q, // Key 4
            KeyEvent.VK_W, // Key 5
            KeyEvent.VK_E, // Key 6
            KeyEvent.VK_A, // Key 7
            KeyEvent.VK_S, // Key 8
            KeyEvent.VK_D, // Key 9
            KeyEvent.VK_Z, // Key A
            KeyEvent.VK_C, // Key B
            KeyEvent.VK_4, // Key C
            KeyEvent.VK_R, // Key D
            KeyEvent.VK_F, // Key E
            KeyEvent.VK_V, // Key F
    };

    // The current key being pressed, 0 if no key
    protected int currentKeyPressed = NO_KEY_PRESSED;

    // Stores the last raw keypress
    protected int rawKeypress;

    // The key to quit the emulator
    protected static final int CHIP8_QUIT = KeyEvent.VK_ESCAPE;

    // The key to enter debug mode
    public static final int CHIP8_STEP = KeyEvent.VK_Y;

    // The key to enter trace mode
    public static final int CHIP8_TRACE = KeyEvent.VK_T;

    // The key to stop trace or debug
    public static final int CHIP8_NORMAL = KeyEvent.VK_M;

    // The key to advance to the next instruction
    public static final int CHIP8_NEXT = KeyEvent.VK_N;

    private Emulator emulator;

    public Keyboard(Emulator emulator) {
        this.emulator = emulator;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        rawKeypress = e.getKeyCode();
        currentKeyPressed = mapKeycodeToChip8Key(e.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        currentKeyPressed = NO_KEY_PRESSED;
    }

    /**
     * Map a keycode value to a Chip 8 key value. See sKeycodeMap definition. Will
     * return -1 if no Chip8 key was pressed. In the case of multiple keys being
     * pressed simultaneously, will return the first one that it finds in the
     * keycode mapping object.
     *
     * @param keycode The code representing the key that was just pressed
     * @return The Chip 8 key value for the specified keycode
     */
    public int mapKeycodeToChip8Key(int keycode) {
        for (int i = 0; i < sKeycodeMap.length; i++) {
            if (sKeycodeMap[i] == keycode) {
                return i;
            }
        }
        return NO_KEY_PRESSED;
    }

    /**
     * Return the current key being pressed.
     *
     * @return The Chip8 key value being pressed
     */
    public int getCurrentKey() {
        return currentKeyPressed;
    }

    /**
     * Returns the currently pressed debug key.
     *
     * @return the value of the currently pressed debug key.
     */
    public int getDebugKey() {
        int debugKey = rawKeypress;
        rawKeypress = 0;
        return debugKey;
    }
}
