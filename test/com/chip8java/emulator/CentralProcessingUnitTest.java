package com.chip8java.emulator;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import java.awt.FontFormatException;
import java.io.FileNotFoundException;
import java.io.IOException;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class CentralProcessingUnitTest extends TestCase {

    private Screen mScreenMock;
    private Keyboard mKeyboardMock;
    private Memory mMemory;
    private CentralProcessingUnit mCPU;
    private CentralProcessingUnit mCPUSpy;

    @Before
    public void setUp() {
        mMemory = new Memory(Memory.MEMORY_4K);
        mScreenMock = mock(Screen.class);
        mKeyboardMock = mock(Keyboard.class);
        Mockito.when(mKeyboardMock.getCurrentKey()).thenReturn(9);
        mCPU = new CentralProcessingUnit(mMemory, mKeyboardMock, mScreenMock);
        mCPUSpy = spy(mCPU);
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
                    mCPU.v[register] = (short) regValue;
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
                    mCPU.v[register] = (short) regValue;
                    mCPU.pc = 0;
                    mCPU.skipIfRegisterNotEqualValue();
                    ;
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
            mCPU.v[register] = (short) register;
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
            mCPU.v[register] = (short) register;
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
        for (int register = 0; register < 0x10; register++) {
            for (int registerValue = 0; registerValue < 0xFF; registerValue += 0x10) {
                for (int value = 0; value < 0xFF; value++) {
                    mCPU.v[register] = (short) registerValue;
                    mCPU.operand = register << 8;
                    mCPU.operand += value;
                    mCPU.addValueToRegister();
                    if (value + registerValue < 256) {
                        assertEquals(value + registerValue, mCPU.v[register]);
                    } else {
                        assertEquals(value + registerValue - 256,
                                mCPU.v[register]);
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
                            mCPU.v[source] = (short) sourceVal;
                            mCPU.v[target] = (short) targetVal;
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
                            mCPU.v[source] = (short) sourceVal;
                            mCPU.v[target] = (short) targetVal;
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
                            mCPU.v[source] = (short) sourceVal;
                            mCPU.v[target] = (short) targetVal;
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
                            mCPU.v[source] = (short) sourceVal;
                            mCPU.v[target] = (short) targetVal;
                            mCPU.operand = source << 8;
                            mCPU.operand += (target << 4);
                            mCPU.addRegisterToRegister();
                            if ((sourceVal + targetVal) > 255) {
                                assertEquals(sourceVal + targetVal - 256,
                                        mCPU.v[source]);
                                assertEquals(1, mCPU.v[0xF]);
                            } else {
                                assertEquals(sourceVal + targetVal,
                                        mCPU.v[source]);
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
                            mCPU.v[source] = (short) sourceVal;
                            mCPU.v[target] = (short) targetVal;
                            mCPU.operand = source << 8;
                            mCPU.operand += (target << 4);
                            mCPU.subtractRegisterFromRegister();
                            if (sourceVal > targetVal) {
                                assertEquals(sourceVal - targetVal,
                                        mCPU.v[source]);
                                assertEquals(1, mCPU.v[0xF]);
                            } else {
                                assertEquals(sourceVal - targetVal + 256,
                                        mCPU.v[source]);
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
                mCPU.v[register] = (short) value;
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
                                assertEquals(targetValue - sourceValue,
                                        mCPU.v[source]);
                                assertEquals(1, mCPU.v[0xF]);
                            } else {
                                assertEquals(256 + targetValue - sourceValue,
                                        mCPU.v[source]);
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
            assertEquals(bcdValue.charAt(0), String.valueOf(mMemory.read(0))
                    .charAt(0));
            assertEquals(bcdValue.charAt(1), String.valueOf(mMemory.read(1))
                    .charAt(0));
            assertEquals(bcdValue.charAt(2), String.valueOf(mMemory.read(2))
                    .charAt(0));
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
                    assertEquals(registerToCheck + 0x89,
                            mCPU.v[registerToCheck]);
                }
            }
        }
    }

    @Test
    public void testSkipIfKeyPressedSkipsCorrectly() {
        for (int register = 0; register < 0xF; register++) {
            mCPU.v[register] = 9;
            mCPU.operand = register << 8;
            mCPU.pc = 0;
            mCPU.skipIfKeyPressed();
            assertEquals(2, mCPU.pc);
        }
    }

    @Test
    public void testSkipIfKeyPressedDoesNotSkipIfNotPressed() {
        for (int register = 0; register < 0xF; register++) {
            mCPU.v[register] = 8;
            mCPU.operand = register << 8;
            mCPU.pc = 0;
            mCPU.skipIfKeyPressed();
            assertEquals(0, mCPU.pc);
        }
    }

    @Test
    public void testSkipIfKeyNotPressedSkipsCorrectly() {
        for (int register = 0; register < 0xF; register++) {
            mCPU.v[register] = 8;
            mCPU.operand = register << 8;
            mCPU.pc = 0;
            mCPU.skipIfKeyNotPressed();
            assertEquals(2, mCPU.pc);
        }
    }

    @Test
    public void testSkipIfKeyNotPressedDoesNotSkipIfPressed() {
        for (int register = 0; register < 0xF; register++) {
            mCPU.v[register] = 9;
            mCPU.operand = register << 8;
            mCPU.pc = 0;
            mCPU.skipIfKeyNotPressed();
            assertEquals(0, mCPU.pc);
        }
    }

    @Test
    public void testJumpToIndexPlusValue() {
        for (int index = 0; index < 0xFFF; index += 10) {
            for (int value = 0; value < 0xFFF; value += 10) {
                mCPU.index = index;
                mCPU.pc = 0;
                mCPU.operand = (short) value;
                mCPU.jumpToIndexPlusValue();
                assertEquals(index + value, mCPU.pc);
            }
        }
    }

    @Test
    public void testAddRegisterToIndex() {
        for (int register = 0; register < 0xF; register++) {
            for (int index = 0; index < 0xFFF; index += 10) {
                mCPU.index = index;
                mCPU.v[register] = 0x89;
                mCPU.operand = register << 8;
                mCPU.addRegisterIntoIndex();
                assertEquals(index + 0x89, mCPU.index);
            }
        }
    }

    @Test
    public void testStoreRegistersInMemory() {
        for (int register = 0; register < 0xF; register++) {
            mCPU.v[register] = (short) register;
            mCPU.operand = register << 8;
            mCPU.storeRegistersInMemory();
            mCPU.index = 0;
            for (int counter = 0; counter < register; counter++) {
                assertEquals(counter, mMemory.read(counter));
            }
        }
    }

    @Test
    public void testGetOpShortDescReturnsADescription() {
        mCPU.returnFromSubroutine();
        assertEquals("RTS", mCPU.getOpShortDesc());
    }

    @Test
    public void testGetOpReturnsHexValueOfOp() {
        mCPU.operand = 0xABCD;
        assertEquals("ABCD", mCPU.getOp());
    }

    @Test
    public void testToHex() {
        assertEquals("ABCD", CentralProcessingUnit.toHex(43981, 4));
    }

    @Test
    public void testJumpToAddressInvoked() {
        mCPUSpy.executeInstruction(0x1);
        verify(mCPUSpy).jumpToAddress();
    }

    @Test
    public void testJumpToSubroutineInvoked() {
        mCPUSpy.executeInstruction(0x2);
        verify(mCPUSpy).jumpToSubroutine();
    }

    @Test
    public void testSkipIfRegisterEqualValueInvoked() {
        mCPUSpy.executeInstruction(0x3);
        verify(mCPUSpy).skipIfRegisterEqualValue();
    }

    @Test
    public void testSkipIfRegisterNotEqualValueInvoked() {
        mCPUSpy.executeInstruction(0x4);
        verify(mCPUSpy).skipIfRegisterNotEqualValue();
    }

    @Test
    public void testSkipIfRegisterEqualRegisterInvoked() {
        mCPUSpy.executeInstruction(0x5);
        verify(mCPUSpy).skipIfRegisterEqualRegister();
    }

    @Test
    public void testMoveValueToRegisterInvoked() {
        mCPUSpy.executeInstruction(0x6);
        verify(mCPUSpy).moveValueToRegister();
    }

    @Test
    public void testAddValueToRegisterInvoked() {
        mCPUSpy.executeInstruction(0x7);
        verify(mCPUSpy).addValueToRegister();
    }

    @Test
    public void testSkipIfRegisterNotEqualRegisterInvoked() {
        mCPUSpy.executeInstruction(0x9);
        verify(mCPUSpy).skipIfRegisterNotEqualRegister();
    }

    @Test
    public void testLoadIndexWithValueInvoked() {
        mCPUSpy.executeInstruction(0xA);
        verify(mCPUSpy).loadIndexWithValue();
    }

    @Test
    public void testJumpToIndexPlusValueInvoked() {
        mCPUSpy.executeInstruction(0xB);
        verify(mCPUSpy).jumpToIndexPlusValue();
    }

    @Test
    public void testGenerateRandomNumberInvoked() {
        mCPUSpy.executeInstruction(0xC);
        verify(mCPUSpy).generateRandomNumber();
    }

    @Test
    public void testDrawSpriteInvoked() {
        mCPUSpy.executeInstruction(0xD);
        verify(mCPUSpy).drawSprite();
    }
    
    @Test
    public void testReturnFromSubroutineInvoked() {
        mCPUSpy.operand = 0xEE;
        mCPUSpy.executeInstruction(0x0);
        verify(mCPUSpy).returnFromSubroutine();
    }
    
    @Test
    public void testMoveRegisterIntoRegisterInvoked() {
        mCPUSpy.operand = 0x0;
        mCPUSpy.executeInstruction(0x8);
        verify(mCPUSpy).moveRegisterIntoRegister();
    }
    
    @Test
    public void testLogicalOrInvoked() {
        mCPUSpy.operand = 0x1;
        mCPUSpy.executeInstruction(0x8);
        verify(mCPUSpy).logicalOr();
    }
    
    @Test
    public void testLogicalAndInvoked() {
        mCPUSpy.operand = 0x2;
        mCPUSpy.executeInstruction(0x8);
        verify(mCPUSpy).logicalAnd();
    }
    
    @Test
    public void testExclusiveOrInvoked() {
        mCPUSpy.operand = 0x3;
        mCPUSpy.executeInstruction(0x8);
        verify(mCPUSpy).exclusiveOr();
    }
    
    @Test
    public void testAddRegisterToRegisterInvoked() {
        mCPUSpy.operand = 0x4;
        mCPUSpy.executeInstruction(0x8);
        verify(mCPUSpy).addRegisterToRegister();
    }
    
    @Test
    public void testSubtractRegisterFromRegisterInvoked() {
        mCPUSpy.operand = 0x5;
        mCPUSpy.executeInstruction(0x8);
        verify(mCPUSpy).subtractRegisterFromRegister();
    }
    
    @Test
    public void testRightShiftInvoked() {
        mCPUSpy.operand = 0x6;
        mCPUSpy.executeInstruction(0x8);
        verify(mCPUSpy).rightShift();
    }
    
    @Test
    public void testSubtractRegisterFromRegister1Invoked() {
        mCPUSpy.operand = 0x7;
        mCPUSpy.executeInstruction(0x8);
        verify(mCPUSpy).subtractRegisterFromRegister1();
    }
    
    @Test
    public void testLeftShiftInvoked() {
        mCPUSpy.operand = 0xE;
        mCPUSpy.executeInstruction(0x8);
        verify(mCPUSpy).leftShift();
    }
    
    @Test
    public void testLogicalOperationsNotSupported() {
        mCPU.operand = 0x8008;
        mCPU.executeInstruction(0x8);
        assertEquals("Operation 8008 not supported", mCPU.getOpShortDesc());

        mCPU.operand = 0x8009;
        mCPU.executeInstruction(0x8);
        assertEquals("Operation 8009 not supported", mCPU.getOpShortDesc());

        mCPU.operand = 0x800A;
        mCPU.executeInstruction(0x8);
        assertEquals("Operation 800A not supported", mCPU.getOpShortDesc());
     
        mCPU.operand = 0x800B;
        mCPU.executeInstruction(0x8);
        assertEquals("Operation 800B not supported", mCPU.getOpShortDesc());

        mCPU.operand = 0x800C;
        mCPU.executeInstruction(0x8);
        assertEquals("Operation 800C not supported", mCPU.getOpShortDesc());

        mCPU.operand = 0x800D;
        mCPU.executeInstruction(0x8);
        assertEquals("Operation 800D not supported", mCPU.getOpShortDesc());
    }
    
    @Test
    public void testSkipIfKeyPressedInvoked() {
        mCPUSpy.operand = 0x9E;
        mCPUSpy.executeInstruction(0xE);
        verify(mCPUSpy).skipIfKeyPressed();
    }
    
    @Test
    public void testSkipIfKeyNotPressedInvoked() {
        mCPUSpy.operand = 0xA1;
        mCPUSpy.executeInstruction(0xE);
        verify(mCPUSpy).skipIfKeyNotPressed();
    }
    
    @Test
    public void testKeyPressedSubroutinesNotSupported() {
        for (int subfunction = 0; subfunction < 0xFF; subfunction++) {
            if ((subfunction != 0x9E) && (subfunction != 0xA1)) {
                mCPU.operand = 0xE000;
                mCPU.operand += subfunction;
                mCPU.executeInstruction(0xE);
                assertEquals("Operation " + String.format("%04X", mCPU.operand) + " not supported", mCPU.getOpShortDesc());
            }
        }
    }
    
    @Test
    public void testMoveDelayTimerIntoRegisterInvoked() {
        mCPUSpy.operand = 0x07;
        mCPUSpy.executeInstruction(0xF);
        verify(mCPUSpy).moveDelayTimerIntoRegister();
    }
    
    @Test
    public void testWaitForKeypressInvoked() {
        mCPUSpy.operand = 0x0A;
        mCPUSpy.executeInstruction(0xF);
        verify(mCPUSpy).waitForKeypress();
    }
    
    @Test
    public void testMoveRegisterIntoDelayRegisterInvoked() {
        mCPUSpy.operand = 0x15;
        mCPUSpy.executeInstruction(0xF);
        verify(mCPUSpy).moveRegisterIntoDelayRegister();
    }
    
    @Test
    public void testMoveRegisterIntoSoundRegisterInvoked() {
        mCPUSpy.operand = 0x18;
        mCPUSpy.executeInstruction(0xF);
        verify(mCPUSpy).moveRegisterIntoSoundRegister();
    }
    
    @Test
    public void testAddRegisterIntoIndexInvoked() {
        mCPUSpy.operand = 0x1E;
        mCPUSpy.executeInstruction(0xF);
        verify(mCPUSpy).addRegisterIntoIndex();
    }
    
    @Test
    public void testLoadIndexWithSpriteInvoked() {
        mCPUSpy.operand = 0x29;
        mCPUSpy.executeInstruction(0xF);
        verify(mCPUSpy).loadIndexWithSprite();
    }
    
    @Test
    public void testStoreBCDInMemoryInvoked() {
        mCPUSpy.operand = 0x33;
        mCPUSpy.executeInstruction(0xF);
        verify(mCPUSpy).storeBCDInMemory();
    }
    
    @Test
    public void testStoreRegistersInMemoryInvoked() {
        mCPUSpy.operand = 0x55;
        mCPUSpy.executeInstruction(0xF);
        verify(mCPUSpy).storeRegistersInMemory();
    }
    
    @Test
    public void testReadRegistersFromMemoryInvoked() {
        mCPUSpy.operand = 0x65;
        mCPUSpy.executeInstruction(0xF);
        verify(mCPUSpy).readRegistersFromMemory();
    }
    
    @Test
    public void testIOSubroutinesNotSupported() {
        for (int subfunction = 0; subfunction < 0xFF; subfunction++) {
            if ((subfunction != 0x07) && (subfunction != 0x0A) &&
                    (subfunction != 0x15) && (subfunction != 0x18) &&
                    (subfunction != 0x1E) && (subfunction != 0x29) &&
                    (subfunction != 0x33) && (subfunction != 0x55) &&
                    (subfunction != 0x65)) {
                mCPU.operand = 0xF000;
                mCPU.operand += subfunction;
                mCPU.executeInstruction(0xF);
                assertEquals("Operation " + String.format("%04X", mCPU.operand) + " not supported", mCPU.getOpShortDesc());
            }
        }
    }
    
    @Test
    public void testScreenSubroutinesNotSupported() {
        for (int subfunction = 0; subfunction < 0xFF; subfunction++) {
            if ((subfunction != 0xE0) && (subfunction != 0xEE)) {
                mCPU.operand = 0x0000;
                mCPU.operand += subfunction;
                mCPU.executeInstruction(0x0);
                assertEquals("Operation " + String.format("%04X", mCPU.operand) + " not supported", mCPU.getOpShortDesc());
            }
        }
    }
    
    @Test
    public void testScreenClearInvoked() {
        mCPU.operand = 0xE0;
        mCPU.executeInstruction(0x0);
        // Both update and clear are called twice (first time on initialization)
        verify(mScreenMock, times(2)).clearScreen();
        verify(mScreenMock, times(2)).updateScreen();
        assertEquals("CLS", mCPU.getOpShortDesc());
    }
    
    @Test
    public void testWaitForKeypress() {
        int register = 1;
        mCPU.operand = register << 8;
        mCPU.waitForKeypress();
        assertEquals(9, mCPU.v[register]);
    }
    
    @Test
    public void testCPUStatusLine1() {
        mCPU.index = 0x10;
        mCPU.delay = 0x9;
        mCPU.sound = 0x3;
        mCPU.pc = 0x1234;
        mCPU.operand = 0x9876;
        mCPU.lastOpDesc = "none";
        String expected = "I:0010 DT:09 ST:03 PC:1234 9876 none";
        assertEquals(expected, mCPU.cpuStatusLine1());
    }
    
    @Test
    public void testCPUStatusLine2() {
        mCPU.v[0] = 0x10;
        mCPU.v[1] = 0x11;
        mCPU.v[2] = 0x12;
        mCPU.v[3] = 0x13;
        mCPU.v[4] = 0x14;
        mCPU.v[5] = 0x15;
        mCPU.v[6] = 0x16;
        mCPU.v[7] = 0x17;
        String expected = "V0:10 V1:11 V2:12 V3:13 V4:14 V5:15 V6:16 V7:17";
        assertEquals(expected, mCPU.cpuStatusLine2());
    }
    
    @Test
    public void testCPUStatusLine3() {
        mCPU.v[8] = 0x18;
        mCPU.v[9] = 0x19;
        mCPU.v[10] = 0x20;
        mCPU.v[11] = 0x21;
        mCPU.v[12] = 0x22;
        mCPU.v[13] = 0x23;
        mCPU.v[14] = 0x24;
        mCPU.v[15] = 0x25;
        String expected = "V8:18 V9:19 VA:20 VB:21 VC:22 VD:23 VE:24 VF:25";
        assertEquals(expected, mCPU.cpuStatusLine3());
    }
    
    @Test
    public void testFetchIncrementExecute() {
        mCPU.pc = 0;
        mMemory.write(0x02, 0);
        mMemory.write(0x34, 1);
        mCPU.fetchIncrementExecute();
        assertEquals(2, mCPU.pc);
        assertEquals(0x0234, mCPU.operand);
    }
    
    @Test
    public void testExecuteInstructionDoesNotFireOnNegativeOpcode() {
        mCPU.executeInstruction(-1);
        assertEquals("Operation 0000 not supported", mCPU.lastOpDesc);
    }
    
    @Test
    public void testDrawSpriteDrawsCorrectPattern() throws FileNotFoundException, FontFormatException, IOException {
        Screen screen = new Screen();
        mCPU = new CentralProcessingUnit(mMemory, mKeyboardMock, screen);
        mCPU.index = 0x200;
        mMemory.write(0xAA, 0x200);
        mCPU.v[0] = 0;
        mCPU.v[1] = 0;
        mCPU.operand = 0x11;
        mCPU.drawSprite();
        assertTrue(screen.pixelOn(0, 0));
        assertFalse(screen.pixelOn(1, 0));
        assertTrue(screen.pixelOn(2, 0));
        assertFalse(screen.pixelOn(3, 0));
        assertTrue(screen.pixelOn(4, 0));
        assertFalse(screen.pixelOn(5, 0));
        assertTrue(screen.pixelOn(6, 0));
        assertFalse(screen.pixelOn(7, 0));
        screen.dispose();
    }
    
    @Test
    public void testDrawSpriteOverTopSpriteTurnsOff() throws FileNotFoundException, FontFormatException, IOException {
        Screen screen = new Screen();
        mCPU = new CentralProcessingUnit(mMemory, mKeyboardMock, screen);
        mCPU.index = 0x200;
        mMemory.write(0xFF, 0x200);
        mCPU.v[0] = 0;
        mCPU.v[1] = 0;
        mCPU.operand = 0x11;
        mCPU.drawSprite();
        mCPU.drawSprite();
        assertEquals(1, mCPU.v[0xF]);
        assertFalse(screen.pixelOn(0, 0));
        assertFalse(screen.pixelOn(1, 0));
        assertFalse(screen.pixelOn(2, 0));
        assertFalse(screen.pixelOn(3, 0));
        assertFalse(screen.pixelOn(4, 0));
        assertFalse(screen.pixelOn(5, 0));
        assertFalse(screen.pixelOn(6, 0));
        assertFalse(screen.pixelOn(7, 0));
        screen.dispose();
    }
    
    @Test
    public void testDrawNoSpriteOverTopSpriteLeavesOn() throws FileNotFoundException, FontFormatException, IOException {
        Screen screen = new Screen();
        mCPU = new CentralProcessingUnit(mMemory, mKeyboardMock, screen);
        mCPU.index = 0x200;
        mMemory.write(0xFF, 0x200);
        mCPU.v[0] = 0;
        mCPU.v[1] = 0;
        mCPU.operand = 0x11;
        mCPU.drawSprite();
        mMemory.write(0x00, 0x200);
        mCPU.drawSprite();
        assertEquals(0, mCPU.v[0xF]);
        assertTrue(screen.pixelOn(0, 0));
        assertTrue(screen.pixelOn(1, 0));
        assertTrue(screen.pixelOn(2, 0));
        assertTrue(screen.pixelOn(3, 0));
        assertTrue(screen.pixelOn(4, 0));
        assertTrue(screen.pixelOn(5, 0));
        assertTrue(screen.pixelOn(6, 0));
        assertTrue(screen.pixelOn(7, 0));
        screen.dispose();
    }
 }
