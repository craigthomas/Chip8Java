/*
 * Copyright (C) 2013 Craig Thomas
 * This project uses an MIT style license - see LICENSE for details.
 */
package com.chip8java.emulator;

import java.util.Random;

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
public class CentralProcessingUnit {

	// The total number of registers in the Chip 8 CPU
	private static final int NUM_REGISTERS = 0xF;
	// The start location of the program counter
	private static final int PROGRAM_COUNTER_START = 0x200;
	// The start location of the stack pointer
	private static final int STACK_POINTER_START = 0x52;
	// The internal 8-bit registers
	private short[] v;
	// The index register
	private int index;
	// The stack pointer register
	private int stack;
	// The program counter
	private int pc;
	// The delay register
	private short delay;
	// The sound register
	private short sound;
	// The current operand
	private int operand;
	// The internal memory for the Chip 8
	private Memory memory;
	// The screen object for the Chip 8
	private Screen screen;
	// A Random number generator used for the class
	Random random;

	public CentralProcessingUnit(Memory memory) {
		this.random = new Random();
		this.memory = memory;
		reset();
	}

	public void fetchIncrementExecute() {
		operand = memory.read(pc);
		operand = operand << 8;
		operand += memory.read(pc + 1);
		pc += 2;
		int opcode = (operand & 0xF000) >> 12;
		executeInstruction(opcode);
	}

	public void executeInstruction(int opcode) {
		switch (opcode) {
		case 0x0:
			int operation = operand & 0x00FF;
			if (operation == 0x00E0) {
				screen.clearScreen();
			}
			if (operation == 0x00EE) {
				returnFromSubroutine();
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

			case 0xE:
				leftShift();
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
	}

	/**
	 * Jump to address.
	 */
	public void jumpToAddress() {
		pc = operand & 0x0FFF;
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
	}

	/**
	 * Move the constant value into the specified register.
	 */
	public void moveValueToRegister() {
		int targetRegister = (operand & 0x0F00) >> 8;
		v[targetRegister] = (short) (operand & 0x00FF);
	}

	/**
	 * Add the constant value to the specified register.
	 */
	public void addValueToRegister() {
		int targetRegister = (operand & 0x0F00) >> 8;
		int temp = v[targetRegister] + (operand & 0x00FF);
		v[targetRegister] = (temp < 256) ? (short) temp : (short) (temp - 256);
	}

	/**
	 * Move the value of the source register into the value of the target
	 * register.
	 */
	public void moveRegisterIntoRegister() {
		int targetRegister = (operand & 0x0F00) >> 8;
		int sourceRegister = (operand & 0x00F0) >> 4;
		v[targetRegister] = v[sourceRegister];
	}

	/**
	 * Perform a logical OR operation between the source and the target
	 * register, and store the result in the target register.
	 */
	public void logicalOr() {
		int targetRegister = (operand & 0x0F00) >> 8;
		int sourceRegister = (operand & 0x00F0) >> 4;
		v[targetRegister] |= v[sourceRegister];
	}

	/**
	 * Perform a logical AND operation between the source and the target
	 * register, and store the result in the target register.
	 */
	public void logicalAnd() {
		int targetRegister = (operand & 0x0F00) >> 8;
		int sourceRegister = (operand & 0x00F0) >> 4;
		v[targetRegister] &= v[sourceRegister];
	}

	/**
	 * Perform a logical XOR operation between the source and the target
	 * register, and store the result in the target register.
	 */
	public void exclusiveOr() {
		int targetRegister = (operand & 0x0F00) >> 8;
		int sourceRegister = (operand & 0x00F0) >> 4;
		v[targetRegister] ^= v[sourceRegister];
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
	}

	/**
	 * Shift the bits in the specified register 1 bit to the right. Bit 0 will
	 * be shifted into register VF.
	 */
	public void rightShift() {
		int sourceRegister = (operand & 0x0F00) >> 8;
		v[0xF] = (short) (v[sourceRegister] & 0x1);
		v[sourceRegister] = (short) (v[sourceRegister] >> 1);
	}

	/**
	 * Shift the bits in the specified register 1 bit to the left. Bit 7 will be
	 * shifted into register VF.
	 */
	public void leftShift() {
		int sourceRegister = (operand & 0x0F00) >> 8;
		v[0xF] = (short) ((v[sourceRegister] & 0x80) >> 8);
		v[sourceRegister] = (short) (v[sourceRegister] << 1);
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
	}

	/**
	 * Load index register with constant value.
	 */
	public void loadIndexWithValue() {
		index = (short) (operand & 0x0FFF);
	}

	/**
	 * Load the program counter with the memory value located at the specified
	 * operand plus the value of the index register.
	 */
	public void jumpToIndexPlusValue() {
		pc = index + (operand & 0x0FFF);
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
	}

	/**
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
	public void drawSprite() {
		int xRegister = (operand & 0x0F00) >> 8;
		int yRegister = (operand & 0x00F0) >> 4;
		int xPos = v[xRegister];
		int yPos = v[yRegister];
		v[0xF] = 0;

		for (int yIndex = 0; yIndex < (operand & 0xF); yIndex++) {
			
			short colorByte = memory.read(index + yIndex);
			int yCoord = yPos + yIndex;
			yCoord = yCoord % screen.getHeight();

			int mask = 1;

			for (int xIndex = 0; xIndex < 8; xIndex++) {
				int xCoord = xPos + xIndex;
				xCoord = xCoord % screen.getWidth();

				boolean turnOn = (colorByte & mask) > 0;

				if (screen.drawPixel(xCoord, yCoord, turnOn)) {
					v[0xF] |= 1;
				}
				mask = mask << 1;
			}
		}
		screen.updateScreen();
	}

	/**
	 * Reset the CPU by blanking out all registers, and reseting the stack
	 * pointer and program counter to their starting values.
	 */
	public void reset() {
		v = new short[NUM_REGISTERS];
		pc = PROGRAM_COUNTER_START;
		stack = STACK_POINTER_START;
		index = 0;
		delay = 0;
		sound = 0;
	}
}
