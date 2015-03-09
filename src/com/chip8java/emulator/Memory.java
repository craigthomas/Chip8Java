/*
 * Copyright (C) 2013 Craig Thomas
 * This project uses an MIT style license - see LICENSE for details.
 */
package com.chip8java.emulator;

import org.apache.commons.io.*;

import java.io.*;
import java.util.logging.Logger;

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
public class Memory {

	// The default size of memory
	public static final int MEMORY_4K = 0x1000;
	// The internal storage array for the emulator's memory
	protected short [] memory;
	// The total size of emulator memory
	private int size;
    // The logger for the class
    private final static Logger LOGGER = Logger.getLogger(Memory.class.getName());


    /**
	 * Default constructor for the memory object. The user must set the 
	 * maximum size of the memory upon creation.
	 * 
	 * @param size The size of memory to allocate in bytes
	 */
	public Memory(int size) {
		this.memory = new short[size];
		this.size = size;
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
		
		return (short)(memory[location] & 0xFF);
	}
	
	/**
	 * Writes a single byte to memory.
	 * 
	 * @param value The value to write to memory
	 * @param location The memory location to write to
	 */
	public void write(int value, int location) {
		if (location > size) {
			throw new IllegalArgumentException("location must be less than memory size");
		}
		
		if (location < 0) {
			throw new IllegalArgumentException("location must be 0 or larger");
		}

		memory[location] = (short)(value & 0xFF);
	}
	
	/**
	 * Load a file full of bytes into emulator memory.
	 * 
	 * @param file The File to load
	 * @param offset The memory location to start loading the file into
	 */
	public boolean loadStreamIntoMemory(InputStream stream, int offset) {
        try {
            byte[] data = IOUtils.toByteArray(stream);
            int currentOffset = offset;
            for (byte theByte : data) {
                int value = theByte;
                write(value, currentOffset);
                currentOffset++;
            }
            return true;
        } catch (IOException e) {
            LOGGER.severe("Error reading from stream");
            LOGGER.severe(e.getMessage());
            return false;
        }
	}
}
