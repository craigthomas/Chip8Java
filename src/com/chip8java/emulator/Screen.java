/*
 * Copyright (C) 2013 Craig Thomas
 * This project uses an MIT style license - see LICENSE for details.
 */
package com.chip8java.emulator;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * A class to emulate a Chip 8 Screen. The original Chip 8 screen was 64 x 32.
 * This class creates a simple AWT canvas with a single back buffer to store
 * the current state of the Chip 8 screen. Two colors are used on the Chip 8 -
 * the <code>foreColor</code> and the <code>backColor</code>. The former is
 * used when turning pixels on, while the latter is used to turn pixels off.
 * 
 * @author Craig Thomas
 */
public class Screen {

	// The default screen width in pixels
	public static final int SCREEN_WIDTH = 64;
	// The default screen height in pixels
	public static final int SCREEN_HEIGHT = 32;
	// The default scaling factor to apply to the screen width and height
	public static final int SCALE_FACTOR = 14;
	// The default title to apply to the emulator window
	public static final String DEFAULT_TITLE = "Yet Another Chip 8 Emulator";
	// The number of buffers to use for bit blitting
	private static final int DEFAULT_NUMBER_OF_BUFFERS = 1;
	// The scaling factor applied to this screen
	private int scaleFactor;
	// The width of the current screen
	private int width;
	// The height of the current screen
	private int height;
	// The color marked for use as the foreground color
	private Color foreColor;
	// The color marked for use as the background color
	private Color backColor;
	// The main canvas to draw on
	private Canvas canvas;
	// Create a back buffer to store image information
	BufferedImage backbuffer;

	/**
	 * The main constructor for a Chip8Screen. This is a convenience 
	 * constructor that will fill in default values for the width, height,
	 * scale, title and keyboard controls.
	 */
	public Screen() {
		this(SCREEN_WIDTH, SCREEN_HEIGHT, SCALE_FACTOR, DEFAULT_TITLE,
				new Keyboard());
	}

	/**
	 * The main constructor for a Chip8Screen. This constructor allows for 
	 * full customization of the Chip8Screen object.
	 * 
	 * @param width
	 * @param height
	 * @param scale
	 * @param title
	 * @param keyboard
	 */
	public Screen(int width, int height, int scale, String title,
			Keyboard keyboard) {
		
		this.scaleFactor = scale;
		this.width = width;
		this.height = height;
		
		foreColor = Color.white;
		backColor = Color.black;
		
		JFrame container = new JFrame(title);
		JPanel panel = (JPanel) container.getContentPane();
		panel.setPreferredSize(new Dimension(width * scale, height * scale));
		panel.setLayout(null);
		
		canvas = new Canvas();
		
		canvas.setBounds(0, 0, width * scale, height * scale);
		panel.add(canvas);
		
		canvas.setIgnoreRepaint(true);
		
		container.pack();
		container.setResizable(false);
		container.setVisible(true);
		
		canvas.createBufferStrategy(DEFAULT_NUMBER_OF_BUFFERS);
		backbuffer = new BufferedImage(width * scale, height * scale, BufferedImage.TYPE_4BYTE_ABGR);
	}

	/**
	 * Low level routine to draw a pixel to the screen. Takes into account the
	 * scaling factor applied to the screen. The top-left corner of the screen
	 * is at coordinate (0, 0). 
	 * 
	 * @param x The x coordinate to place the pixel
	 * @param y The y coordinate to place the pixel
	 * @param color The Color of the pixel to draw
	 */
	private void drawPixelPrimitive(int x, int y, Color color) {
		Graphics2D graphics = backbuffer.createGraphics();
		graphics.setColor(color);
		graphics.fillRect(x * scaleFactor, y * scaleFactor, scaleFactor, scaleFactor);
		graphics.dispose();
	}
	
	/**
	 * Returns <code>true</code> if the pixel at the location (x, y) is
	 * currently painted with the foreground color.
	 * 
	 * @param x The x coordinate of the pixel to check
	 * @param y The y coordinate of the pixel to check
	 * @return Returns <code>true</code> if the pixel (x, y) is turned on
	 */
	public boolean pixelOn(int x, int y) {
		Color color = new Color(backbuffer.getRGB(x * scaleFactor, y * scaleFactor), true);
		return color.equals(foreColor);
	}
	
	/**
	 * Turn a pixel on or off at the specified location. Drawing is performed
	 * via an XOR operation - if a pixel is already turned on, and the argument
	 * <code>on</code> is true, turn the pixel off and return <code>true</code>, 
	 * otherwise, set the pixel accordingly and return <code>false</code>.
	 * 
	 * @param x The x coordinate to place the pixel
	 * @param y The y coordinate to place the pixel
	 * @param on Set to <code>true</code>
	 * @return Returns <code>true</code> if the pixel was already on and 
	 * <code>on</code> was set to <code>true</code>, <code>false</code>
	 * otherwise
	 */
	public boolean drawPixel(int x, int y, boolean on) {
		if (on && pixelOn(x, y)) {
			drawPixelPrimitive(x, y, backColor);
			return true;
		}
		else if (on) {
			drawPixelPrimitive(x, y, foreColor);
		}
		else {
			drawPixelPrimitive(x, y, backColor);
		}
		return false;
	}
	
	/**
	 * Clears the screen. Note that the caller must call 
	 * <code>updateScreen</code> to flush the back buffer to the screen.
	 */
	public void clearScreen() {
		Graphics2D graphics = backbuffer.createGraphics();
		graphics.setColor(backColor);
		graphics.fillRect(0, 0, width * scaleFactor, height * scaleFactor);
		graphics.dispose();
	}
	
	/**
	 * Flushes the contents of the back buffer to the screen.
	 */
	public void updateScreen() {
		Graphics2D graphics = (Graphics2D) canvas.getBufferStrategy().getDrawGraphics();
		graphics.drawImage(backbuffer, null, 0, 0);
		graphics.dispose();
		canvas.getBufferStrategy().show();
	}
	
	/**
	 * Returns the height of the screen.
	 * 
	 * @return The height of the screen in pixels
	 */
	public int getHeight() {
		return height;
	}
	
	/**
	 * Returns the width of the screen.
	 * 
	 * @return The width of the screen
	 */
	public int getWidth() {
		return width;
	}
}
