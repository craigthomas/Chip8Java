/*
 * Copyright (C) 2013-2015 Craig Thomas
 * This project uses an MIT style license - see LICENSE for details.
 */
package com.chip8java.emulator;

import static org.junit.Assert.*;

import java.io.*;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the Memory module.
 */
public class MemoryTest {

    private static final String TEST_ROM = "test/resources/test.chip8";
    private Memory mMemory;
    private Random random;
    
    @Before
    public void setUp() {
        mMemory = new Memory(Memory.MEMORY_4K);
        random = new Random();
        for (int location = 0; location < Memory.MEMORY_4K; location++) {
            mMemory.memory[location] = (short) (random.nextInt(Short.MAX_VALUE + 1) & 0xFF);
        }
    }

    public InputStream openStream(String filename) {
        InputStream inputStream;
        try {
            inputStream = new FileInputStream(new File(filename));
            return inputStream;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void closeStream(InputStream stream) {
        try {
            stream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Test
    public void testMemoryReadWorksCorrectly() {
        for (int location = 0; location < Memory.MEMORY_4K; location++) {
            assertEquals(mMemory.memory[location], mMemory.read(location));
        }
    }
    
    @Test
    public void testMemoryWriteWorksCorrectly() {
        for (int location = 0; location < Memory.MEMORY_4K; location++) {
            short value = (short) (random.nextInt(Short.MAX_VALUE + 1) & 0xFF);
            mMemory.write(value, location);
            assertEquals(value, mMemory.memory[location]);
        }
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testMemoryReadThrowsExceptionWhenLocationOutOfBounds() {
        mMemory.read(16384);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testMemoryReadThrowsExceptionWhenLocationNegative() {
        mMemory.read(-16384);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testMemoryWriteThrowsExceptionWhenLocationOutOfBounds() {
        mMemory.write(0, 16384);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testMemoryWriteThrowsExceptionWhenLocationNegative() {
        mMemory.write(0, -16384);
    }

    @Test
    public void testLoadRomIntoMemoryReturnsTrueOnGoodFilename() {
        InputStream inputStream = openStream(TEST_ROM);
        assertTrue(mMemory.loadStreamIntoMemory(inputStream, 0x200));
        closeStream(inputStream);
        assertEquals(0x61, mMemory.read(0x200));
        assertEquals(0x62, mMemory.read(0x201));
        assertEquals(0x63, mMemory.read(0x202));
        assertEquals(0x64, mMemory.read(0x203));
        assertEquals(0x65, mMemory.read(0x204));
        assertEquals(0x66, mMemory.read(0x205));
        assertEquals(0x67, mMemory.read(0x206));
    }

}
