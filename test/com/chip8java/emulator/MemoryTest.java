package com.chip8java.emulator;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Before;
import org.junit.Test;

public class MemoryTest {

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

}
