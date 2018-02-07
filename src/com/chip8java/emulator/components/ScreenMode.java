/*
 * Copyright (C) 2013-2018 Craig Thomas
 * This project uses an MIT style license - see LICENSE for details.
 */
package com.chip8java.emulator.components;

/**
 * This class represents the series of options needed to set the
 * resolution of a Chip 8 screen.
 *
 * @author Craig Thomas
 */
public class ScreenMode
{
    // The width in pixels of the screen
    private int width;

    // The height in pixels of the screen
    private int height;

    // The scaling factor for the screen
    private int scale;

    public ScreenMode(int width, int height, int scale) {
        if (scale < 1) {
            throw new IllegalArgumentException("scale must be >= 1");
        }

        this.width = width;
        this.height = height;
        this.scale = scale;
    }

    /**
     * Returns the current width of the screen.
     *
     * @return the current width of the screen
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the current height of the screen.
     *
     * @return the current height of the screen
     */
    public int getHeight() {
        return height;
    }

    /**
     * Returns the scaling factor for the screen.
     *
     * @return the current screen scaling factor
     */
    public int getScale() {
        return scale;
    }
}
