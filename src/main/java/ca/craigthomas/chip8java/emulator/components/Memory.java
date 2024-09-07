/*
 * Copyright (C) 2013-2018 Craig Thomas
 * This project uses an MIT style license - see LICENSE for details.
 */
package ca.craigthomas.chip8java.emulator.components;

import ca.craigthomas.chip8java.emulator.common.IO;

import java.io.*;

/**
 * Emulates the memory associated with a Chip 8 computer. Note - due to the
 * fact that Java does not have a native unsigned byte, all memory values are
 * stored as shorts instead. While having a separate class for memory access
 * may seem to be unwarranted, other architectures used to perform memory
 * mapped I/O. Since the I/O routines were accessed through memory addresses,
 * it makes more sense to have a separate class responsible for all memory.
 *
 * @author Craig Thomas
 */
public class Memory
{
    // Acceptable memory sizes
    public static final int MEMORY_4K = 4096;
    public static final int MEMORY_64K = 65536;

    // The internal storage array for the emulator's memory
    protected short[] memory;

    // The total size of emulator memory
    private int size;

    /**
     * Alternate constructor for the memory object. The memory object will default to
     * 64K.
     */
    public Memory() {
        this(false);
    }

    /**
     * Default constructor for the memory object. The user must set the
     * maximum size of the memory upon creation.
     *
     * @param memorySize4k if True, will set the maximum memory size to 4K, otherwise 64k
     */
    public Memory(boolean memorySize4k) {
        this.size = (memorySize4k) ? MEMORY_4K : MEMORY_64K;
        this.memory = new short[size];
    }

    /**
     * Reads a single byte value from memory.
     *
     * @param location The memory location to read from
     * @return The value read from memory
     */
    public short read(int location) {
        if (location > size) {
            throw new IllegalArgumentException("location must be less than memory size");
        }

        if (location < 0) {
            throw new IllegalArgumentException("location must be 0 or larger");
        }

        return (short) (memory[location] & 0xFF);
    }

    /**
     * Writes a single byte to memory.
     *
     * @param value    The value to write to memory
     * @param location The memory location to write to
     */
    public void write(int value, int location) {
        if (location > size) {
            throw new IllegalArgumentException("location must be less than memory size");
        }

        if (location < 0) {
            throw new IllegalArgumentException("location must be 0 or larger");
        }

        memory[location] = (short) (value & 0xFF);
    }

    /**
     * Returns the size of memory allocated to the emulator.
     *
     * @return the memory size in bytes
     */
    public int getSize() {
        return size;
    }

    /**
     * Load a file full of bytes into emulator memory.
     *
     * @param stream The open stream to read from
     * @param offset The memory location to start loading the file into
     */
    public boolean loadStreamIntoMemory(InputStream stream, int offset) {
        return IO.copyStreamToShortArray(stream, memory, offset);
    }
}
