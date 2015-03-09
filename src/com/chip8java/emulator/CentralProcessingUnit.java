/*
 * Copyright (C) 2013-2015 Craig Thomas
 * This project uses an MIT style license - see LICENSE for details.
 */
package com.chip8java.emulator;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A class to emulate a Chip 8 CPU. There are several good resources out on the
 * web that describe the internals of the Chip 8 CPU. For example:
 * 
 * http://devernay.free.fr/hacks/chip8/C8TECH10.HTM
 * http://michael.toren.net/mirrors/chip8/chip8def.htm
 * 
 * As usual, a simple Google search will find you other excellent examples.
 * 
 * @author Craig Thomas
 */
public class CentralProcessingUnit extends Thread {

    // The number of milliseconds for the delay timer
    private static final long DELAY_INTERVAL = 17;
	// The total number of registers in the Chip 8 CPU
	private static final int NUM_REGISTERS = 16;
	// The start location of the program counter
	public static final int PROGRAM_COUNTER_START = 0x200;
	// The start location of the stack pointer
	public static final int STACK_POINTER_START = 0x52;
	// The internal 8-bit registers
	protected short[] v;
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
	// The internal memory for the Chip 8
	private Memory memory;
	// The mScreen object for the Chip 8
	private Screen mScreen;
	// The mKeyboard object for the Chip 8
	private Keyboard mKeyboard;
	// A Random number generator used for the class
	Random random;
	// A description of the last operation
	protected String lastOpDesc;
	// Create a timer to use to count down the timer registers
	private Timer timer;
	// Determines if CPU is in trace mode
	private boolean mTrace;
	// Determines if the CPU is in step mode
	private boolean mStep;
    // Determines if the CPU is paused
    private boolean mPaused;

	public CentralProcessingUnit(Memory memory, Keyboard keyboard, Screen screen) {
		this.random = new Random();
		this.memory = memory;
        mScreen = screen;
		mKeyboard = keyboard;
		mTrace = false;
		mStep = false;
        mPaused = false;
		timer = new Timer("Delay Timer");
		timer.schedule(new TimerTask() {
		    @Override
		    public void run() {
		        decrementTimers();
		    }
		}, DELAY_INTERVAL, DELAY_INTERVAL);
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
	public void executeInstruction(int opcode) {
		switch (opcode) {
		case 0x0:
			switch (operand & 0x00FF) {
			case 0xE0:
				mScreen.clearScreen();
				lastOpDesc = "CLS";
				break;
				
			case 0xEE:
				returnFromSubroutine();
				break;

			default:
                lastOpDesc = "Operation " + toHex(operand, 4) + " not supported";
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
			jumpToIndexPlusValue();
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

			case 0x33:
				storeBCDInMemory();
				break;

			case 0x55:
				storeRegistersInMemory();
				break;

			case 0x65:
				readRegistersFromMemory();
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
	 * Return from subroutine. Pop the current value in the stack pointer off of
	 * the stack, and set the program counter to the value popped.
	 */
	public void returnFromSubroutine() {
		stack -= 1;
		pc = memory.read(stack) << 8;
		stack -= 1;
		pc += memory.read(stack);
		lastOpDesc = "RTS";
	}

	/**
	 * Jump to address.
	 */
	public void jumpToAddress() {
		pc = operand & 0x0FFF;
		lastOpDesc = "JUMP " + toHex(operand & 0x0FFF, 3);
	}

	/**
	 * Jump to subroutine. Save the current program counter on the stack.
	 */
	public void jumpToSubroutine() {
		memory.write(pc & 0x00FF, stack);
		stack += 1;
		memory.write((pc & 0xFF00) >> 8, stack);
		stack += 1;
		pc = operand & 0x0FFF;
		lastOpDesc = "CALL " + toHex(operand & 0x0FFF, 3);		
	}

	/**
	 * Skip if register contents equal to constant value. The program counter is
	 * updated to skip the next instruction by advancing it by 2 bytes.
	 */
	public void skipIfRegisterEqualValue() {
		int sourceRegister = (operand & 0x0F00) >> 8;
		if (v[sourceRegister] == (operand & 0x00FF)) {
			pc += 2;
		}
		lastOpDesc = "SKE V" + toHex(sourceRegister, 1) + ", " + toHex(operand & 0x00FF, 2);		
	}

	/**
	 * Skip if register contents not equal to constant value. The program
	 * counter is updated to skip the next instruction by advancing it by 2
	 * bytes.
	 */
	public void skipIfRegisterNotEqualValue() {
		int sourceRegister = (operand & 0x0F00) >> 8;
		if (v[sourceRegister] != (operand & 0x00FF)) {
			pc += 2;
		}
		lastOpDesc = "SKNE V" + toHex(sourceRegister, 1) + ", " + toHex(operand & 0x00FF, 2);		
	}

	/**
	 * Skip if source register is equal to target register. The program counter
	 * is updated to skip the next instruction by advancing it by 2 bytes.
	 */
	public void skipIfRegisterEqualRegister() {
		int sourceRegister = (operand & 0x0F00) >> 8;
		int targetRegister = (operand & 0x00F0) >> 4;
		if (v[sourceRegister] == v[targetRegister]) {
			pc += 2;
		}
		lastOpDesc = "SKE V" + toHex(sourceRegister, 1) + ", V" + toHex(targetRegister, 1);
	}

	/**
	 * Move the constant value into the specified register.
	 */
	public void moveValueToRegister() {
		int targetRegister = (operand & 0x0F00) >> 8;
		v[targetRegister] = (short) (operand & 0x00FF);
		lastOpDesc = "LOAD V" + toHex(targetRegister, 1) + ", " + toHex(operand & 0x00FF, 2);
	}

	/**
	 * Add the constant value to the specified register.
	 */
	public void addValueToRegister() {
		int targetRegister = (operand & 0x0F00) >> 8;
		int temp = v[targetRegister] + (operand & 0x00FF);
		v[targetRegister] = (temp < 256) ? (short) temp : (short) (temp - 256);
		lastOpDesc = "ADD V" + toHex(targetRegister, 1) + ", " + toHex(operand & 0x00FF, 2);
	}

	/**
	 * Move the value of the source register into the value of the target
	 * register.
	 */
	public void moveRegisterIntoRegister() {
		int targetRegister = (operand & 0x0F00) >> 8;
		int sourceRegister = (operand & 0x00F0) >> 4;
		v[targetRegister] = v[sourceRegister];
		lastOpDesc = "LOAD V" + toHex(targetRegister, 1) + ", V" + toHex(sourceRegister, 1);
	}

	/**
	 * Perform a logical OR operation between the source and the target
	 * register, and store the result in the target register.
	 */
	public void logicalOr() {
		int targetRegister = (operand & 0x0F00) >> 8;
		int sourceRegister = (operand & 0x00F0) >> 4;
		v[targetRegister] |= v[sourceRegister];
		lastOpDesc = "OR V" + toHex(targetRegister, 1) + ", V" + toHex(sourceRegister, 1);
	}

	/**
	 * Perform a logical AND operation between the source and the target
	 * register, and store the result in the target register.
	 */
	public void logicalAnd() {
		int targetRegister = (operand & 0x0F00) >> 8;
		int sourceRegister = (operand & 0x00F0) >> 4;
		v[targetRegister] &= v[sourceRegister];
		lastOpDesc = "AND V" + toHex(targetRegister, 1) + ", V" + toHex(sourceRegister, 1);
	}

	/**
	 * Perform a logical XOR operation between the source and the target
	 * register, and store the result in the target register.
	 */
	public void exclusiveOr() {
		int targetRegister = (operand & 0x0F00) >> 8;
		int sourceRegister = (operand & 0x00F0) >> 4;
		v[targetRegister] ^= v[sourceRegister];
		lastOpDesc = "XOR V" + toHex(targetRegister, 1) + ", V" + toHex(sourceRegister, 1);
	}

	/**
	 * Add the value in the source register to the value in the target register,
	 * and store the result in the target register. If a carry is generated, set
	 * a carry flag in register VF.
	 */
	public void addRegisterToRegister() {
		int targetRegister = (operand & 0x0F00) >> 8;
		int sourceRegister = (operand & 0x00F0) >> 4;
		int temp = v[targetRegister] + v[sourceRegister];
		if (temp > 255) {
			v[targetRegister] = (short) (temp - 256);
			v[0xF] = 1;
		} else {
			v[targetRegister] = (short) temp;
			v[0xF] = 0;
		}
		lastOpDesc = "ADD V" + toHex(targetRegister, 1) + ", V" + toHex(sourceRegister, 1);
	}

	/**
	 * Subtract the value in the target register from the value in the source
	 * register, and store the result in the target register. If a borrow is NOT
	 * generated, set a carry flag in register VF.
	 */
	public void subtractRegisterFromRegister() {
		int targetRegister = (operand & 0x0F00) >> 8;
		int sourceRegister = (operand & 0x00F0) >> 4;
		int resultValue;
		if (v[targetRegister] > v[sourceRegister]) {
			resultValue = v[targetRegister] - v[sourceRegister];
			v[0xF] = 1;
		} else {
			resultValue = 256 + v[targetRegister] - v[sourceRegister];
			v[0xF] = 0;
		}
		v[targetRegister] = (short) resultValue;
		lastOpDesc = "SUBN V" + toHex(targetRegister, 1) + ", V" + toHex(sourceRegister, 1);
	}

	/**
	 * Shift the bits in the specified register 1 bit to the right. Bit 0 will
	 * be shifted into register VF.
	 */
	public void rightShift() {
		int sourceRegister = (operand & 0x0F00) >> 8;
		v[0xF] = (short) (v[sourceRegister] & 0x1);
		v[sourceRegister] = (short) (v[sourceRegister] >> 1);
		lastOpDesc = "SHR V" + toHex(sourceRegister, 1);
	}
	
   /**
     * Subtract the value in the target register from the value in the source
     * register, and store the result in the target register. If a borrow is NOT
     * generated, set a carry flag in register VF.
     */
    public void subtractRegisterFromRegister1() {
        int targetRegister = (operand & 0x0F00) >> 8;
        int sourceRegister = (operand & 0x00F0) >> 4;
        int resultValue;
        if (v[sourceRegister] > v[targetRegister]) {
            resultValue = v[sourceRegister] - v[targetRegister];
            v[0xF] = 1;
        } else {
            resultValue = 256 + v[sourceRegister] - v[targetRegister];
            v[0xF] = 0;
        }
        v[targetRegister] = (short) resultValue;
        lastOpDesc = "SUBN V" + toHex(targetRegister, 1) + ", V" + toHex(sourceRegister, 1);
    }

	/**
	 * Shift the bits in the specified register 1 bit to the left. Bit 7 will be
	 * shifted into register VF.
	 */
	public void leftShift() {
		int sourceRegister = (operand & 0x0F00) >> 8;
		v[0xF] = (short) ((v[sourceRegister] & 0x80) >> 8);
		v[sourceRegister] = (short) (v[sourceRegister] << 1);
		lastOpDesc = "SHL V" + toHex(sourceRegister, 1);
	}

	/**
	 * Skip if source register is equal to target register. The program counter
	 * is updated to skip the next instruction by advancing it by 2 bytes.
	 */
	public void skipIfRegisterNotEqualRegister() {
		int sourceRegister = (operand & 0x0F00) >> 8;
		int targetRegister = (operand & 0x00F0) >> 4;
		if (v[sourceRegister] != v[targetRegister]) {
			pc += 2;
		}
		lastOpDesc = "SKNE V" + toHex(sourceRegister, 1) + ", V" + toHex(targetRegister, 1);
	}

	/**
	 * Load index register with constant value.
	 */
	public void loadIndexWithValue() {
		index = (short) (operand & 0x0FFF);
		lastOpDesc = "LOAD I, " + toHex(index, 3);
	}

	/**
	 * Load the program counter with the memory value located at the specified
	 * operand plus the value of the index register.
	 */
	public void jumpToIndexPlusValue() {
		pc = index + (operand & 0x0FFF);
		lastOpDesc = "JUMP I + " + toHex(operand & 0x0FFF, 3);
	}

	/**
	 * A random number between 0 and 255 is generated. The contents of it are
	 * then ANDed with the constant value passed in the operand. The result is
	 * stored in the target register.
	 */
	public void generateRandomNumber() {
		int value = operand & 0x00FF;
		int targetRegister = (operand & 0x0F00) >> 8;
		v[targetRegister] = (short) (value & random.nextInt(256));
		lastOpDesc = "RAND V" + toHex(targetRegister, 1) + ", " + toHex(value, 2);
	}

	/**
	 * Draws the sprite pointed to in the index register at the specified x and
	 * y coordinates. Drawing is done via an XOR routine, meaning that if the
	 * target pixel is already turned on, and a pixel is set to be turned on at
	 * that same location via the draw, then the pixel is turned off. The
	 * routine will wrap the pixels if they are drawn off the edge of the
	 * mScreen. Each sprite is 8 bits (1 byte) wide. The num_bytes parameter sets
	 * how tall the sprite is. Consecutive bytes in the memory pointed to by the
	 * index register make up the bytes of the sprite. Each bit in the sprite
	 * byte determines whether a pixel is turned on (1) or turned off (0). If
	 * writing a pixel to a location causes that pixel to be turned off, then VF
	 * will be set to 1.
	 */
	public void drawSprite() {
		int xRegister = (operand & 0x0F00) >> 8;
		int yRegister = (operand & 0x00F0) >> 4;
		int xPos = v[xRegister];
		int yPos = v[yRegister];
		v[0xF] = 0;

		for (int yIndex = 0; yIndex < (operand & 0xF); yIndex++) {

			short colorByte = memory.read(index + yIndex);
			int yCoord = yPos + yIndex;
			yCoord = yCoord % mScreen.getHeight();

			int mask = 0x80;

			for (int xIndex = 0; xIndex < 8; xIndex++) {
				int xCoord = xPos + xIndex;
				xCoord = xCoord % mScreen.getWidth();

				boolean turnOn = (colorByte & mask) > 0;
				boolean currentOn = mScreen.pixelOn(xCoord, yCoord);
				
				if (turnOn && currentOn) {
				    v[0xF] |= 1;
				    turnOn = false;
				} else if (!turnOn && currentOn) {
				    turnOn = true;
				}

				mScreen.drawPixel(xCoord, yCoord, turnOn);
				mask = mask >> 1;
			}
		}
		lastOpDesc = "DRAW V" + toHex(xRegister, 1) + ", V" + toHex(yRegister, 1);
	}

	/**
	 * Check to see if the key specified in the source register is pressed, and
	 * if it is, skips the next instruction.
	 */
	void skipIfKeyPressed() {
		int sourceRegister = (operand & 0x0F00) >> 8;
		int keyToCheck = v[sourceRegister];
		if (mKeyboard.getCurrentKey() == keyToCheck) {
			pc += 2;
		}
		lastOpDesc = "SKPR V" + toHex(sourceRegister, 1);
	}

	/**
	 * Check for the specified keypress in the source register and if it is NOT
	 * pressed, will skip the next instruction.
	 */
	void skipIfKeyNotPressed() {
		int sourceRegister = (operand & 0x0F00) >> 8;
		int keyToCheck = v[sourceRegister];
		if (mKeyboard.getCurrentKey() != keyToCheck) {
			pc += 2;
		}
		lastOpDesc = "SKUP V" + toHex(sourceRegister, 1);
	}

	/**
	 * Move the value of the delay timer into the target register.
	 */
	void moveDelayTimerIntoRegister() {
		int targetRegister = (operand & 0x0F00) >> 8;
		v[targetRegister] = delay;
		lastOpDesc = "LOAD V" + toHex(targetRegister, 1) + ", DELAY";
	}

	/**
	 * Stop execution until a key is pressed. Move the value of the key pressed
	 * into the specified register.
	 */
	void waitForKeypress() {
		int targetRegister = (operand & 0x0F00) >> 8;
		int currentKey = mKeyboard.getCurrentKey();
		while (currentKey == 0) {
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			currentKey = mKeyboard.getCurrentKey();
		}
		v[targetRegister] = (short)currentKey;
		lastOpDesc = "KEYD V" + toHex(targetRegister, 1);
	}

	/**
	 * Move the value stored in the specified source register into the delay
	 * timer.
	 */
	void moveRegisterIntoDelayRegister() {
		int sourceRegister = (operand & 0x0F00) >> 8;
		delay = v[sourceRegister];
		lastOpDesc = "LOAD DELAY, V" + toHex(sourceRegister, 1);
	}

	/**
	 * Move the value stored in the specified source register into the sound
	 * timer.
	 */
	void moveRegisterIntoSoundRegister() {
		int sourceRegister = (operand & 0x0F00) >> 8;
		sound = v[sourceRegister];
		lastOpDesc = "LOAD SOUND, V" + toHex(sourceRegister, 1);
	}

	/**
	 * Load the index with the sprite indicated in the source register. All
	 * sprites are 5 bytes long, so the location of the specified sprite is its
	 * index multiplied by 5.
	 */
	void loadIndexWithSprite() {
		int sourceRegister = (operand & 0x0F00) >> 8;
		index = v[sourceRegister] * 5;
		lastOpDesc = "LOAD I, V" + toHex(sourceRegister, 1);
	}

	/**
	 * Add the value of the register into the index register value.
	 */
	void addRegisterIntoIndex() {
		int sourceRegister = (operand & 0x0F00) >> 8;
		index += v[sourceRegister];
		lastOpDesc = "ADD I, V" + toHex(sourceRegister, 1);
	}

	/**
	 * Take the value stored in source and place the digits in the following
	 * locations:
	 * 
	 * hundreds -> self.memory[index] tens -> self.memory[index + 1] ones ->
	 * self.memory[index + 2]
	 * 
	 * For example, if the value is 123, then the following values will be
	 * placed at the specified locations:
	 * 
	 * 1 -> self.memory[index] 2 -> self.memory[index + 1] 3 ->
	 * self.memory[index + 2]
	 */
	void storeBCDInMemory() {
		int sourceRegister = (operand & 0x0F00) >> 8;
		int bcdValue = v[sourceRegister];
		memory.write(bcdValue / 100, index);
		memory.write((bcdValue % 100) / 10, index + 1);
		memory.write((bcdValue % 100) % 10, index + 2);
		lastOpDesc = "BCD V" + toHex(sourceRegister, 1) + " (" + bcdValue + ")";
	}

	/**
	 * Store all of the V registers in the memory pointed to by the index
	 * register. The source register contains the number of V registers to
	 * store. For example, to store all of the V registers, the source register
	 * would contain the value 0xF.
	 */
	void storeRegistersInMemory() {
		int numRegisters = (operand & 0x0F00) >> 8;
		for (int counter = 0; counter <= numRegisters; counter++) {
			memory.write(v[counter], index + counter);
		}
		lastOpDesc = "STOR " + toHex(numRegisters, 1);
	}

	/**
	 * Read all of the V registers from the memory pointed to by the index
	 * register. The source register contains the number of V registers to load.
	 * For example, to load all of the V registers, the source register would
	 * contain the value 0xF.
	 */
	void readRegistersFromMemory() {
		int numRegisters = (operand & 0x0F00) >> 8;
		for (int counter = 0; counter <= numRegisters; counter++) {
			v[counter] = memory.read(index + counter);
		}
		lastOpDesc = "READ " + toHex(numRegisters, 1);
	}

	/**
	 * Reset the CPU by blanking out all registers, and resetting the stack
	 * pointer and program counter to their starting values.
	 */
	public void reset() {
		v = new short[NUM_REGISTERS];
		pc = PROGRAM_COUNTER_START;
		stack = STACK_POINTER_START;
		index = 0;
		delay = 0;
		sound = 0;
        if (mScreen != null) {
            mScreen.clearScreen();
        }
	}
	
	/**
	 * Decrement the delay timer and the sound timer if they are not zero.
	 */
	public void decrementTimers() {
	    if (delay != 0) {
	        delay--;
	    }
	    if (sound != 0) {
	        sound--;
	    }
	}
	
	/**
	 * Return the string of the last operation that occurred.
	 * 
	 * @return A string containing the last operation
	 */
	public String getOpShortDesc() {
		return lastOpDesc;
	}
	
	/**
	 * Return a String representation of the operand.
	 * 
	 * @return A string containing the operand
	 */
	public String getOp() {
		return toHex(operand, 4);
	}
	
	/**
	 * Convers a number into a hex string containing the number of digits 
	 * specified.
	 * 
	 * @param number The number to convert to hex
	 * @param numDigits The number of digits to include
	 * @return The String representation of the hex value
	 */
	public static String toHex(int number, int numDigits) {
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
     * Returns true if the CPU is in trace mode, false otherwise.
     *
     * @return whether the CPU is in trace mode
     */
    public boolean getTrace() {
        return mTrace;
    }
	
	/**
	 * Sets whether or not the CPU should be set to step mode. Will turn on the
	 * mScreen overlay, and activate step if set to true. If set to false, will
	 * turn off the mScreen overlay, and will put the CPU back into normal
	 * execution mode.
	 * 
	 * @param step Whether to turn step on (true) or off (false)
	 */
    public void setStep(boolean step) {
        mStep = step;
    }

    /**
     * Returns true if the CPU is in step mode, false otherwise.
     *
     * @return true if the CPU is in step mode, false otherwise
     */
    public boolean getStep() {
        return mStep;
    }

    /**
     * Sets whether the CPU execution should be paused.
     *
     * @param paused true if the CPU should be paused
     */
    public void setPaused(boolean paused) {
        mPaused = paused;
    }

    /**
     * Returns whether the CPU is paused.
     *
     * @return true if the CPU is paused, false otherwise
     */
    public boolean getPaused() {
        return mPaused;
    }
    
    /**
     * Will check to see if a debugging key was pressed. Will return true if 
     * one was pressed. Will also set the correct trace and step flags
     * depending on what debug key was pressed.
     * 
     * @return True if a debug key was pressed, false otherwise
     */
    public boolean interpretDebugKey() {
        int key = mKeyboard.getDebugKey();
        
        if (key == Keyboard.CHIP8_NORMAL) {
            //setTrace(false);
            return true;
        }
        
        if (key == Keyboard.CHIP8_STEP) {
            setStep(!mStep);
            return true;
        }
        
        if (key == Keyboard.CHIP8_TRACE) {
            //setTrace(!mTrace);
            return true;
        }
        
        if (key == Keyboard.CHIP8_NEXT) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Continually runs the main CPU code over and over in a loop until the
     * interpreter is interrupted.
     * 
     * @throws InterruptedException
     */
    public void run() {
        while (true) {
            if (!mPaused) {
                fetchIncrementExecute();
            } else {
                try {
                    sleep(300);
                } catch (InterruptedException e ) {

                }
            }
            
            if (mStep) {
                while (!interpretDebugKey()) {
                    try {
                        sleep(300);
                    } catch (InterruptedException e ) {

                    }
                    if (!mStep) {
                        break;
                    }
                }
            }
            
            interpretDebugKey();
        }
    }
}
