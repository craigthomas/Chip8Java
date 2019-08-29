/*
 * Copyright (C) 2013-2018 Craig Thomas
 * This project uses an MIT style license - see LICENSE for details.
 */
package com.chip8java.emulator.common;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.logging.Logger;

public class IO
{
    // The logger for the class
    private final static Logger LOGGER = Logger.getLogger(IO.class.getName());

    /**
     * Attempts to open the specified filename as an InputStream. Will return null if there is
     * an error.
     *
     * @param filename The String containing the full path to the filename to open
     * @return An opened InputStream, or null if there is an error
     */
    public static InputStream openInputStream(String filename) {
        try {
            return new FileInputStream(new File(filename));
        } catch (Exception e) {
            LOGGER.severe("Error opening file: " + e.getMessage());
            return null;
        }
    }

    /**
     * Attempts to open the specified resource as an InputStream. Will return null if there is
     * an error.
     *
     * @param filename The String containing the full path to the filename to open
     * @return An opened InputStream, or null if there is an error
     */
    public static InputStream openInputStreamFromResource(String filename) {
        try {
            return IO.class.getClassLoader().getResourceAsStream(filename);
        } catch (Exception e) {
            LOGGER.severe("Error opening resource file: " + e.getMessage());
            return null;
        }
    }

    /**
     * Closes an open input or output stream.
     *
     * @param stream the stream to close
     */
    public static boolean closeStream(Closeable stream) {
        try {
            stream.close();
            return true;
        } catch (Exception e) {
            LOGGER.severe("Error closing stream: " + e.getMessage());
            return false;
        }
    }

    /**
     * Copies an array of bytes to an array of shorts, starting at the offset
     * in the target memory array.
     *
     * @param stream the stream with the bytes to copy
     * @param target the target short array
     * @param offset where in the target array to copy bytes to
     * @return true if the source was not null, false otherwise
     */
    public static boolean copyStreamToShortArray(InputStream stream, short[] target, int offset) {
        int byteCounter = offset;
        byte[] source;

        if (target == null) {
            return false;
        }

        try {
            source = IOUtils.toByteArray(stream);
        } catch (Exception e) {
            LOGGER.severe("Error copying stream: " + e.getMessage());
            return false;
        }

        if (source.length > (target.length - offset)) {
            return false;
        }

        for (byte data : source) {
            target[byteCounter] = data;
            byteCounter++;
        }

        return true;
    }
}
