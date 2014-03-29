/*
 * Copyright (C) 2013 Craig Thomas
 * This project uses an MIT style license - see LICENSE for details.
 */
package com.chip8java.emulator;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class Keyboard extends KeyAdapter {

	// Map from a keypress event to key values
	private static final int [][] keycodeMapping = {
		{0x1, KeyEvent.VK_4},
		{0x2, KeyEvent.VK_5},
		{0x3, KeyEvent.VK_6},
		{0x4, KeyEvent.VK_R},
		{0x5, KeyEvent.VK_T},
		{0x6, KeyEvent.VK_Y},
		{0x7, KeyEvent.VK_F},
		{0x8, KeyEvent.VK_G},
		{0x9, KeyEvent.VK_H},
		{0xA, KeyEvent.VK_V},
		{0xB, KeyEvent.VK_B},
		{0xC, KeyEvent.VK_7},
		{0xD, KeyEvent.VK_U},
		{0xE, KeyEvent.VK_J},
		{0xF, KeyEvent.VK_N},
	};
	// The current key being pressed, 0 if no key
	private int currentKey = 0;
	// Whether the trace key is turned on
	private boolean trace = false;
	// Whether the step key is turned on
	private boolean step = false;
	// The key to quit the emulator
	private static final int CHIP8_QUIT = KeyEvent.VK_ESCAPE;
	// The key to enter debug mode
	private static final int CHIP8_STEP = KeyEvent.VK_Z;
	// The key to enter trace mode
	private static final int CHIP8_TRACE = KeyEvent.VK_X;
	// The key to stop trace or debug
	private static final int CHIP8_NORMAL = KeyEvent.VK_C;
	
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
		case CHIP8_QUIT:
			System.exit(0);
			break;
			
		case CHIP8_TRACE:
			trace = true;
			break;
			
		case CHIP8_STEP:
			step = true;
			break;
			
		case CHIP8_NORMAL:
			trace = false;
			step = false;
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
			if (keycodeMapping[i][1] == keycode) {
				return i;
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
	
	/**
	 * Returns whether trace is turned on or not.
	 * 
	 * @return
	 */
	public boolean getTrace() {
		return trace;
	}
	
	public boolean getStep() {
		return step;
	}
}
