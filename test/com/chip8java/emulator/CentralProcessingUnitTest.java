package com.chip8java.emulator;

import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

import junit.framework.TestCase;

public class CentralProcessingUnitTest extends TestCase {

	private Screen mScreenMock;
	private Keyboard mKeyboardMock;
	private Memory mMemory;
	private CentralProcessingUnit mCPU;
	
	@Before
	public void setUp() {
		mMemory = new Memory(Memory.MEMORY_4K);
		mScreenMock = mock(Screen.class);
		mKeyboardMock = mock(Keyboard.class);
		mCPU = new CentralProcessingUnit(mMemory, mKeyboardMock, mScreenMock);
	}
	
	@Test
	public void testReturnFromSubroutine() {
		for (int address = 0x200; address < 0xFFFF; address += 0x10) {
			mMemory.write(address & 0x00FF, mCPU.stack);
			mMemory.write((address & 0xFF00) >> 8, mCPU.stack + 1);
			mCPU.stack += 2;
			mCPU.pc = 0;
			mCPU.returnFromSubroutine();
			assertEquals(address, mCPU.pc);
		}
	}
	
	@Test
	public void testJumpToAddress() {
		for (int address = 0x0; address < 0xFFFF; address += 0x10) {
			mCPU.operand = address;
			mCPU.pc = 0;
			assertEquals(0, mCPU.pc);
			mCPU.jumpToAddress();
			assertEquals(address & 0x0FFF, mCPU.pc);
		}
	}
	
	@Test
	public void testJumpToSubroutine() {
		for (int address = 0x200; address < 0xFFFF; address += 0x10) {
			mCPU.operand = address;
			mCPU.stack = 0;
			mCPU.pc = 0x100;
			mCPU.jumpToSubroutine();
			assertEquals(address & 0x0FFF, mCPU.pc);
			assertEquals(2, mCPU.stack);
			assertEquals(0, mMemory.read(0));
			assertEquals(1, mMemory.read(1));
		}
	}
	
	@Test
	public void testSkipIfRegisterEqualValue() {
		for (int register = 0; register < 0x10; register++) {
			for (int value = 0; value < 0xFF; value += 0x10) {
				for (int regValue = 0; regValue < 0xFF; regValue++) {
					mCPU.operand = register << 8;
					mCPU.operand += value;
					mCPU.v[register] = (short)regValue;
					mCPU.pc = 0;
					mCPU.skipIfRegisterEqualValue();
					if (value == regValue) {
						assertEquals(2, mCPU.pc);
					} else {
						assertEquals(0, mCPU.pc);
					}
				}
			}
		}
	}
	
	@Test
	public void testSkipIfRegisterNotEqualValue() {
		for (int register = 0; register < 0x10; register++) {
			for (int value = 0; value < 0xFF; value += 0x10) {
				for (int regValue = 0; regValue < 0xFF; regValue++) {
					mCPU.operand = register << 8;
					mCPU.operand += value;
					mCPU.v[register] = (short)regValue;
					mCPU.pc = 0;
					mCPU.skipIfRegisterNotEqualValue();;
					if (value != regValue) {
						assertEquals(2, mCPU.pc);
					} else {
						assertEquals(0, mCPU.pc);
					}
				}
			}
		}		
	}
	
	@Test
	public void testSkipIfRegisterEqualRegister() {
		for (int register = 0; register < 0x10; register++) { 
			mCPU.v[register] = (short)register;
		}
		
		for (int register1 = 0; register1 < 0x10; register1++) {
			for (int register2 = 0; register2 < 0x10; register2++) {
				mCPU.operand = register1;
				mCPU.operand <<= 4;
				mCPU.operand += register2;
				mCPU.operand <<= 4;
				mCPU.pc = 0;
				mCPU.skipIfRegisterEqualRegister();
				if (register1 == register2) {
					assertEquals(2, mCPU.pc);
				} else {
					assertEquals(0, mCPU.pc);
				}
			}
		}
	}
	
	@Test
	public void testSkipIfRegisterNotEqualRegister() {
		for (int register = 0; register < 0x10; register++) { 
			mCPU.v[register] = (short)register;
		}
		
		for (int register1 = 0; register1 < 0x10; register1++) {
			for (int register2 = 0; register2 < 0x10; register2++) {
				mCPU.operand = register1;
				mCPU.operand <<= 4;
				mCPU.operand += register2;
				mCPU.operand <<= 4;
				mCPU.pc = 0;
				mCPU.skipIfRegisterNotEqualRegister();
				if (register1 != register2) {
					assertEquals(2, mCPU.pc);
				} else {
					assertEquals(0, mCPU.pc);
				}
			}
		}
	}
	
	@Test
	public void testMoveValueToRegister() {
		int value = 0x23;
		
		for (int register = 0; register < 0x10; register++) {
			mCPU.operand = 0x60 + register;
			mCPU.operand <<= 8;
			mCPU.operand += value;
			mCPU.moveValueToRegister();
			for (int registerToCheck = 0; registerToCheck < 0x10; registerToCheck++) {
				if (registerToCheck != register) {
					assertEquals(0, mCPU.v[registerToCheck]); 
				} else {
					assertEquals(value, mCPU.v[registerToCheck]);
				}
			}
			mCPU.v[register] = 0;
		}
	}
	
	@Test
	public void testAddValueToRegister() {
		for (int register = 0; register <0x10; register++) {
			for (int registerValue = 0; registerValue < 0xFF; registerValue += 0x10) {
				for (int value = 0; value < 0xFF; value++) {
					mCPU.v[register] = (short)registerValue;
					mCPU.operand = register << 8;
					mCPU.operand += value;
					mCPU.addValueToRegister();
					if (value + registerValue < 256) {
						assertEquals(value + registerValue, mCPU.v[register]);
					} else {
						assertEquals(value + registerValue - 256, mCPU.v[register]);
					}
				}
			}
		}
	}
	
	@Test
	public void testMoveRegisterIntoRegister() {
		for (int source = 0; source < 0x10; source++) {
			for (int target = 0; target < 0x10; target++) {
				if (source != target) {
					mCPU.v[target] = 0x32;
					mCPU.v[source] = 0;
					mCPU.operand = source << 8;
					mCPU.operand += (target << 4);
					mCPU.moveRegisterIntoRegister();
					assertEquals(0x32, mCPU.v[source]);
				}
			}
		}
	}
	
	@Test
	public void testLogicalOr() {
		for (int source = 0; source < 0x10; source++) {
			for (int target = 0; target < 0x10; target++) {
				if (source != target) {
					for (int sourceVal = 0; sourceVal < 0xFF; sourceVal += 0x10) {
						for (int targetVal = 0; targetVal < 0xFF; targetVal += 0x10) {
							mCPU.v[source] = (short)sourceVal;
							mCPU.v[target] = (short)targetVal;
							mCPU.operand = source << 8;
							mCPU.operand += (target << 4);
							mCPU.logicalOr();
							assertEquals(sourceVal | targetVal, mCPU.v[source]);
						}
					}
				}
			}
		}
	}
	
	@Test
	public void testLogicalAnd() {
		for (int source = 0; source < 0x10; source++) {
			for (int target = 0; target < 0x10; target++) {
				if (source != target) {
					for (int sourceVal = 0; sourceVal < 0xFF; sourceVal += 0x10) {
						for (int targetVal = 0; targetVal < 0xFF; targetVal += 0x10) {
							mCPU.v[source] = (short)sourceVal;
							mCPU.v[target] = (short)targetVal;
							mCPU.operand = source << 8;
							mCPU.operand += (target << 4);
							mCPU.logicalAnd();
							assertEquals(sourceVal & targetVal, mCPU.v[source]);
						}
					}
				}
			}
		}		
	}
	
	@Test
	public void testExclusiveOr() {
		for (int source = 0; source < 0x10; source++) {
			for (int target = 0; target < 0x10; target++) {
				if (source != target) {
					for (int sourceVal = 0; sourceVal < 0xFF; sourceVal += 0x10) {
						for (int targetVal = 0; targetVal < 0xFF; targetVal += 0x10) {
							mCPU.v[source] = (short)sourceVal;
							mCPU.v[target] = (short)targetVal;
							mCPU.operand = source << 8;
							mCPU.operand += (target << 4);
							mCPU.exclusiveOr();
							assertEquals(sourceVal ^ targetVal, mCPU.v[source]);
						}
					}
				}
			}
		}				
	}
	
	@Test
	public void testAddToRegister() {
		for (int source = 0; source < 0xF; source++) {
			for (int target = 0; target < 0xF; target++) {
				if (source != target) {
					for (int sourceVal = 0; sourceVal < 0xFF; sourceVal += 0x10) {
						for (int targetVal = 0; targetVal < 0xFF; targetVal += 0x10) {
							mCPU.v[source] = (short)sourceVal;
							mCPU.v[target] = (short)targetVal;
							mCPU.operand = source << 8;
							mCPU.operand += (target << 4);
							mCPU.addRegisterToRegister();
							if ((sourceVal + targetVal) > 255) {
								assertEquals(sourceVal + targetVal - 256, mCPU.v[source]);
								assertEquals(1, mCPU.v[0xF]);
							} else {
								assertEquals(sourceVal + targetVal, mCPU.v[source]);
								assertEquals(0, mCPU.v[0xF]);
							}
						}
					}
				}
			}
		}		
	}

	@Test
	public void testSubtractRegisterFromRegister() {
		for (int source = 0; source < 0xF; source++) {
			for (int target = 0; target < 0xF; target++) {
				if (source != target) {
					for (int sourceVal = 0; sourceVal < 0xFF; sourceVal += 0x10) {
						for (int targetVal = 0; targetVal < 0xFF; targetVal += 0x10) {
							mCPU.v[source] = (short)sourceVal;
							mCPU.v[target] = (short)targetVal;
							mCPU.operand = source << 8;
							mCPU.operand += (target << 4);
							mCPU.subtractRegisterFromRegister();
							if (sourceVal > targetVal) {
								assertEquals(sourceVal - targetVal, mCPU.v[source]);
								assertEquals(1, mCPU.v[0xF]);
							} else {
								assertEquals(sourceVal - targetVal + 256, mCPU.v[source]);
								assertEquals(0, mCPU.v[0xF]);
							}
						}
					}
				}
			}
		}		
	}

}
