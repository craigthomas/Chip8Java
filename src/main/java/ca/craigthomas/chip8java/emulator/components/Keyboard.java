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
            KeyEvent.VK_4, // Key 1
            KeyEvent.VK_5, // Key 2
            KeyEvent.VK_6, // Key 3
            KeyEvent.VK_7, // Key 4
            KeyEvent.VK_R, // Key 5
            KeyEvent.VK_Y, // Key 6
            KeyEvent.VK_U, // Key 7
            KeyEvent.VK_F, // Key 8
            KeyEvent.VK_G, // Key 9
            KeyEvent.VK_H, // Key A
            KeyEvent.VK_J, // Key B
            KeyEvent.VK_V, // Key C
            KeyEvent.VK_B, // Key D
            KeyEvent.VK_N, // Key E
            KeyEvent.VK_M, // Key F
    };

    // The current key being pressed, 0 if no key
    protected int currentKeyPressed = 0;

    // Stores the last debug key keypress
    protected int debugKeyPressed;

    // The key to quit the emulator
    protected static final int CHIP8_QUIT = KeyEvent.VK_ESCAPE;

    // The key to enter debug mode
    public static final int CHIP8_STEP = KeyEvent.VK_Z;

    // The key to enter trace mode
    public static final int CHIP8_TRACE = KeyEvent.VK_X;

    // The key to stop trace or debug
    public static final int CHIP8_NORMAL = KeyEvent.VK_C;

    // The key to advance to the next instruction
    public static final int CHIP8_NEXT = KeyEvent.VK_N;

    private Emulator emulator;

    public Keyboard(Emulator emulator) {
        this.emulator = emulator;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        debugKeyPressed = e.getKeyCode();

        switch (debugKeyPressed) {
            case CHIP8_QUIT:
                emulator.kill();
                break;

            default:
                currentKeyPressed = mapKeycodeToChip8Key(e.getKeyCode());
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        currentKeyPressed = 0;
    }

    /**
     * Map a keycode value to a Chip 8 key value. See sKeycodeMap definition. Will
     * return 0 if no Chip8 key was pressed. In the case of multiple keys being
     * pressed simultaneously, will return the first one that it finds in the
     * keycode mapping object.
     *
     * @param keycode The code representing the key that was just pressed
     * @return The Chip 8 key value for the specified keycode
     */
    public int mapKeycodeToChip8Key(int keycode) {
        for (int i = 0; i < sKeycodeMap.length; i++) {
            if (sKeycodeMap[i] == keycode) {
                return i + 1;
            }
        }
        return 0;
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
    public int getDebugKey() {
        int debugKey = debugKeyPressed;
        debugKeyPressed = 0;
        return debugKey;
    }
}
