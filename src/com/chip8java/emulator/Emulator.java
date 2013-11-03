/*
 * Copyright (C) 2013 Craig Thomas
 * This project uses an MIT style license - see LICENSE for details.
 */
package com.chip8java.emulator;


public class Emulator {

	public static void main(String [] argv) {
		Screen screen = new Screen();
		screen.clearScreen();
		try {
			while (true) {
				boolean result = screen.drawPixel(1, 1, true);
				if (result) {
					screen.drawPixel(2, 2, true);
				}
				else {
					screen.drawPixel(2, 2, false);
				}
				screen.updateScreen();
				Thread.sleep(1000);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
