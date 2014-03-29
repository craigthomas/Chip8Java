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
	
	@Test
	public void testRightShift() {
	    for (int register = 0; register < 0xF; register++) {
	        for (int value = 0; value < 0xFF; value++) {
	            mCPU.v[register] = (short)value;
	            mCPU.operand = register << 8;
	            for (int index = 1; index < 8; index++) {
	                int shiftedValue = value >> index;
	                mCPU.v[0xF] = 0;
	                int bitZero = mCPU.v[register] & 1;
	                mCPU.rightShift();
	                assertEquals(shiftedValue, mCPU.v[register]);
	                assertEquals(bitZero, mCPU.v[0xF]);
	            }
	        }
	    }
	}
	
	@Test
	public void testSubtractRegisterFromRegister1() {
	    for (int source = 0; source < 0xF; source++) {
	        for (int target = 0; target < 0xF; target++) {
	            if (source != target) {
	                for (int sourceValue = 0; sourceValue < 0xFF; sourceValue += 10) {
	                    for (int targetValue = 0; targetValue < 0xF; targetValue++) {
	                        mCPU.v[source] = (short) sourceValue;
	                        mCPU.v[target] = (short) targetValue;
	                        mCPU.operand = source << 8;
	                        mCPU.operand += (target << 4);
	                        mCPU.subtractRegisterFromRegister1();
	                        if (targetValue > sourceValue) {
	                            assertEquals(targetValue - sourceValue, mCPU.v[source]);
	                            assertEquals(1, mCPU.v[0xF]);
	                        } else {
                                assertEquals(256 + targetValue - sourceValue, mCPU.v[source]);
                                assertEquals(0, mCPU.v[0xF]);	                            
	                        }
	                    }
	                }
	            }
	        }
	    }
	}
	
	@Test
	public void testLeftShift() {
	    for (int register = 0; register < 0xF; register++) {
	        for (int value = 0; value < 256; value++) {
	            mCPU.v[register] = (short) value;
	            mCPU.operand = register << 8;
	            int shiftedValue = value;
	            for (int index = 1; index < 8; index++) {
	                shiftedValue = value << index;
	                int bitSeven = (shiftedValue & 0x100) >> 9;
	                shiftedValue = shiftedValue & 0xFFFF;
	                mCPU.v[0xF] = 0;
	                mCPU.leftShift();
	                assertEquals(shiftedValue, mCPU.v[register]);
	                assertEquals(bitSeven, mCPU.v[0xF]);
	            }
	        }
	    }
	}
	
	@Test
	public void testLoadIndexWithValue() {
	    for (int value = 0; value < 0x10000; value++) {
	        mCPU.operand = value;
	        mCPU.loadIndexWithValue();
	        assertEquals(value & 0x0FFF, mCPU.index);
	    }
	}
	
	@Test
	public void testGenerateRandomNumber() {
	    for (int register = 0; register < 0xF; register++) {
	        for (int value = 0; value < 0xFF; value += 10) {
	            mCPU.v[register] = -1;
	            mCPU.operand = register << 8;
	            mCPU.operand += value;
	            mCPU.generateRandomNumber();
	            assertTrue(mCPU.v[register] >= 0);
                assertTrue(mCPU.v[register] <= 255);
	        }
	    }
	}
	
	@Test
	public void testMoveDelayTimerIntoRegister() {
        for (int register = 0; register < 0xF; register++) {
            for (int value = 0; value < 0xFF; value += 10) {
                mCPU.delay = (short) value;
                mCPU.operand = register << 8;
                mCPU.v[register] = 0;
                mCPU.moveDelayTimerIntoRegister();
                assertEquals(value, mCPU.v[register]);
            }
        }
	}

	@Test
	public void testMoveRegisterIntoDelayRegister() {
        for (int register = 0; register < 0xF; register++) {
            for (int value = 0; value < 0xFF; value += 10) {
                mCPU.v[register] = (short) value;
                mCPU.operand = register << 8;
                mCPU.delay = 0;
                mCPU.moveRegisterIntoDelayRegister();
                assertEquals(value, mCPU.delay);
            }
        }
	}
	
	@Test
	public void testMoveRegisterIntoSoundRegister() {
        for (int register = 0; register < 0xF; register++) {
            for (int value = 0; value < 0xFF; value += 10) {
                mCPU.v[register] = (short) value;
                mCPU.operand = register << 8;
                mCPU.sound = 0;
                mCPU.moveRegisterIntoSoundRegister();
                assertEquals(value, mCPU.sound);
            }
        }
	}
	
	@Test
	public void testLoadIndexWithSprite() {
	    for (int number = 0; number < 0x10; number++) {
	        mCPU.index = 0xFFF;
	        mCPU.v[0] = (short) number;
	        mCPU.operand = 0xF029;
	        mCPU.loadIndexWithSprite();
	        assertEquals(number * 5, mCPU.index);
	    }
	}
	
	@Test
	public void testStoreBCDInMemory() {
	    for (int number = 0; number < 0x100; number++) {
	        String bcdValue = String.valueOf(number);
	        if (number < 100) {
	            bcdValue = "0" + bcdValue;
	        }
            if (number < 10) {
                bcdValue = "0" + bcdValue;
            }
	        mCPU.index = 0;
	        mCPU.v[0] = (short) number;
	        mCPU.operand = 0xF033;
	        mCPU.storeBCDInMemory();
	        assertEquals(bcdValue.charAt(0), String.valueOf(mMemory.read(0)).charAt(0));
            assertEquals(bcdValue.charAt(1), String.valueOf(mMemory.read(1)).charAt(0));
            assertEquals(bcdValue.charAt(2), String.valueOf(mMemory.read(2)).charAt(0));
	    }
	}
	
	@Test
	public void testReadRegistersFromMemory() {
	    int index = 0x500;
	    mCPU.index = index;
	    
	    for (int register = 0; register < 0xF; register++) {
	        mMemory.write(register + 0x89, index + register);
	    }
	    
	    for (int register = 0; register < 0xF; register++) {
	        for (int registerToSet = 0; registerToSet < 0xF; registerToSet++) {
	            mCPU.v[registerToSet] = 0;
	        }
	        
	        mCPU.operand = 0xF000;
	        mCPU.operand += (register << 8);
	        mCPU.operand += 0x65;
	        mCPU.readRegistersFromMemory();
	        for (int registerToCheck = 0; registerToCheck < 0xF; registerToCheck++) {
	            if (registerToCheck >= register) {
	                assertEquals(0, mCPU.v[registerToCheck]);
	            } else {
	                assertEquals(registerToCheck + 0x89, mCPU.v[registerToCheck]);
	            }
	        }
	    }
	}
}
