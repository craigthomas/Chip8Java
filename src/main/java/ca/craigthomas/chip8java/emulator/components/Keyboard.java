/*
 * Copyright (C) 2013-2025 Craig Thomas
 * This project uses an MIT style license - see LICENSE for details.
 */
package ca.craigthomas.chip8java.emulator.components;

import org.lwjgl.glfw.GLFW;

/**
 * The Keyboard class listens for keypress events and translates them into their
 * equivalent Chip8 key values, or will flag that a debug key was pressed.
 */
public class Keyboard
{
    // Map from a keypress event to key values
    public static final int[] keycodeMap = {
            GLFW.GLFW_KEY_X, // 0x0
            GLFW.GLFW_KEY_1, // 0x1
            GLFW.GLFW_KEY_2, // 0x2
            GLFW.GLFW_KEY_3, // 0x3
            GLFW.GLFW_KEY_Q, // 0x4
            GLFW.GLFW_KEY_W, // 0x5
            GLFW.GLFW_KEY_E, // 0x6
            GLFW.GLFW_KEY_A, // 0x7
            GLFW.GLFW_KEY_S, // 0x8
            GLFW.GLFW_KEY_D, // 0x9
            GLFW.GLFW_KEY_Z, // 0xA
            GLFW.GLFW_KEY_C, // 0xB
            GLFW.GLFW_KEY_4, // 0xC
            GLFW.GLFW_KEY_R, // 0xD
            GLFW.GLFW_KEY_F, // 0xE
            GLFW.GLFW_KEY_V, // 0xF
    };

    public boolean [] keypressMap = {
        false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false
    };

    // The current key being pressed, -1 if no key
    protected int currentKeyPressed = -1;

    // Stores the last raw key keypress
    protected int rawKeyPressed;

    // The key to quit the emulator
    protected static final int CHIP8_QUIT = GLFW.GLFW_KEY_ESCAPE;

    public Keyboard() {}

    /**
     * Flag that a current key is pressed based on the GFLW key code
     * passed by the event callback.
     *
     * @param glfwKeyCode the GLFW key that was pressed
     */
    public void keyPressed(int glfwKeyCode) {
        for (int x = 0; x < 16; x++) {
            if (glfwKeyCode == keycodeMap[x]) {
                keypressMap[x] = true;
            }
        }
        currentKeyPressed = mapKeycodeToChip8Key(glfwKeyCode);
    }

    /**
     * Flag that a current key is released based on the GLFW key code
     * passed by the event calback.
     *
     * @param glfwKeyCode the GLFW key that was released
     */
    public void keyReleased(int glfwKeyCode) {
        for (int x = 0; x < 16; x++) {
            if (glfwKeyCode == keycodeMap[x]) {
                keypressMap[x] = false;
            }
        }
    }

    /**
     * Map a keycode value to a Chip 8 key value. See keycodeMap definition. Will
     * return -1 if no Chip8 key was pressed. In the case of multiple keys being
     * pressed simultaneously, will return the first one that it finds in the
     * keycode mapping object.
     *
     * @param keycode The code representing the key that was just pressed
     * @return The Chip 8 key value for the specified keycode
     */
    public int mapKeycodeToChip8Key(int keycode) {
        for (int i = 0; i < keycodeMap.length; i++) {
            if (keycodeMap[i] == keycode) {
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
     * Returns true if the specified key in the keymap is currently reported
     * as being pressed.
     *
     * @param key the key number in the keymap to check for
     * @return true if the key is pressed
     */
    public boolean isKeyPressed(int key) {
        if (key >= 0 && key < 16) {
            return keypressMap[key];
        }
        return false;
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
