/*
 * Copyright (C) 2013-2024 Craig Thomas
 * This project uses an MIT style license - see LICENSE for details.
 */
package ca.craigthomas.chip8java.emulator.components;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;
import javax.sound.midi.*;

/**
 * A class to emulate a Super Chip 8 CPU. There are several good resources out on the
 * web that describe the internals of the Chip 8 CPU. For example:
 * <p>
 * http://devernay.free.fr/hacks/chip8/C8TECH10.HTM
 * http://michael.toren.net/mirrors/chip8/chip8def.htm
 * <p>
 * As usual, a simple Google search will find you other excellent examples.
 *
 * @author Craig Thomas
 */
public class CentralProcessingUnit extends Thread
{
    // The normal mode for the CPU
    protected final static int MODE_NORMAL = 1;

    // The extended mode for the CPU
    protected final static int MODE_EXTENDED = 2;

    // The logger for the class
    private final static Logger LOGGER = Logger.getLogger(Emulator.class.getName());

    // The number of milliseconds for the delay timer
    private static final long DELAY_INTERVAL = 17;

    // The total number of registers in the Chip 8 CPU
    private static final int NUM_REGISTERS = 16;

    // The start location of the program counter
    public static final int PROGRAM_COUNTER_START = 0x200;

    // The start location of the stack pointer
    private static final int STACK_POINTER_START = 0x52;

    // The internal 8-bit registers
    protected short[] v;

    // The RPL register storage
    protected short[] rpl;

    // The index register
    protected int index;

    // The stack pointer register
    protected int stack;

    // The program counter
    protected int pc;

    // The delay register
    protected short delay;

    // The sound register
    protected short sound;

    // The current operand
    protected int operand;

    // The current pitch
    protected int pitch;

    // The current sound playback rate
    protected double playbackRate;

    // The internal memory for the Chip 8
    private final Memory memory;

    // The screen object for the Chip 8
    private final Screen screen;

    // The keyboard object for the Chip 8
    private final Keyboard keyboard;

    // A Random number generator used for the class
    private final Random random;

    // A description of the last operation
    protected String lastOpDesc;

    // A Midi device for simple tone generation
    private Synthesizer synthesizer;

    // The Midi channel to perform playback on
    private MidiChannel midiChannel;

    // The current operating mode for the CPU
    protected int mode;

    public static final int DEFAULT_CPU_CYCLE_TIME = 0;

    CentralProcessingUnit(Memory memory, Keyboard keyboard, Screen screen) {
        this.random = new Random();
        this.memory = memory;
        this.screen = screen;
        this.keyboard = keyboard;
        Timer timer = new Timer("Delay Timer");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                decrementTimers();
            }
        }, DELAY_INTERVAL, DELAY_INTERVAL);
        mode = MODE_NORMAL;

        try {
            synthesizer = MidiSystem.getSynthesizer();
            synthesizer.open();
            midiChannel = synthesizer.getChannels()[0];
        } catch (MidiUnavailableException e) {
            LOGGER.warning("Midi device not available for sound playback");
        }

        reset();
    }

    /**
     * Fetch the next instruction from memory, increment the program counter
     * to the next instruction, and execute the instruction.
     */
    public void fetchIncrementExecute() {
        operand = memory.read(pc);
        operand = operand << 8;
        operand += memory.read(pc + 1);
        operand = operand & 0x0FFFF;
        pc += 2;
        int opcode = (operand & 0x0F000) >> 12;
        executeInstruction(opcode);
    }

    /**
     * Given an opcode, execute the correct function.
     *
     * @param opcode The operation to execute
     */
    protected void executeInstruction(int opcode) {
        switch (opcode) {
            case 0x0:
                switch (operand & 0x00FF) {
                    case 0xE0:
                        screen.clearScreen();
                        lastOpDesc = "CLS";
                        break;

                    case 0xEE:
                        returnFromSubroutine();
                        break;

                    case 0xFB:
                        scrollRight();
                        break;

                    case 0xFC:
                        scrollLeft();
                        break;

                    case 0xFD:
                        kill();
                        break;

                    case 0xFE:
                        disableExtendedMode();
                        break;

                    case 0xFF:
                        enableExtendedMode();
                        break;

                    default:
                        if ((operand & 0xF0) == 0xC0) {
                            scrollDown(operand);
                        } else {
                            lastOpDesc = "Operation " + toHex(operand, 4) + " not supported";
                        }
                        break;
                }
                break;

            case 0x1:
                jumpToAddress();
                break;

            case 0x2:
                jumpToSubroutine();
                break;

            case 0x3:
                skipIfRegisterEqualValue();
                break;

            case 0x4:
                skipIfRegisterNotEqualValue();
                break;

            case 0x5:
                skipIfRegisterEqualRegister();
                break;

            case 0x6:
                moveValueToRegister();
                break;

            case 0x7:
                addValueToRegister();
                break;

            case 0x8:
                switch (operand & 0x000F) {
                    case 0x0:
                        moveRegisterIntoRegister();
                        break;

                    case 0x1:
                        logicalOr();
                        break;

                    case 0x2:
                        logicalAnd();
                        break;

                    case 0x3:
                        exclusiveOr();
                        break;

                    case 0x4:
                        addRegisterToRegister();
                        break;

                    case 0x5:
                        subtractRegisterFromRegister();
                        break;

                    case 0x6:
                        rightShift();
                        break;

                    case 0x7:
                        subtractRegisterFromRegister1();
                        break;

                    case 0xE:
                        leftShift();
                        break;

                    default:
                        lastOpDesc = "Operation " + toHex(operand, 4) + " not supported";
                        break;
                }
                break;

            case 0x9:
                skipIfRegisterNotEqualRegister();
                break;

            case 0xA:
                loadIndexWithValue();
                break;

            case 0xB:
                jumpToRegisterPlusValue();
                break;

            case 0xC:
                generateRandomNumber();
                break;

            case 0xD:
                drawSprite();
                break;

            case 0xE:
                switch (operand & 0x00FF) {
                    case 0x9E:
                        skipIfKeyPressed();
                        break;

                    case 0xA1:
                        skipIfKeyNotPressed();
                        break;

                    default:
                        lastOpDesc = "Operation " + toHex(operand, 4) + " not supported";
                        break;
                }
                break;

            case 0xF:
                switch (operand & 0x00FF) {
                    case 0x07:
                        moveDelayTimerIntoRegister();
                        break;

                    case 0x0A:
                        waitForKeypress();
                        break;

                    case 0x15:
                        moveRegisterIntoDelayRegister();
                        break;

                    case 0x18:
                        moveRegisterIntoSoundRegister();
                        break;

                    case 0x1E:
                        addRegisterIntoIndex();
                        break;

                    case 0x29:
                        loadIndexWithSprite();
                        break;

                    case 0x30:
                        loadIndexWithExtendedSprite();
                        break;

                    case 0x33:
                        storeBCDInMemory();
                        break;

                    case 0x3A:
                        loadPitch();
                        break;

                    case 0x55:
                        storeRegistersInMemory();
                        break;

                    case 0x65:
                        readRegistersFromMemory();
                        break;

                    case 0x75:
                        storeRegistersInRPL();
                        break;

                    case 0x85:
                        readRegistersFromRPL();
                        break;

                    default:
                        lastOpDesc = "Operation " + toHex(operand, 4) + " not supported";
                        break;
                }
                break;

            default:
                lastOpDesc = "Operation " + toHex(operand, 4) + " not supported";
                break;
        }
    }

    /**
     * 00FB - SCRR
     * Scrolls the screen right by 4 pixels.
     */
    private void scrollRight() {
        screen.scrollRight();
        lastOpDesc = "Scroll Right";
    }

    /**
     * 00FC - SCRL
     * Scrolls the screen left by 4 pixels.
     */
    private void scrollLeft() {
        screen.scrollLeft();
        lastOpDesc = "Scroll Left";
    }

    /**
     * 00EE - RTS
     * Return from subroutine. Pop the current value in the stack pointer off of
     * the stack, and set the program counter to the value popped.
     */
    protected void returnFromSubroutine() {
        stack -= 1;
        pc = memory.read(stack) << 8;
        stack -= 1;
        pc += memory.read(stack);
        lastOpDesc = "RTS";
    }

    /**
     * 1nnn - JUMP nnn
     * Jump to address.
     */
    protected void jumpToAddress() {
        pc = operand & 0x0FFF;
        lastOpDesc = "JUMP " + toHex(operand & 0x0FFF, 3);
    }

    /**
     * 2nnn - CALL nnn
     * Jump to subroutine. Save the current program counter on the stack.
     */
    protected void jumpToSubroutine() {
        memory.write(pc & 0x00FF, stack);
        stack += 1;
        memory.write((pc & 0xFF00) >> 8, stack);
        stack += 1;
        pc = operand & 0x0FFF;
        lastOpDesc = "CALL " + toHex(operand & 0x0FFF, 3);
    }

    /**
     * 3xnn - SKE Vx, nn
     * Skip if register contents equal to constant value. The program counter is
     * updated to skip the next instruction by advancing it by 2 bytes.
     */
    protected void skipIfRegisterEqualValue() {
        int x = (operand & 0x0F00) >> 8;
        if (v[x] == (operand & 0x00FF)) {
            pc += 2;
        }
        lastOpDesc = "SKE V" + toHex(x, 1) + ", " + toHex(operand & 0x00FF, 2);
    }

    /**
     * 4xnn - SKNE Vx, nn
     * Skip if register contents not equal to constant value. The program
     * counter is updated to skip the next instruction by advancing it by 2
     * bytes.
     */
    protected void skipIfRegisterNotEqualValue() {
        int x = (operand & 0x0F00) >> 8;
        if (v[x] != (operand & 0x00FF)) {
            pc += 2;
        }
        lastOpDesc = "SKNE V" + toHex(x, 1) + ", " + toHex(operand & 0x00FF, 2);
    }

    /**
     * 5xy0 - SKE Vx, Vy
     * Skip if source register is equal to target register. The program counter
     * is updated to skip the next instruction by advancing it by 2 bytes.
     */
    protected void skipIfRegisterEqualRegister() {
        int x = (operand & 0x0F00) >> 8;
        int y = (operand & 0x00F0) >> 4;
        if (v[x] == v[y]) {
            pc += 2;
        }
        lastOpDesc = "SKE V" + toHex(x, 1) + ", V" + toHex(y, 1);
    }

    /**
     * 6xnn - LOAD Vx, nn
     * Move the constant value into the specified register.
     */
    protected void moveValueToRegister() {
        int x = (operand & 0x0F00) >> 8;
        v[x] = (short) (operand & 0x00FF);
        lastOpDesc = "LOAD V" + toHex(x, 1) + ", " + toHex(operand & 0x00FF, 2);
    }

    /**
     * 7xnn - ADD Vx, nn
     * Add the constant value to the specified register.
     */
    protected void addValueToRegister() {
        int x = (operand & 0x0F00) >> 8;
        v[x] = (short) ((v[x] + (operand & 0x00FF)) % 256);
        lastOpDesc = "ADD V" + toHex(x, 1) + ", " + toHex(operand & 0x00FF, 2);
    }

    /**
     * 8xy0 - LOAD Vx, Vy
     * Move the value of the source register into the value of the target
     * register.
     */
    protected void moveRegisterIntoRegister() {
        int x = (operand & 0x0F00) >> 8;
        int y = (operand & 0x00F0) >> 4;
        v[x] = v[y];
        lastOpDesc = "LOAD V" + toHex(x, 1) + ", V" + toHex(y, 1);
    }

    /**
     * 8xy1 - OR Vx, Vy
     * Perform a logical OR operation between the source and the target
     * register, and store the result in the target register.
     */
    protected void logicalOr() {
        int x = (operand & 0x0F00) >> 8;
        int y = (operand & 0x00F0) >> 4;
        v[x] |= v[y];
        lastOpDesc = "OR V" + toHex(x, 1) + ", V" + toHex(y, 1);
    }

    /**
     * 8xy2 - AND Vx, Vy
     * Perform a logical AND operation between the source and the target
     * register, and store the result in the target register.
     */
    protected void logicalAnd() {
        int x = (operand & 0x0F00) >> 8;
        int y = (operand & 0x00F0) >> 4;
        v[x] &= v[y];
        lastOpDesc = "AND V" + toHex(x, 1) + ", V" + toHex(y, 1);
    }

    /**
     * 8xy3 - XOR Vx, Vy
     * Perform a logical XOR operation between the source and the target
     * register, and store the result in the target register.
     */
    protected void exclusiveOr() {
        int x = (operand & 0x0F00) >> 8;
        int y = (operand & 0x00F0) >> 4;
        v[x] ^= v[y];
        lastOpDesc = "XOR V" + toHex(x, 1) + ", V" + toHex(y, 1);
    }

    /**
     * 8xy4 - ADD Vx, Vy
     * Add the value in the source register to the value in the target register,
     * and store the result in the target register. If a carry is generated, set
     * a carry flag in register VF.
     */
    protected void addRegisterToRegister() {
        int x = (operand & 0x0F00) >> 8;
        int y = (operand & 0x00F0) >> 4;
        short carry = (v[x] + v[y]) > 255 ? (short) 1 : (short) 0;
        v[x] = (short) ((v[x] + v[y]) % 256);
        v[0xF] = carry;
        lastOpDesc = "ADD V" + toHex(x, 1) + ", V" + toHex(y, 1);
    }

    /**
     * 8xy5 - SUB Vx, Vy
     * Subtract the value in the target register from the value in the source
     * register, and store the result in the target register. If a borrow is NOT
     * generated, set a carry flag in register VF.
     */
    protected void subtractRegisterFromRegister() {
        int x = (operand & 0x0F00) >> 8;
        int y = (operand & 0x00F0) >> 4;
        short borrow = (v[x] >= v[y]) ? (short) 1 : (short) 0;
        v[x] = (v[x] >= v[y]) ? (short) (v[x] - v[y]) : (short) (256 + v[x] - v[y]);
        v[0xF] = borrow;
        lastOpDesc = "SUBN V" + toHex(x, 1) + ", V" + toHex(y, 1);
    }

    /**
     * 8xy6 - SHR Vx, Vy
     * Shift the bits in the specified register 1 bit to the right. Bit 0 will
     * be shifted into register VF.
     */
    protected void rightShift() {
        int x = (operand & 0x0F00) >> 8;
        int y = (operand & 0x00F0) >> 4;
        short bit_one = (short) (v[y] & 0x1);
        v[x] = (short) ((v[y] >> 1) & 0xFF);
        v[0xF] = bit_one;
        lastOpDesc = "SHR V" + toHex(x, 1) + ", V" + toHex(y, 1);
    }

    /**
     * 8xy7 - SUBN Vx, Vy
     * Subtract the value in the target register from the value in the source
     * register, and store the result in the target register. If a borrow is NOT
     * generated, set a carry flag in register VF.
     */
    protected void subtractRegisterFromRegister1() {
        int x = (operand & 0x0F00) >> 8;
        int y = (operand & 0x00F0) >> 4;
        short not_borrow = (v[y] >= v[x]) ? (short) 1 : (short) 0;
        v[x] = (v[y] >= v[x]) ? (short) (v[y] - v[x]) : (short) (256 + v[y] - v[x]);
        v[0xF] = not_borrow;
        lastOpDesc = "SUBN V" + toHex(x, 1) + ", V" + toHex(y, 1);
    }

    /**
     * 8xyE - SHL Vx, Vy
     * Shift the bits in the specified register 1 bit to the left. Bit 7 will be
     * shifted into register VF.
     */
    protected void leftShift() {
        int x = (operand & 0x0F00) >> 8;
        int y = (operand & 0x00F0) >> 4;
        short bit_seven = (short) ((v[y] & 0x80) >> 7);
        v[x] = (short) ((v[y] << 1) & 0xFF);
        v[0xF] = bit_seven;
        lastOpDesc = "SHL V" + toHex(x, 1) + ", V" + toHex(y, 1);
    }

    /**
     * 9xy0 - SKNE Vx, Vy
     * Skip if source register is equal to target register. The program counter
     * is updated to skip the next instruction by advancing it by 2 bytes.
     */
    protected void skipIfRegisterNotEqualRegister() {
        int x = (operand & 0x0F00) >> 8;
        int y = (operand & 0x00F0) >> 4;
        if (v[x] != v[y]) {
            pc += 2;
        }
        lastOpDesc = "SKNE V" + toHex(x, 1) + ", V" + toHex(y, 1);
    }

    /**
     * Annn - LOAD I, nnn
     * Load index register with constant value.
     */
    protected void loadIndexWithValue() {
        index = (short) (operand & 0x0FFF);
        lastOpDesc = "LOAD I, " + toHex(index, 3);
    }

    /**
     * Bnnn - JUMP V0 + nnn
     * Load the program counter with the memory value located at the specified
     * operand plus the value of the index register.
     */
    protected void jumpToRegisterPlusValue() {
        pc = v[0] + (operand & 0x0FFF);
        lastOpDesc = "JUMP V0 + " + toHex(operand & 0x0FFF, 3);
    }

    /**
     * Cxnn - RAND Vx, nn
     * A random number between 0 and 255 is generated. The contents of it are
     * then ANDed with the constant value passed in the operand. The result is
     * stored in the target register.
     */
    protected void generateRandomNumber() {
        int value = operand & 0x00FF;
        int x = (operand & 0x0F00) >> 8;
        v[x] = (short) (value & random.nextInt(256));
        lastOpDesc = "RAND V" + toHex(x, 1) + ", " + toHex(value, 2);
    }

    /**
     * Dxyn - DRAW x, y, num_bytes
     * Draws the sprite pointed to in the index register at the specified x and
     * y coordinates. Drawing is done via an XOR routine, meaning that if the
     * target pixel is already turned on, and a pixel is set to be turned on at
     * that same location via the draw, then the pixel is turned off. The
     * routine will wrap the pixels if they are drawn off the edge of the
     * screen. Each sprite is 8 bits (1 byte) wide. The num_bytes parameter sets
     * how tall the sprite is. Consecutive bytes in the memory pointed to by the
     * index register make up the bytes of the sprite. Each bit in the sprite
     * byte determines whether a pixel is turned on (1) or turned off (0). If
     * writing a pixel to a location causes that pixel to be turned off, then VF
     * will be set to 1.
     */
    protected void drawSprite() {
        int xRegister = (operand & 0x0F00) >> 8;
        int yRegister = (operand & 0x00F0) >> 4;
        int xPos = v[xRegister];
        int yPos = v[yRegister];
        int numBytes = (operand & 0xF);
        v[0xF] = 0;

        String drawOperation = "DRAW";
        if ((mode == MODE_EXTENDED) && (numBytes == 0)) {
            drawExtendedSprite(xPos, yPos);
            drawOperation = "DRAWEX";
        } else {
            drawNormalSprite(xPos, yPos, numBytes);
        }
        lastOpDesc = drawOperation + " V" + toHex(xRegister, 1) + ", V" + toHex(yRegister, 1);
    }

    /**
     * Draws the sprite on the screen based on the Super Chip 8 extensions.
     * Sprites are considered to be 16 bytes high.
     *
     * @param xPos the x position to draw the sprite at
     * @param yPos the y position to draw the sprite at
     */
    private void drawExtendedSprite(int xPos, int yPos) {
        for (int yIndex = 0; yIndex < 16; yIndex++) {
            for (int xByte = 0; xByte < 2; xByte++) {
                short colorByte = memory.read(index + (yIndex * 2) + xByte);
                int yCoord = yPos + yIndex;
                yCoord = yCoord % screen.getHeight();

                int mask = 0x80;

                for (int xIndex = 0; xIndex < 8; xIndex++) {
                    int xCoord = xPos + xIndex + (xByte * 8);
                    xCoord = xCoord % screen.getWidth();

                    boolean turnOn = (colorByte & mask) > 0;
                    boolean currentOn = screen.getPixel(xCoord, yCoord);

                    v[0xF] += (turnOn && currentOn) ? (short) 1 : (short) 0;
                    screen.drawPixel(xCoord, yCoord, turnOn ^ currentOn);
                    mask = mask >> 1;
                }
            }
        }
    }

    /**
     * Draws a sprite on the screen while in NORMAL mode.
     *
     * @param xPos the X position of the sprite
     * @param yPos the Y position of the sprite
     * @param numBytes the number of bytes to draw
     */
    private void drawNormalSprite(int xPos, int yPos, int numBytes) {
        for (int yIndex = 0; yIndex < numBytes; yIndex++) {
            short colorByte = memory.read(index + yIndex);
            int yCoord = yPos + yIndex;
            yCoord = yCoord % screen.getHeight();

            int mask = 0x80;

            for (int xIndex = 0; xIndex < 8; xIndex++) {
                int xCoord = xPos + xIndex;
                xCoord = xCoord % screen.getWidth();

                boolean turnOn = (colorByte & mask) > 0;
                boolean currentOn = screen.getPixel(xCoord, yCoord);

                v[0xF] |= (turnOn && currentOn) ? (short) 1 : (short) 0;
                screen.drawPixel(xCoord, yCoord, turnOn ^ currentOn);
                mask = mask >> 1;
            }
        }
    }

    /**
     * Ex9E - SKPR Vx
     * Check to see if the key specified in the source register is pressed, and
     * if it is, skips the next instruction.
     */
    protected void skipIfKeyPressed() {
        int x = (operand & 0x0F00) >> 8;
        int keyToCheck = v[x];
        if (keyboard.getCurrentKey() == keyToCheck) {
            pc += 2;
        }
        lastOpDesc = "SKPR V" + toHex(x, 1);
    }

    /**
     * ExA1 - SKUP Vx
     * Check for the specified keypress in the source register and if it is NOT
     * pressed, will skip the next instruction.
     */
    protected void skipIfKeyNotPressed() {
        int x = (operand & 0x0F00) >> 8;
        int keyToCheck = v[x];
        if (keyboard.getCurrentKey() != keyToCheck) {
            pc += 2;
        }
        lastOpDesc = "SKUP V" + toHex(x, 1);
    }

    /**
     * Fx07 - LOAD Vx, DELAY
     * Move the value of the delay timer into the target register.
     */
    protected void moveDelayTimerIntoRegister() {
        int x = (operand & 0x0F00) >> 8;
        v[x] = delay;
        lastOpDesc = "LOAD V" + toHex(x, 1) + ", DELAY";
    }

    /**
     * Fx0A - KEYD Vx
     * Stop execution until a key is pressed. Move the value of the key pressed
     * into the specified register.
     */
    protected void waitForKeypress() {
        int x = (operand & 0x0F00) >> 8;
        int currentKey = keyboard.getCurrentKey();
        while (currentKey == 0) {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            currentKey = keyboard.getCurrentKey();
        }
        v[x] = (short) currentKey;
        lastOpDesc = "KEYD V" + toHex(x, 1);
    }

    /**
     * Fx15 - LOAD DELAY, Vx
     * Move the value stored in the specified source register into the delay
     * timer.
     */
    protected void moveRegisterIntoDelayRegister() {
        int x = (operand & 0x0F00) >> 8;
        delay = v[x];
        lastOpDesc = "LOAD DELAY, V" + toHex(x, 1);
    }

    /**
     * Fx18 - LOAD SOUND, Vx
     * Move the value stored in the specified source register into the sound
     * timer.
     */
    protected void moveRegisterIntoSoundRegister() {
        int x = (operand & 0x0F00) >> 8;
        sound = v[x];
        lastOpDesc = "LOAD SOUND, V" + toHex(x, 1);
    }

    /**
     * Fx1E - ADD I, Vx
     * Add the value of the register into the index register value.
     */
    protected void addRegisterIntoIndex() {
        int x = (operand & 0x0F00) >> 8;
        index += v[x];
        lastOpDesc = "ADD I, V" + toHex(x, 1);
    }

    /**
     * Fx29 - LOAD I, Vx
     * Load the index with the sprite indicated in the source register. All
     * sprites are 5 bytes long, so the location of the specified sprite is its
     * index multiplied by 5.
     */
    protected void loadIndexWithSprite() {
        int x = (operand & 0x0F00) >> 8;
        index = v[x] * 5;
        lastOpDesc = "LOAD I, V" + toHex(x, 1);
    }

    /**
     * Fx30 - LOAD I, Vx
     * Load the index with the sprite indicated in the source register. All
     * sprites are 10 bytes long, so the location of the specified sprite is its
     * index multiplied by 10.
     */
    protected void loadIndexWithExtendedSprite() {
        int x = (operand & 0x0F00) >> 8;
        index = v[x] * 10;
        lastOpDesc = "LOADEXT I, V" + toHex(x, 1);
    }

    /**
     * Fx33 - BCD Vx
     * Take the value stored in source and place the digits in the following
     * locations:
     * <p>
     * hundreds -> self.memory[index] tens -> self.memory[index + 1] ones ->
     * self.memory[index + 2]
     * <p>
     * For example, if the value is 123, then the following values will be
     * placed at the specified locations:
     * <p>
     * 1 -> self.memory[index] 2 -> self.memory[index + 1] 3 ->
     * self.memory[index + 2]
     */
    protected void storeBCDInMemory() {
        int x = (operand & 0x0F00) >> 8;
        int bcdValue = v[x];
        memory.write(bcdValue / 100, index);
        memory.write((bcdValue % 100) / 10, index + 1);
        memory.write((bcdValue % 100) % 10, index + 2);
        lastOpDesc = "BCD V" + toHex(x, 1) + " (" + bcdValue + ")";
    }

    /**
     * Fx3A - Pitch Vx
     * Loads the value from register x into the pitch register.
     */
    protected void loadPitch() {
        int x = (operand & 0x0F00) >> 8;
        pitch = v[x];
        playbackRate = 4000 * Math.pow(2.0, ((pitch - 64) / 48));
        lastOpDesc = "PITCH V" + toHex(x, 1) + " (" + v[x] + ")";
    }

    /**
     * Fn55 - STOR [I]
     * Store the V registers in the memory pointed to by the index
     * register.
     */
    protected void storeRegistersInMemory() {
        int n = (operand & 0x0F00) >> 8;
        for (int counter = 0; counter <= n; counter++) {
            memory.write(v[counter], index + counter);
        }
        lastOpDesc = "STOR " + toHex(n, 1);
    }

    /**
     * Fn65 - LOAD V, I
     * Read the V registers from the memory pointed to by the index
     * register.
     */
    protected void readRegistersFromMemory() {
        int n = (operand & 0x0F00) >> 8;
        for (int counter = 0; counter <= n; counter++) {
            v[counter] = memory.read(index + counter);
        }
        lastOpDesc = "READ " + toHex(n, 1);
    }

    /**
     * Fn75 - STORRPL n
     * Stores the values from the V registers into the RPL registers.
     */
    protected void storeRegistersInRPL() {
        int n = (operand & 0x0F00) >> 8;
        System.arraycopy(v, 0, rpl, 0, n + 1);
        lastOpDesc = "STORRPL " + toHex(n, 1);
    }

    /**
     * Fn85 - READRPL n
     * Reads the values from the RPL registers back into the V registers.
     */
    protected void readRegistersFromRPL() {
        int n = (operand & 0x0F00) >> 8;
        System.arraycopy(rpl, 0, v, 0, n + 1);
        lastOpDesc = "READRPL " + toHex(n, 1);
    }

    /**
     * Reset the CPU by blanking out all registers, and resetting the stack
     * pointer and program counter to their starting values.
     */
    public void reset() {
        v = new short[NUM_REGISTERS];
        rpl = new short[NUM_REGISTERS];
        pc = PROGRAM_COUNTER_START;
        stack = STACK_POINTER_START;
        index = 0;
        delay = 0;
        sound = 0;
        pitch = 64;
        playbackRate = 4000.0;
        if (screen != null) {
            screen.clearScreen();
        }
    }

    /**
     * Decrement the delay timer and the sound timer if they are not zero.
     */
    private void decrementTimers() {
        delay -= (delay != 0) ? (short) 1 : (short) 0;

        if (sound != 0) {
            sound--;
            midiChannel.noteOn(60, 50);
        }
        if (sound == 0 && midiChannel != null) {
            midiChannel.noteOff(60);
        }
    }

    /**
     * Turns on extended mode for the CPU.
     */
    protected void enableExtendedMode() {
        screen.setExtendedScreenMode();
        mode = MODE_EXTENDED;
    }

    /**
     * Turns on extended mode for the CPU.
     */
    private void disableExtendedMode() {
        screen.setNormalScreenMode();
        mode = MODE_NORMAL;
    }

    /**
     * Scrolls the screen down by the specified number of pixels.
     *
     * @param operand the operand to parse
     */
    private void scrollDown(int operand) {
        int numPixels = operand & 0xF;
        screen.scrollDown(numPixels);
        lastOpDesc = "Scroll Down " + numPixels;
    }

    /**
     * Return the string of the last operation that occurred.
     *
     * @return A string containing the last operation
     */
    protected String getOpShortDesc() {
        return lastOpDesc;
    }

    /**
     * Return a String representation of the operand.
     *
     * @return A string containing the operand
     */
    protected String getOp() {
        return toHex(operand, 4);
    }

    /**
     * Converts a number into a hex string containing the number of digits
     * specified.
     *
     * @param number    The number to convert to hex
     * @param numDigits The number of digits to include
     * @return The String representation of the hex value
     */
    protected static String toHex(int number, int numDigits) {
        String format = "%0" + numDigits + "X";
        return String.format(format, number);
    }

    /**
     * Returns a status line containing the contents of the Index, Delay Timer,
     * Sound Timer, Program Counter, current operand value, and a short
     * description of the last operation run.
     *
     * @return A String containing Index, Delay, Sound, PC, operand and op
     */
    public String cpuStatusLine1() {
        return "I:" + toHex(index, 4) + " DT:" + toHex(delay, 2) + " ST:" +
                toHex(sound, 2) + " PC:" + toHex(pc, 4) + " " +
                getOp() + " " + getOpShortDesc();
    }

    /**
     * Returns a status line containing the values of the first 8 registers.
     *
     * @return A String containing the values of the first 8 registers
     */
    public String cpuStatusLine2() {
        return "V0:" + toHex(v[0], 2) + " V1:" + toHex(v[1], 2) + " V2:" +
                toHex(v[2], 2) + " V3:" + toHex(v[3], 2) + " V4:" +
                toHex(v[4], 2) + " V5:" + toHex(v[5], 2) + " V6:" +
                toHex(v[6], 2) + " V7:" + toHex(v[7], 2);
    }

    /**
     * Returns a status line containing the values of the last 8 registers.
     *
     * @return A String containing the values of the last 8 registers
     */
    public String cpuStatusLine3() {
        return "V8:" + toHex(v[8], 2) + " V9:" + toHex(v[9], 2) + " VA:" +
                toHex(v[10], 2) + " VB:" + toHex(v[11], 2) + " VC:" +
                toHex(v[12], 2) + " VD:" + toHex(v[13], 2) + " VE:" +
                toHex(v[14], 2) + " VF:" + toHex(v[15], 2);
    }

    /**
     * Stops CPU execution.
     */
    public void kill() {
        synthesizer.close();
    }
}
