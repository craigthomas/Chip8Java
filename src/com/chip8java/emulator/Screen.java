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
    // The default title to apply to the emulator window
    public static final String DEFAULT_TITLE = "Yet Another Chip 8 Emulator";
    // The number of buffers to use for bit blitting
    private static final int DEFAULT_NUMBER_OF_BUFFERS = 2;
    // The font to use for the overlay
    private static final String DEFAULT_FONT = "src/resources/VeraMono.ttf";
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
    // The overlay screen to print when trace is turned on
    BufferedImage overlay;
    // The font to use for the overlay
    private Font overlayFont;
    // The overlay background color
    private Color overlayBackColor;
    // The overlay border color
    private Color overlayBorderColor;
    // The container for the window object
    private JFrame container;
    // Whether to write the overlay information to the screen
    private boolean mWriteOverlay;

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
        this(SCREEN_WIDTH, SCREEN_HEIGHT, SCALE_FACTOR, DEFAULT_TITLE);
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
        this(SCREEN_WIDTH, SCREEN_HEIGHT, scale, DEFAULT_TITLE);
    }

    /**
     * The main constructor for a Chip8Screen. This constructor allows for full
     * customization of the Chip8Screen object.
     * 
     * @param width
     * @param height
     * @param scale
     * @param title
     * @throws IOException
     * @throws FontFormatException
     * @throws FileNotFoundException
     */
    public Screen(int width, int height, int scale, String title)
            throws FileNotFoundException, FontFormatException, IOException {

        if (scale < 1) {
            throw new IllegalArgumentException("scale must be > 1");
        }

        this.scaleFactor = scale;
        this.width = width;
        this.height = height;

        foreColor = Color.white;
        backColor = Color.black;
        overlayBackColor = new Color(0.0f, 0.27f, 0.0f, 1.0f);
        overlayBorderColor = new Color(0.0f, 0.78f, 0.0f, 1.0f);

        overlayFont = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(
                DEFAULT_FONT));
        overlayFont = overlayFont.deriveFont(11F);

        container = new JFrame(title);

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        JMenuItem openFile = new JMenuItem("Open", KeyEvent.VK_O);
        fileMenu.add(openFile);

        JMenuItem closeFile = new JMenuItem("Close", KeyEvent.VK_C);
        fileMenu.add(closeFile);

        fileMenu.addSeparator();

        JMenuItem exitFile = new JMenuItem("Exit", KeyEvent.VK_E);
        fileMenu.add(exitFile);

        menuBar.add(fileMenu);
        container.setJMenuBar(menuBar);

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
        backbuffer = new BufferedImage(width * scale, height * scale,
                BufferedImage.TYPE_4BYTE_ABGR);
        overlay = new BufferedImage(342, 53, BufferedImage.TYPE_4BYTE_ABGR);
    }

    public void setKeyListener(Keyboard keyboard) {
        canvas.addKeyListener(keyboard);
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
     * Flushes the contents of the back buffer to the screen.
     */
    public void updateScreen() {
        Graphics2D graphics = (Graphics2D) canvas.getBufferStrategy()
                .getDrawGraphics();
        graphics.drawImage(backbuffer, null, 0, 0);
        if (mWriteOverlay) {
            Composite composite = AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, 0.7f);
            graphics.setComposite(composite);
            graphics.drawImage(overlay, null, 5, (height * scaleFactor) - 57);
        }
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

    /**
     * Returns the scale of the screen.
     * 
     * @return The scale factor of the screen
     */
    public int getScale() {
        return scaleFactor;
    }

    /**
     * Returns the title of the screen.
     * 
     * @return The scale factor of the screen
     */
    public String getTitle() {
        return container.getTitle();
    }

    /**
     * Write the current status of the CPU to the overlay window.
     * 
     * @param cpu
     *            The CentralProcessingUnit object with the current state.
     */
    public void updateOverlayInformation(CentralProcessingUnit cpu) {
        Graphics2D graphics = overlay.createGraphics();

        graphics.setColor(overlayBorderColor);
        graphics.fillRect(0, 0, 342, 53);

        graphics.setColor(overlayBackColor);
        graphics.fillRect(1, 1, 340, 51);

        graphics.setColor(Color.white);
        graphics.setFont(overlayFont);

        String line1 = cpu.cpuStatusLine1();
        String line2 = cpu.cpuStatusLine2();
        String line3 = cpu.cpuStatusLine3();

        graphics.drawString(line1, 5, 16);
        graphics.drawString(line2, 5, 31);
        graphics.drawString(line3, 5, 46);
        graphics.dispose();
    }
    
    /**
     * Sets whether or not the overlay information for the CPU should be turned
     * off or on. If set to true, writes CPU information.
     * 
     * @param writeOverlay 
     *             Whether or not to print CPU information
     */
    public void setWriteOverlay(boolean writeOverlay) {
        mWriteOverlay = writeOverlay;
    }

    /**
     * Get rid of the window.
     */
    public void dispose() {
        container.dispose();
    }
}
