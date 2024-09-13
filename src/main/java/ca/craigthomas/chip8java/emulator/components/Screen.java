/*
 * Copyright (C) 2013-2024 Craig Thomas
 * This project uses an MIT style license - see LICENSE for details.
 */
package ca.craigthomas.chip8java.emulator.components;

import java.awt.*;
import java.awt.image.BufferedImage;

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
    public static final int WIDTH = 128;
    public static final int HEIGHT = 64;

    public static final int SCREEN_MODE_NORMAL = 0;
    public static final int SCREEN_MODE_EXTENDED = 1;

    private int scale = 1;

    // The current screen mode
    private int screenMode;

    // The colors used for drawing on bitplanes
    private Color color0;
    private Color color1;
    private Color color2;
    private Color color3;

    // Create a back buffer to store image information
    protected BufferedImage backBuffer;

    /**
     * A constructor for a Chip8Screen. This is a convenience constructor that
     * will fill in default values for the scale and bitplane colors.
     */
    public Screen() {
        this(1);
    }

    /**
     * A constructor for a Chip8Screen. This is a convenience constructor that
     * will allow the caller to set the scale factor alone.
     *
     * @param scale The scale factor for the screen
     */
    public Screen(int scale) {
        this(scale, Color.black, Color.decode("#FF33CC"), Color.decode("#33CCFF"), Color.white);
    }

    /**
     * The main constructor for a Chip8Screen. This constructor allows for full
     * customization of the Chip8Screen object.
     *
     * @param scale the scale factor for the new screen
     * @param color0 the color for bitplane 0
     * @param color1 the color for bitplane 1
     * @param color2 the color for bitplane 2
     * @param color3 the color for bitplane 3
     */
    public Screen(int scale, Color color0, Color color1, Color color2, Color color3) {
        this.scale = scale;
        this.color0 = color0;
        this.color1 = color1;
        this.color2 = color2;
        this.color3 = color3;
        this.screenMode = SCREEN_MODE_NORMAL;
        this.createBackBuffer();
    }

    /**
     * Generates the BufferedImage that will act as the back buffer for the
     * screen. Flags the Screen state as having changed.
     */
    private void createBackBuffer() {
        backBuffer = new BufferedImage(WIDTH * scale, HEIGHT * scale, BufferedImage.TYPE_4BYTE_ABGR);
    }

    /**
     * Returns the color for the specified bitplane.
     *
     * @param bitplane The bitplane color to return
     */
    Color getBitplaneColor(int bitplane) {
        if (bitplane == 0) {
            return color0;
        }

        if (bitplane == 1) {
            return color1;
        }

        if (bitplane == 2) {
            return color2;
        }

        return color3;
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
        int mode_scale = (screenMode == SCREEN_MODE_EXTENDED) ? 1 : 2;
        Graphics2D graphics = backBuffer.createGraphics();
        graphics.setColor(color);
        graphics.fillRect(
                x * scale * mode_scale,
                y * scale * mode_scale,
                scale * mode_scale,
                scale * mode_scale);
        graphics.dispose();
    }

    /**
     * Returns <code>true</code> if the pixel at the location (x, y) is
     * currently on.
     *
     * @param x The x coordinate of the pixel to check
     * @param y The y coordinate of the pixel to check
     * @param bitplane the bitplane to check
     * @return Returns <code>true</code> if the pixel (x, y) is turned on
     */
    public boolean getPixel(int x, int y, int bitplane) {
        if (bitplane == 0) {
            return false;
        }

        Color bitplaneColor = getBitplaneColor(bitplane);
        int modeScale = (screenMode == SCREEN_MODE_EXTENDED) ? 1 : 2;
        int xScale = x * modeScale * scale;
        int yScale = y * modeScale * scale;
        Color color = new Color(backBuffer.getRGB(xScale, yScale), true);
        return color.equals(bitplaneColor) || color.equals(color3);
    }

    /**
     * Turn a pixel on or off at the specified location.
     *
     * @param x  The x coordinate to place the pixel
     * @param y  The y coordinate to place the pixel
     * @param turnOn Turns the pixel on at location x, y if <code>true</code>
     * @param bitplane the bitplane to draw to
     */
    public void drawPixel(int x, int y, boolean turnOn, int bitplane) {
        if (bitplane == 0) {
            return;
        }

        int otherBitplane = (bitplane == 1) ? 2 : 1;
        boolean otherPixelOn = getPixel(x, y, otherBitplane);

        Color drawColor = getBitplaneColor(0);

        if (turnOn && otherPixelOn) {
            drawColor = getBitplaneColor(3);
        }
        if (turnOn && !otherPixelOn) {
            drawColor = getBitplaneColor(bitplane);
        }
        if (!turnOn && otherPixelOn) {
            drawColor = getBitplaneColor(otherBitplane);
        }
        drawPixelPrimitive(x, y, drawColor);
    }

    /**
     * Clears the screen. Note that the caller must call
     * <code>updateScreen</code> to flush the back buffer to the screen.
     *
     * @param bitplane The bitplane to clear
     */
    public void clearScreen(int bitplane) {
        if (bitplane == 0) {
            return;
        }

        if (bitplane == 3) {
            Graphics2D graphics = backBuffer.createGraphics();
            graphics.setColor(getBitplaneColor(0));
            graphics.fillRect(0, 0, WIDTH * scale, HEIGHT * scale);
            graphics.dispose();
            return;
        }

        int maxX = getWidth();
        int maxY = getHeight();
        for (int x = 0; x < maxX; x++) {
            for (int y = 0; y < maxY; y++) {
                drawPixel(x, y, false, bitplane);
            }
        }
    }

    /**
     * Scrolls the screen 4 pixels to the right.
     *
     * @param bitplane the bitplane to scroll
     */
    public void scrollRight(int bitplane) {
        if (bitplane == 0) {
            return;
        }

        int modeScale = (screenMode == SCREEN_MODE_EXTENDED) ? 1 : 2;

        int width = WIDTH * scale;
        int height = HEIGHT * scale;
        int right = scale * 4 * modeScale;

        if (bitplane == 3) {
            BufferedImage bufferedImage = copyImage(backBuffer.getSubimage(0, 0, width, height));
            Graphics2D graphics = backBuffer.createGraphics();
            graphics.setColor(color0);
            graphics.fillRect(0, 0, width, height);
            graphics.drawImage(bufferedImage, right, 0, null);
            graphics.dispose();
            return;
        }

        int maxX = getWidth();
        int maxY = getHeight();

        // Blank out any pixels in the right vertical lines that we will copy to
        for (int x = maxX - 4; x < maxX; x++) {
            for (int y = 0; y < maxY; y++) {
                drawPixel(x, y, false, bitplane);
            }
        }

        // Start copying pixels from the left to the right and shift by 4 pixels
        for (int x = maxX - 4 - 1; x > -1; x--) {
            for (int y = 0; y < maxY; y++) {
                boolean currentPixel = getPixel(x, y, bitplane);
                drawPixel(x, y, false, bitplane);
                drawPixel(x + 4, y, currentPixel, bitplane);
            }
        }

        // Blank out any pixels in the left 4 vertical lines
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < maxY; y++) {
                drawPixel(x, y, false, bitplane);
            }
        }
    }

    /**
     * Scrolls the screen 4 pixels to the left.
     *
     * @param bitplane the bitplane to scroll
     */
    public void scrollLeft(int bitplane) {
        if (bitplane == 0) {
            return;
        }

        int maxX = getWidth();
        int maxY = getHeight();
        int modeScale = (screenMode == SCREEN_MODE_EXTENDED) ? 1 : 2;
        int screenWidth = maxX * modeScale * scale;
        int screenHeight = maxY * modeScale * scale;

        // If bitplane 3 is selected, we can just do a fast copy instead of pixel by pixel
        if (bitplane == 3) {
            int left = -(scale * 4 * modeScale);
            BufferedImage bufferedImage = copyImage(backBuffer.getSubimage(0, 0, screenWidth, screenHeight));
            Graphics2D graphics = backBuffer.createGraphics();
            graphics.setColor(color0);
            graphics.fillRect(0, 0, screenWidth, screenHeight);
            graphics.drawImage(bufferedImage, left, 0, null);
            graphics.dispose();
            return;
        }

        // Blank out any pixels in the left 4 vertical lines we will copy to
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < maxY; y++) {
                drawPixel(x, y, false, bitplane);
            }
        }

        // Start copying pixels from the right to the left and shift by 4 pixels
        for (int x = 4; x < maxX; x++) {
            for (int y = 0; y < maxY; y++) {
                boolean currentPixel = getPixel(x, y, bitplane);
                drawPixel(x, y, false, bitplane);
                drawPixel(x - 4, y, currentPixel, bitplane);
            }
        }

        // Blank out any pixels in the right 4 vertical columns
        for (int x = maxX - 4; x < maxX; x++) {
            for (int y = 0; y < maxY; y++) {
                drawPixel(x, y, false, bitplane);
            }
        }
    }

    /**
     * Scrolls the screen down by the specified number of pixels.
     *
     * @param numPixels the number of pixels to scroll down
     */
    public void scrollDown(int numPixels) {
        int width = this.getWidth() * scale;
        int height = this.getHeight() * scale;
        int modeScale = (screenMode == SCREEN_MODE_EXTENDED) ? 1 : 2;
        int down = numPixels * scale * modeScale;

        BufferedImage bufferedImage = copyImage(backBuffer.getSubimage(0, 0, width, height));
        Graphics2D graphics = backBuffer.createGraphics();
        graphics.setColor(color0);
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
        return (screenMode == SCREEN_MODE_EXTENDED) ? 64 : 32;
    }

    /**
     * Returns the width of the screen.
     *
     * @return The width of the screen
     */
    public int getWidth() {
        return (screenMode == SCREEN_MODE_EXTENDED) ? 128 : 64;
    }

    /**
     * Returns the scale of the screen.
     *
     * @return The scale factor of the screen
     */
    public int getScale() {
        return scale;
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
        screenMode = SCREEN_MODE_EXTENDED;
    }

    /**
     * Turns on the normal screen mode for the emulator (when operating
     * in Super Chip 8 mode).
     */
    public void setNormalScreenMode() {
        screenMode = SCREEN_MODE_NORMAL;
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
