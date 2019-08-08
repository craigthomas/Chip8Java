/*
 * Copyright (C) 2013-2018 Craig Thomas
 * This project uses an MIT style license - see LICENSE for details.
 */
package com.chip8java.emulator.components;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * A class to emulate a Chip 8 Screen. The original Chip 8 screen was 64 x 32.
 * This class creates a simple AWT canvas with a single back buffer to store the
 * current state of the Chip 8 screen. Two colors are used on the Chip 8 - the
 * <code>foreColor</code> and the <code>backColor</code>. The former is used
 * when turning pixels on, while the latter is used to turn pixels off.
 *
 * @author Craig Thomas
 */
public class Screen
{
    // Screen dimensions for when the emulator is in normal mode
    public static ScreenMode normalScreenMode = new ScreenMode(64, 32, 14);

    // Screen dimensions for when the emulator is in extended mode
    private static ScreenMode extendedScreenMode = new ScreenMode(128, 64, 7);

    // The current screen mode
    private ScreenMode screenMode;

    // The color marked for use as the foreground color
    private Color foreColor;

    // The color marked for use as the background color
    private Color backColor;

    // Create a back buffer to store image information
    protected BufferedImage backBuffer;

    // Whether the state of the screen has changed between Normal and Extended
    protected boolean stateChanged;

    /**
     * A constructor for a Chip8Screen. This is a convenience constructor that
     * will fill in default values for the width, height, and scale.
     *
     * @throws IOException         on failure to set the new ScreenMode
     */
    public Screen() throws IOException {
        this(new ScreenMode(0, 0, normalScreenMode.getScale()));
    }

    /**
     * A constructor for a Chip8Screen. This is a convenience constructor that
     * will allow the caller to set the scale factor alone.
     *
     * @param scale The scale factor for the screen
     */
    public Screen(int scale) {
        this(new ScreenMode(0, 0, scale));
    }

    /**
     * The main constructor for a Chip8Screen. This constructor allows for full
     * customization of the Chip8Screen object.
     *
     * @param screenMode the new ScreenMode for the display
     */
    public Screen(ScreenMode screenMode) {
        foreColor = Color.white;
        backColor = Color.black;
        this.screenMode = screenMode;
        setNormalScreenMode();
        stateChanged = false;
    }

    /**
     * Generates the BufferedImage that will act as the back buffer for the
     * screen. Flags the Screen state as having changed.
     */
    private void createBackBuffer() {
        backBuffer = new BufferedImage(
                screenMode.getWidth() * screenMode.getScale(),
                screenMode.getHeight() * screenMode.getScale(),
                BufferedImage.TYPE_4BYTE_ABGR);

        stateChanged = true;
    }

    /**
     * Low level routine to draw a pixel to the screen. Takes into account the
     * scaling factor applied to the screen. The top-left corner of the screen
     * is at coordinate (0, 0).
     *
     * @param x     The x coordinate to place the pixel
     * @param y     The y coordinate to place the pixel
     * @param color The Color of the pixel to draw
     */
    private void drawPixelPrimitive(int x, int y, Color color) {
        int scaleFactor = screenMode.getScale();
        Graphics2D graphics = backBuffer.createGraphics();
        graphics.setColor(color);
        graphics.fillRect(
                x * scaleFactor,
                y * scaleFactor, scaleFactor,
                scaleFactor);
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
        int scaleFactor = screenMode.getScale();
        Color color = new Color(
                backBuffer.getRGB(x * scaleFactor, y * scaleFactor),
                true);
        return color.equals(foreColor);
    }

    /**
     * Turn a pixel on or off at the specified location.
     *
     * @param x  The x coordinate to place the pixel
     * @param y  The y coordinate to place the pixel
     * @param on Turns the pixel on at location x, y if <code>true</code>
     */
    public void drawPixel(int x, int y, boolean on) {
        drawPixelPrimitive(x, y, (on) ? foreColor : backColor);
    }

    /**
     * Clears the screen. Note that the caller must call
     * <code>updateScreen</code> to flush the back buffer to the screen.
     */
    public void clearScreen() {
        int scaleFactor = screenMode.getScale();
        Graphics2D graphics = backBuffer.createGraphics();
        graphics.setColor(backColor);
        graphics.fillRect(
                0,
                0,
                screenMode.getWidth() * scaleFactor,
                screenMode.getHeight() * scaleFactor);
        graphics.dispose();
    }

    /**
     * Scrolls the screen 4 pixels to the right.
     */
    public void scrollRight() {
        int scale = screenMode.getScale();
        int width = screenMode.getWidth() * scale;
        int height = screenMode.getHeight() * scale;
        int right = scale * 4;

        BufferedImage bufferedImage = copyImage(backBuffer.getSubimage(0, 0, width, height));
        Graphics2D graphics = backBuffer.createGraphics();
        graphics.setColor(backColor);
        graphics.fillRect(0,0, width, height);
        graphics.drawImage(bufferedImage, right, 0, null);
        graphics.dispose();
    }

    /**
     * Scrolls the screen 4 pixels to the left.
     */
    public void scrollLeft() {
        int scale = screenMode.getScale();
        int width = screenMode.getWidth() * scale;
        int height = screenMode.getHeight() * scale;
        int left = -(scale * 4);

        BufferedImage bufferedImage = copyImage(backBuffer.getSubimage(0, 0, width, height));
        Graphics2D graphics = backBuffer.createGraphics();
        graphics.setColor(backColor);
        graphics.fillRect(0,0, width, height);
        graphics.drawImage(bufferedImage, left, 0, null);
        graphics.dispose();
    }

    /**
     * Scrolls the screen down by the specified number of pixels.
     *
     * @param numPixels the number of pixels to scroll down
     */
    public void scrollDown(int numPixels) {
        int scale = screenMode.getScale();
        int width = screenMode.getWidth() * scale;
        int height = screenMode.getHeight() * scale;
        int down = numPixels * scale;

        BufferedImage bufferedImage = copyImage(backBuffer.getSubimage(0, 0, width, height));
        Graphics2D graphics = backBuffer.createGraphics();
        graphics.setColor(backColor);
        graphics.fillRect(0,0, width, height);
        graphics.drawImage(bufferedImage, 0, down, null);
        graphics.dispose();
    }

    /**
     * Returns the height of the screen.
     *
     * @return The height of the screen in pixels
     */
    public int getHeight() {
        return screenMode.getHeight();
    }

    /**
     * Returns the width of the screen.
     *
     * @return The width of the screen
     */
    public int getWidth() {
        return screenMode.getWidth();
    }

    /**
     * Returns the scale of the screen.
     *
     * @return The scale factor of the screen
     */
    public int getScale() {
        return screenMode.getScale();
    }

    /**
     * Returns the BufferedImage that has the contents of the screen.
     *
     * @return the backBuffer for the screen
     */
    public BufferedImage getBuffer() {
        return backBuffer;
    }

    /**
     * Turns on the extended screen mode for the emulator (when operating
     * in Super Chip 8 mode). Flags the state of the emulator screen as
     * having been changed.
     */
    public void setExtendedScreenMode() {
        this.screenMode = new ScreenMode(
                Screen.extendedScreenMode.getWidth(),
                Screen.extendedScreenMode.getHeight(),
                this.screenMode.getScale());
        createBackBuffer();
    }

    /**
     * Turns on the normal screen mode for the emulator (when operating
     * in Super Chip 8 mode).
     */
    public void setNormalScreenMode() {
        this.screenMode = new ScreenMode(
                Screen.normalScreenMode.getWidth(),
                Screen.normalScreenMode.getHeight(),
                this.screenMode.getScale());
        createBackBuffer();
    }

    /**
     * Returns true if the state of the screen has changed between Normal
     * and Extended. False otherwise.
     *
     * @return true if the Screen state has changed
     */
    public boolean getStateChanged() {
        return stateChanged;
    }

    /**
     * Clears the state change flag for the Screen.
     */
    public void clearStateChanged() {
        stateChanged = false;
    }

    /**
     * Generates a copy of the original back buffer.
     *
     * @param source the source to copy from
     * @return a BufferedImage that is a copy of the original source
     */
    private BufferedImage copyImage(BufferedImage source) {
        BufferedImage bufferedImage = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
        Graphics graphics = bufferedImage.getGraphics();
        graphics.drawImage(source, 0, 0, null);
        graphics.dispose();
        return bufferedImage;
    }
}
