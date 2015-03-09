/*
 * Copyright (C) 2013 Craig Thomas
 * This project uses an MIT style license - see LICENSE for details.
 */
package com.chip8java.emulator;

import java.awt.AlphaComposite;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import javax.swing.JCheckBoxMenuItem;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.*;

/**
 * A class to emulate a Chip 8 Screen. The original Chip 8 screen was 64 x 32.
 * This class creates a simple AWT canvas with a single back buffer to store the
 * current state of the Chip 8 screen. Two colors are used on the Chip 8 - the
 * <code>foreColor</code> and the <code>backColor</code>. The former is used
 * when turning pixels on, while the latter is used to turn pixels off.
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
    // Create a back buffer to store image information
    BufferedImage backbuffer;

    /**
     * A constructor for a Chip8Screen. This is a convenience constructor that
     * will fill in default values for the width, height, scale, and title.
     * 
     * @throws IOException
     * @throws FontFormatException
     * @throws FileNotFoundException
     */
    public Screen() throws FileNotFoundException, FontFormatException,
            IOException {
        this(SCREEN_WIDTH, SCREEN_HEIGHT, SCALE_FACTOR);
    }

    /**
     * A constructor for a Chip8Screen. This is a convenience constructor that
     * will allow the caller to set the scale factor alone.
     * 
     * @param scale
     *            The scale factor for the screen
     * @throws FileNotFoundException
     * @throws FontFormatException
     * @throws IOException
     */
    public Screen(int scale) throws FileNotFoundException, FontFormatException,
            IOException {
        this(SCREEN_WIDTH, SCREEN_HEIGHT, scale);
    }

    /**
     * The main constructor for a Chip8Screen. This constructor allows for full
     * customization of the Chip8Screen object.
     * 
     * @param width
     * @param height
     * @param scale
     * @throws IOException
     * @throws FontFormatException
     * @throws FileNotFoundException
     */
    public Screen(int width, int height, int scale)
            throws FileNotFoundException, FontFormatException, IOException {

        if (scale < 1) {
            throw new IllegalArgumentException("scale must be > 1");
        }

        this.scaleFactor = scale;
        this.width = width;
        this.height = height;

        foreColor = Color.white;
        backColor = Color.black;

        backbuffer = new BufferedImage(width * scale, height * scale,
                BufferedImage.TYPE_4BYTE_ABGR);
    }

    /**
     * Low level routine to draw a pixel to the screen. Takes into account the
     * scaling factor applied to the screen. The top-left corner of the screen
     * is at coordinate (0, 0).
     * 
     * @param x
     *            The x coordinate to place the pixel
     * @param y
     *            The y coordinate to place the pixel
     * @param color
     *            The Color of the pixel to draw
     */
    private void drawPixelPrimitive(int x, int y, Color color) {
        Graphics2D graphics = backbuffer.createGraphics();
        graphics.setColor(color);
        graphics.fillRect(x * scaleFactor, y * scaleFactor, scaleFactor,
                scaleFactor);
        graphics.dispose();
    }

    /**
     * Returns <code>true</code> if the pixel at the location (x, y) is
     * currently painted with the foreground color.
     * 
     * @param x
     *            The x coordinate of the pixel to check
     * @param y
     *            The y coordinate of the pixel to check
     * @return Returns <code>true</code> if the pixel (x, y) is turned on
     */
    public boolean pixelOn(int x, int y) {
        Color color = new Color(backbuffer.getRGB(x * scaleFactor, y
                * scaleFactor), true);
        return color.equals(foreColor);
    }

    /**
     * Turn a pixel on or off at the specified location.
     * 
     * @param x
     *            The x coordinate to place the pixel
     * @param y
     *            The y coordinate to place the pixel
     * @param on
     *            Turns the pixel on at location x, y if <code>true</code>
     */
    public void drawPixel(int x, int y, boolean on) {
        if (on) {
            drawPixelPrimitive(x, y, foreColor);
            return;
        }
        drawPixelPrimitive(x, y, backColor);
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

    /**
     * Returns the scale of the screen.
     * 
     * @return The scale factor of the screen
     */
    public int getScale() {
        return scaleFactor;
    }

    /**
     * Returns the BufferedImage that has the contents of the screen.
     *
     * @return the
     */
    public BufferedImage getBuffer() {
        return backbuffer;
    }
}
