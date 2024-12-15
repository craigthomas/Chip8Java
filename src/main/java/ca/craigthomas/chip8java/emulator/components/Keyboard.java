/*
 * Copyright (C) 2013-2018 Craig Thomas
 * This project uses an MIT style license - see LICENSE for details.
 */
package ca.craigthomas.chip8java.emulator.components;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * The Keyboard class listens for keypress events and translates them into their
 * equivalent Chip8 key values, or will flag that a debug key was pressed.
 */
public class Keyboard extends KeyAdapter
{
    // Map from a keypress event to key values
    public static final int[] sKeycodeMap = {
            KeyEvent.VK_X, // 0x0
            KeyEvent.VK_1, // 0x1
            KeyEvent.VK_2, // 0x2
            KeyEvent.VK_3, // 0x3
            KeyEvent.VK_Q, // 0x4
            KeyEvent.VK_W, // 0x5
            KeyEvent.VK_E, // 0x6
            KeyEvent.VK_A, // 0x7
            KeyEvent.VK_S, // 0x8
            KeyEvent.VK_D, // 0x9
            KeyEvent.VK_Z, // 0xA
            KeyEvent.VK_C, // 0xB
            KeyEvent.VK_4, // 0xC
            KeyEvent.VK_R, // 0xD
            KeyEvent.VK_F, // 0xE
            KeyEvent.VK_V, // 0xF
    };

    // The current key being pressed, -1 if no key
    protected int currentKeyPressed = -1;

    // Stores the last raw key keypress
    protected int rawKeyPressed;

    // The key to quit the emulator
    protected static final int CHIP8_QUIT = KeyEvent.VK_ESCAPE;

    public Keyboard(Emulator emulator) {}

    @Override
    public void keyPressed(KeyEvent e) {
        rawKeyPressed = e.getKeyCode();
        currentKeyPressed = mapKeycodeToChip8Key(rawKeyPressed);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        currentKeyPressed = -1;
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
        return -1;
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
     * Returns the currently pressed debug key. Will return 0 if no debug key was
     * pressed.
     *
     * @return the value of the currently pressed debug key.
     */
    public int getRawKeyPressed() {
        return rawKeyPressed;
    }
}
