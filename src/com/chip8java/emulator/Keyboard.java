/*
 * Copyright (C) 2013 Craig Thomas
 * This project uses an MIT style license - see LICENSE for details.
 */
package com.chip8java.emulator;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class Keyboard extends KeyAdapter {

	// Map from a keypress event to key values
	protected static final int [] keycodeMapping = {
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
	private int currentKey = 0;
	// The key to quit the emulator
	public static final int CHIP8_QUIT = KeyEvent.VK_ESCAPE;
	// The key to enter debug mode
	public static final int CHIP8_STEP = KeyEvent.VK_Z;
	// The key to enter trace mode
	public static final int CHIP8_TRACE = KeyEvent.VK_X;
	// The key to stop trace or debug
	public static final int CHIP8_NORMAL = KeyEvent.VK_C;
	// The key to advance to the next instruction
	public static final int CHIP8_NEXT = KeyEvent.VK_N;
	// Stores the last debug action
	private int debugKey;
	
	public void keyPressed(KeyEvent e) {
	    debugKey = e.getKeyCode();
	    
		switch (debugKey) {
		case CHIP8_QUIT:
			System.exit(0);
			break;
		}
		
		currentKey = mapKeycodeToChip8Key(e.getKeyCode());
	}
	
	public void keyReleased(KeyEvent e) {
		currentKey = 0;
	}
	
	/**
	 * Map a keycode value to a Chip 8 key value. See keycodeMapping definition.
	 * 
	 * @param keycode The code representing the key that was just pressed
	 * @return The Chip 8 key value for the specified keycode
	 */
	public int mapKeycodeToChip8Key(int keycode) {
		for (int i = 0; i < keycodeMapping.length; i++) {
			if (keycodeMapping[i] == keycode) {
				return i + 1;
			}
		}
		return 0;
	}
	
	/**
	 * Return the current key being pressed.
	 * 
	 * @return The Chip 8 key value being pressed
	 */
	public int getCurrentKey() {
		return currentKey;
	}
	
	public int getDebugKey() {
	    int theKey = debugKey;
	    debugKey = 0;
	    return theKey;
	}
}
