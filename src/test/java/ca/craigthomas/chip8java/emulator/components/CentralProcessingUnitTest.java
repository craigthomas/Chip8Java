/*
 * Copyright (C) 2013-2024 Craig Thomas
 * This project uses an MIT style license - see LICENSE for details.
 */
package ca.craigthomas.chip8java.emulator.components;

import static ca.craigthomas.chip8java.emulator.components.CentralProcessingUnit.MODE_EXTENDED;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import java.awt.*;
import java.io.IOException;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.swing.*;

/**
 * Tests for the Chip8 CPU.
 */
public class CentralProcessingUnitTest
{
    private Screen screenMock;
    private Keyboard keyboardMock;
    private Memory memory;
    private CentralProcessingUnit cpu;
    private CentralProcessingUnit cpuSpy;
    private Screen screen;
    private JFrame container;
    private Canvas canvas;

    @Before
    public void setUp() {
        memory = new Memory();
        screenMock = Mockito.mock(Screen.class);
        keyboardMock = Mockito.mock(Keyboard.class);
        Mockito.when(keyboardMock.getCurrentKey()).thenReturn(9);
        cpu = new CentralProcessingUnit(memory, keyboardMock, screenMock);
        cpuSpy = spy(cpu);
        canvas = new Canvas();
    }

    private void setUpCanvas() throws IOException, FontFormatException {
        screen = new Screen();
        container = new JFrame();

        JPanel panel = (JPanel) container.getContentPane();
        panel.setPreferredSize(new Dimension(screen.getWidth() * screen.getScale(), screen.getHeight() * screen.getScale()));
        panel.setLayout(null);

        canvas.setBounds(0, 0, screen.getWidth() * screen.getScale(), screen.getHeight() * screen.getScale());
        canvas.setIgnoreRepaint(true);

        panel.add(canvas);

        container.pack();
        container.setResizable(false);
        container.setVisible(true);

        canvas.createBufferStrategy(2);
    }

    private void tearDownCanvas() {
        container.dispose();
    }

    @Test
    public void testReturnFromSubroutine() {
        for (int address = 0x200; address < 0xFFFF; address += 0x10) {
            memory.write(address & 0x00FF, cpu.stack);
            memory.write((address & 0xFF00) >> 8, cpu.stack + 1);
            cpu.stack += 2;
            cpu.pc = 0;
            cpu.returnFromSubroutine();
            assertEquals(address, cpu.pc);
        }
    }

    @Test
    public void testJumpToAddress() {
        for (int address = 0x0; address < 0xFFFF; address += 0x10) {
            cpu.operand = address;
            cpu.pc = 0;
            assertEquals(0, cpu.pc);
            cpu.jumpToAddress();
            assertEquals(address & 0x0FFF, cpu.pc);
        }
    }

    @Test
    public void testJumpToSubroutine() {
        for (int address = 0x200; address < 0xFFFF; address += 0x10) {
            cpu.operand = address;
            cpu.stack = 0;
            cpu.pc = 0x100;
            cpu.jumpToSubroutine();
            assertEquals(address & 0x0FFF, cpu.pc);
            assertEquals(2, cpu.stack);
            assertEquals(0, memory.read(0));
            assertEquals(1, memory.read(1));
        }
    }

    @Test
    public void testSkipIfRegisterEqualValue() {
        for (int register = 0; register < 0x10; register++) {
            for (int value = 0; value < 0xFF; value += 0x10) {
                for (int regValue = 0; regValue < 0xFF; regValue++) {
                    cpu.operand = register << 8;
                    cpu.operand += value;
                    cpu.v[register] = (short) regValue;
                    cpu.pc = 0;
                    cpu.skipIfRegisterEqualValue();
                    if (value == regValue) {
                        assertEquals(2, cpu.pc);
                    } else {
                        assertEquals(0, cpu.pc);
                    }
                }
            }
        }
    }

    @Test
    public void testSkipIfRegisterEqualValueLoadLongException() {
        memory.write(0xF0, 0x0200);
        memory.write(0x00, 0x0201);
        cpu.v[1] = 1;
        cpu.operand = 0x3101;
        cpu.skipIfRegisterEqualValue();
        assertEquals(0x0204, cpu.pc);
    }

    @Test
    public void testSkipIfRegisterNotEqualValue() {
        for (int register = 0; register < 0x10; register++) {
            for (int value = 0; value < 0xFF; value += 0x10) {
                for (int regValue = 0; regValue < 0xFF; regValue++) {
                    cpu.operand = register << 8;
                    cpu.operand += value;
                    cpu.v[register] = (short) regValue;
                    cpu.pc = 0;
                    cpu.skipIfRegisterNotEqualValue();
                    if (value != regValue) {
                        assertEquals(2, cpu.pc);
                    } else {
                        assertEquals(0, cpu.pc);
                    }
                }
            }
        }
    }

    @Test
    public void testSkipIfRegisterNotEqualValueLoadLongException() {
        memory.write(0xF0, 0x0200);
        memory.write(0x00, 0x0201);
        cpu.v[1] = 1;
        cpu.operand = 0x4102;
        cpu.skipIfRegisterNotEqualValue();
        assertEquals(0x0204, cpu.pc);
    }

    @Test
    public void testSkipIfRegisterEqualRegister() {
        for (int register = 0; register < 0x10; register++) {
            cpu.v[register] = (short) register;
        }

        for (int register1 = 0; register1 < 0x10; register1++) {
            for (int register2 = 0; register2 < 0x10; register2++) {
                cpu.operand = register1;
                cpu.operand <<= 4;
                cpu.operand += register2;
                cpu.operand <<= 4;
                cpu.pc = 0;
                cpu.skipIfRegisterEqualRegister();
                if (register1 == register2) {
                    assertEquals(2, cpu.pc);
                } else {
                    assertEquals(0, cpu.pc);
                }
            }
        }
    }

    @Test
    public void testSkipIfRegisterEqualRegisterLoadLongException() {
        memory.write(0xF0, 0x0200);
        memory.write(0x00, 0x0201);
        cpu.v[1] = 1;
        cpu.v[2] = 1;
        cpu.operand = 0x5120;
        cpu.skipIfRegisterEqualRegister();
        assertEquals(0x0204, cpu.pc);
    }

    @Test
    public void testSkipIfRegisterNotEqualRegister() {
        for (int register = 0; register < 0x10; register++) {
            cpu.v[register] = (short) register;
        }

        for (int register1 = 0; register1 < 0x10; register1++) {
            for (int register2 = 0; register2 < 0x10; register2++) {
                cpu.operand = register1;
                cpu.operand <<= 4;
                cpu.operand += register2;
                cpu.operand <<= 4;
                cpu.pc = 0;
                cpu.skipIfRegisterNotEqualRegister();
                if (register1 != register2) {
                    assertEquals(2, cpu.pc);
                } else {
                    assertEquals(0, cpu.pc);
                }
            }
        }
    }

    @Test
    public void testSkipIfRegisterNotEqualRegisterLoadLongException() {
        memory.write(0xF0, 0x0200);
        memory.write(0x00, 0x0201);
        cpu.v[1] = 1;
        cpu.v[2] = 2;
        cpu.operand = 0x9120;
        cpu.skipIfRegisterNotEqualRegister();
        assertEquals(0x0204, cpu.pc);
    }

    @Test
    public void testMoveValueToRegister() {
        int value = 0x23;

        for (int register = 0; register < 0x10; register++) {
            cpu.operand = 0x60 + register;
            cpu.operand <<= 8;
            cpu.operand += value;
            cpu.moveValueToRegister();
            for (int registerToCheck = 0; registerToCheck < 0x10; registerToCheck++) {
                if (registerToCheck != register) {
                    assertEquals(0, cpu.v[registerToCheck]);
                } else {
                    assertEquals(value, cpu.v[registerToCheck]);
                }
            }
            cpu.v[register] = 0;
        }
    }

    @Test
    public void testAddValueToRegister() {
        for (int register = 0; register < 0x10; register++) {
            for (int registerValue = 0; registerValue < 0xFF; registerValue += 0x10) {
                for (int value = 0; value < 0xFF; value++) {
                    cpu.v[register] = (short) registerValue;
                    cpu.operand = register << 8;
                    cpu.operand += value;
                    cpu.addValueToRegister();
                    if (value + registerValue < 256) {
                        assertEquals(value + registerValue, cpu.v[register]);
                    } else {
                        assertEquals(value + registerValue - 256,
                                cpu.v[register]);
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
                    cpu.v[target] = 0x32;
                    cpu.v[source] = 0;
                    cpu.operand = source << 8;
                    cpu.operand += (target << 4);
                    cpu.moveRegisterIntoRegister();
                    assertEquals(0x32, cpu.v[source]);
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
                            cpu.v[source] = (short) sourceVal;
                            cpu.v[target] = (short) targetVal;
                            cpu.operand = source << 8;
                            cpu.operand += (target << 4);
                            cpu.logicalOr();
                            assertEquals(sourceVal | targetVal, cpu.v[source]);
                        }
                    }
                }
            }
        }
    }

    @Test
    public void testLogicalOrLogicQuirksClearsFlag() {
        cpu.v[1] = 0;
        cpu.v[2] = 1;
        cpu.v[0xF] = 1;
        cpu.operand = 0x8121;
        cpu.setLogicQuirks(true);
        cpu.logicalOr();
        assertEquals(1, cpu.v[1]);
        assertEquals(0, cpu.v[0xF]);
    }

    @Test
    public void testLogicalAnd() {
        for (int source = 0; source < 0x10; source++) {
            for (int target = 0; target < 0x10; target++) {
                if (source != target) {
                    for (int sourceVal = 0; sourceVal < 0xFF; sourceVal += 0x10) {
                        for (int targetVal = 0; targetVal < 0xFF; targetVal += 0x10) {
                            cpu.v[source] = (short) sourceVal;
                            cpu.v[target] = (short) targetVal;
                            cpu.operand = source << 8;
                            cpu.operand += (target << 4);
                            cpu.logicalAnd();
                            assertEquals(sourceVal & targetVal, cpu.v[source]);
                        }
                    }
                }
            }
        }
    }

    @Test
    public void testLogicalAndLogicQuirksClearsFlag() {
        cpu.v[1] = 0;
        cpu.v[2] = 1;
        cpu.v[0xF] = 1;
        cpu.operand = 0x8122;
        cpu.setLogicQuirks(true);
        cpu.logicalAnd();
        assertEquals(0, cpu.v[1]);
        assertEquals(0, cpu.v[0xF]);
    }

    @Test
    public void testExclusiveOr() {
        for (int source = 0; source < 0x10; source++) {
            for (int target = 0; target < 0x10; target++) {
                if (source != target) {
                    for (int sourceVal = 0; sourceVal < 0xFF; sourceVal += 0x10) {
                        for (int targetVal = 0; targetVal < 0xFF; targetVal += 0x10) {
                            cpu.v[source] = (short) sourceVal;
                            cpu.v[target] = (short) targetVal;
                            cpu.operand = source << 8;
                            cpu.operand += (target << 4);
                            cpu.exclusiveOr();
                            assertEquals(sourceVal ^ targetVal, cpu.v[source]);
                        }
                    }
                }
            }
        }
    }

    @Test
    public void testExclusiveOrLogicQuirksClearsFlag() {
        cpu.v[1] = 1;
        cpu.v[2] = 1;
        cpu.v[0xF] = 1;
        cpu.operand = 0x8123;
        cpu.setLogicQuirks(true);
        cpu.exclusiveOr();
        assertEquals(0, cpu.v[1]);
        assertEquals(0, cpu.v[0xF]);
    }

    @Test
    public void testAddToRegister() {
        for (int source = 0; source < 0xF; source++) {
            for (int target = 0; target < 0xF; target++) {
                if (source != target) {
                    for (int sourceVal = 0; sourceVal < 0xFF; sourceVal += 0x10) {
                        for (int targetVal = 0; targetVal < 0xFF; targetVal += 0x10) {
                            cpu.v[source] = (short) sourceVal;
                            cpu.v[target] = (short) targetVal;
                            cpu.operand = source << 8;
                            cpu.operand += (target << 4);
                            cpu.addRegisterToRegister();
                            if ((sourceVal + targetVal) > 255) {
                                assertEquals(sourceVal + targetVal - 256,
                                        cpu.v[source]);
                                assertEquals(1, cpu.v[0xF]);
                            } else {
                                assertEquals(sourceVal + targetVal,
                                        cpu.v[source]);
                                assertEquals(0, cpu.v[0xF]);
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
                            cpu.v[source] = (short) sourceVal;
                            cpu.v[target] = (short) targetVal;
                            cpu.operand = source << 8;
                            cpu.operand += (target << 4);
                            cpu.subtractRegisterFromRegister();
                            if (sourceVal > targetVal) {
                                assertEquals(sourceVal - targetVal, cpu.v[source]);
                                assertEquals(1, cpu.v[0xF]);
                            }
                            if (sourceVal < targetVal) {
                                assertEquals(sourceVal - targetVal + 256, cpu.v[source]);
                                assertEquals(0, cpu.v[0xF]);
                            }
                            if (sourceVal == targetVal) {
                                assertEquals(0, cpu.v[source]);
                                assertEquals(1, cpu.v[0xF]);
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    public void testRightShift() {
        for (int x = 0; x < 0xF; x++) {
            for (int y = 0; y < 0xF; y++) {
                for (int value = 0; value < 256; value++) {
                    cpu.operand = x << 8;
                    cpu.operand |= y << 4;
                    cpu.v[y] = (short) value;
                    short shifted_val = (short) (value >> 1);
                    short bit_zero = (short) (cpu.v[y] & 0x1);
                    cpu.v[0xF] = (short) 0;
                    cpu.rightShift();
                    assertEquals(shifted_val, cpu.v[x]);
                    assertEquals(bit_zero, cpu.v[0xF]);
                }
            }
        }
    }

    @Test
    public void testRightShiftQuirks() {
        cpu.setShiftQuirks(true);
        for (int x = 0; x < 0xF; x++) {
            for (int value = 0; value < 256; value++) {
                cpu.operand = x << 8;
                cpu.v[x] = (short) value;
                short shifted_val = (short) (value >> 1);
                short bit_zero = (short) (cpu.v[x] & 0x1);
                cpu.v[0xF] = (short) 0;
                cpu.rightShift();
                assertEquals(shifted_val, cpu.v[x]);
                assertEquals(bit_zero, cpu.v[0xF]);
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
                            cpu.v[source] = (short) sourceValue;
                            cpu.v[target] = (short) targetValue;
                            cpu.operand = source << 8;
                            cpu.operand += (target << 4);
                            cpu.subtractRegisterFromRegister1();
                            if (targetValue > sourceValue) {
                                assertEquals(targetValue - sourceValue, cpu.v[source]);
                                assertEquals(1, cpu.v[0xF]);
                            }
                            if (targetValue < sourceValue) {
                                assertEquals(256 + targetValue - sourceValue, cpu.v[source]);
                                assertEquals(0, cpu.v[0xF]);
                            }
                            if (targetValue == sourceValue) {
                                assertEquals(0, cpu.v[source]);
                                assertEquals(1, cpu.v[0xF]);
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    public void testLeftShift() {
        for (int x = 0; x < 0xF; x++) {
            for (int y = 0; y < 0xF; y++) {
                for (int value = 0; value < 256; value++) {
                    cpu.v[y] = (short) value;
                    cpu.operand = x << 8;
                    cpu.operand |= y << 4;
                    short bit_seven = (short) ((value & 0x80) >> 7);
                    short shifted_val = (short) ((value << 1) & 0xFF);
                    cpu.v[0xF] = (short) 0;
                    cpu.leftShift();
                    assertEquals(shifted_val, cpu.v[x]);
                    assertEquals(bit_seven, cpu.v[0xF]);
                }
            }
        }
    }

    @Test
    public void testLeftShiftQuirks() {
        cpu.setShiftQuirks(true);
        for (int x = 0; x < 0xF; x++) {
            for (int value = 0; value < 256; value++) {
                cpu.v[x] = (short) value;
                cpu.operand = x << 8;
                short bit_seven = (short) ((value & 0x80) >> 7);
                short shifted_val = (short) ((value << 1) & 0xFF);
                cpu.v[0xF] = (short) 0;
                cpu.leftShift();
                assertEquals(shifted_val, cpu.v[x]);
                assertEquals(bit_seven, cpu.v[0xF]);
            }
        }
    }

    @Test
    public void testLoadIndexWithValue() {
        for (int value = 0; value < 0x10000; value++) {
            cpu.operand = value;
            cpu.loadIndexWithValue();
            assertEquals(value & 0x0FFF, cpu.index);
        }
    }

    @Test
    public void testGenerateRandomNumber() {
        for (int register = 0; register < 0xF; register++) {
            for (int value = 0; value < 0xFF; value += 10) {
                cpu.v[register] = -1;
                cpu.operand = register << 8;
                cpu.operand += value;
                cpu.generateRandomNumber();
                assertTrue(cpu.v[register] >= 0);
                assertTrue(cpu.v[register] <= 255);
            }
        }
    }

    @Test
    public void testMoveDelayTimerIntoRegister() {
        for (int register = 0; register < 0xF; register++) {
            for (int value = 0; value < 0xFF; value += 10) {
                cpu.delay = (short) value;
                cpu.operand = register << 8;
                cpu.v[register] = 0;
                cpu.moveDelayTimerIntoRegister();
                assertEquals(value, cpu.v[register]);
            }
        }
    }

    @Test
    public void testMoveRegisterIntoDelayRegister() {
        int register = 8;
        short value = 10;
        cpu.v[register] = value;
        cpu.operand = register << 8;
        cpu.delay = 0;
        cpu.moveRegisterIntoDelayRegister();
        assertEquals(value, cpu.delay);
    }

    @Test
    public void testLoadIndexWithSprite() {
        for (int number = 0; number < 0x10; number++) {
            cpu.index = 0xFFF;
            cpu.v[0] = (short) number;
            cpu.operand = 0xF029;
            cpu.loadIndexWithSprite();
            assertEquals(number * 5, cpu.index);
        }
    }

    @Test
    public void testLoadIndexWithExtendedSprite() {
        for (int number = 0; number < 0x10; number++) {
            cpu.index = 0xFFF;
            cpu.v[0] = (short) number;
            cpu.operand = 0xF030;
            cpu.loadIndexWithExtendedSprite();
            assertEquals(number * 10, cpu.index);
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
            cpu.index = 0;
            cpu.v[0] = (short) number;
            cpu.operand = 0xF033;
            cpu.storeBCDInMemory();
            assertEquals(bcdValue.charAt(0), String.valueOf(memory.read(0))
                    .charAt(0));
            assertEquals(bcdValue.charAt(1), String.valueOf(memory.read(1))
                    .charAt(0));
            assertEquals(bcdValue.charAt(2), String.valueOf(memory.read(2))
                    .charAt(0));
        }
    }

    @Test
    public void testReadRegistersFromMemory() {
        int index = 0x500;
        cpu.index = index;

        for (int n = 0; n < 0x10; n++) {
            memory.write(n + 0x89, index + n);
        }

        for (int n = 0; n < 0x10; n++) {
            cpu.index = index;
            for (int r = 0; r < 0x10; r++) {
                cpu.v[r] = 0;
            }

            cpu.operand = 0xF065;
            cpu.operand |= (n << 8);
            int indexBefore = cpu.index;
            cpu.readRegistersFromMemory();
            assertEquals(indexBefore + n + 1, cpu.index);

            for (int r = 0; r < 0x10; r++) {
                if (r > n) {
                    assertEquals(0, cpu.v[r]);
                } else {
                    assertEquals(r + 0x89, cpu.v[r]);
                }
            }
        }
    }

    @Test
    public void testReadRegistersFromMemoryIndexQuirks() {
        cpu.setIndexQuirks(true);
        int index = 0x500;
        cpu.index = index;

        for (int register = 0; register < 0xF; register++) {
            memory.write(register + 0x89, index + register);
        }

        for (int register = 0; register < 0xF; register++) {
            for (int registerToSet = 0; registerToSet < 0xF; registerToSet++) {
                cpu.v[registerToSet] = 0;
            }

            cpu.operand = 0xF000;
            cpu.operand += (register << 8);
            cpu.operand += 0x65;
            cpu.readRegistersFromMemory();
            for (int registerToCheck = 0; registerToCheck <= 0xF; registerToCheck++) {
                if (registerToCheck > register) {
                    assertEquals(0, cpu.v[registerToCheck]);
                } else {
                    assertEquals(registerToCheck + 0x89,
                            cpu.v[registerToCheck]);
                }
            }
        }
    }

    @Test
    public void testSkipIfKeyPressedSkipsCorrectly() {
        Keyboard keyboard = new Keyboard();
        keyboard.keypressMap[9] = true;
        cpu = new CentralProcessingUnit(memory, keyboard, screen);
        for (int register = 0; register < 0xF; register++) {
            cpu.v[register] = 9;
            cpu.operand = register << 8;
            cpu.pc = 0;
            cpu.skipIfKeyPressed();
            assertEquals(2, cpu.pc);
        }
    }

    @Test
    public void testSkipIfKeyPressedDoesNotSkipIfNotPressed() {
        for (int register = 0; register < 0xF; register++) {
            cpu.v[register] = 8;
            cpu.operand = register << 8;
            cpu.pc = 0;
            cpu.skipIfKeyPressed();
            assertEquals(0, cpu.pc);
        }
    }

    @Test
    public void testSkipIfKeyNotPressedSkipsCorrectly() {
        for (int register = 0; register < 0xF; register++) {
            cpu.v[register] = 8;
            cpu.operand = register << 8;
            cpu.pc = 0;
            cpu.skipIfKeyNotPressed();
            assertEquals(2, cpu.pc);
        }
    }

    @Test
    public void testSkipIfKeyNotPressedDoesNotSkipIfPressed() {
        Keyboard keyboard = new Keyboard();
        cpu = new CentralProcessingUnit(memory, keyboard, screen);
        keyboard.keypressMap[9] = true;
        for (int register = 0; register < 0xF; register++) {
            cpu.v[register] = 9;
            cpu.operand = register << 8;
            cpu.pc = 0;
            cpu.skipIfKeyNotPressed();
            assertEquals(0, cpu.pc);
        }
    }

    @Test
    public void testJumpToRegisterPlusValue() {
        for (int index = 0; index < 0xFFF; index += 10) {
            for (int value = 0; value < 0xFFF; value += 10) {
                cpu.v[0] = (short) index;
                cpu.pc = 0;
                cpu.operand = (short) value;
                cpu.jumpToRegisterPlusValue();
                assertEquals(index + value, cpu.pc);
            }
        }
    }

    @Test
    public void testJumpToRegisterPlusValueJumpQuirks() {
        cpu.setJumpQuirks(true);
        for (int register = 0; register <= 0x0F; register++) {
            for (int index = 0; index < 0xFFF; index += 10) {
                for (int value = 0; value < 0xFF; value += 10) {
                    cpu.v[register] = (short) index;
                    cpu.pc = 0;
                    cpu.operand = (short) value;
                    cpu.operand |= (register << 8);
                    cpu.jumpToRegisterPlusValue();
                    assertEquals(index + value, cpu.pc);
                }
            }
        }
    }

    @Test
    public void testAddRegisterToIndex() {
        for (int register = 0; register < 0xF; register++) {
            for (int index = 0; index < 0xFFF; index += 10) {
                cpu.index = index;
                cpu.v[register] = 0x89;
                cpu.operand = register << 8;
                cpu.addRegisterIntoIndex();
                assertEquals(index + 0x89, cpu.index);
            }
        }
    }

    @Test
    public void testStoreRegistersInMemory() {
        int index = 0x500;
        cpu.index = index;

        for (int x = 0; x < 0x10; x++) {
            cpu.v[x] = (short) (x + 0x89);
        }

        for (int n = 0; n < 0x10; n++) {
            cpu.index = index;
            for (int c = 0; c < 0x10; c++) {
                memory.write(0x00, cpu.index + c);
            }

            cpu.operand = n << 8;
            int indexBefore = cpu.index;
            cpu.storeRegistersInMemory();
            assertEquals(indexBefore + n + 1, cpu.index);

            for (int c = 0; c < 0x10; c++) {
                if (c > n) {
                    assertEquals(0, memory.read(index + c));
                } else {
                    assertEquals(0x89 + c, memory.read(index + c));
                }
            }
        }
    }

    @Test
    public void testStoreRegistersInMemoryIndexQuirks() {
        cpu.setIndexQuirks(true);
        int index = 0x500;
        cpu.index = index;

        for (int x = 0; x < 0x10; x++) {
            cpu.v[x] = (short) (x + 0x89);
        }

        for (int n = 0; n < 0x10; n++) {
            cpu.index = index;
            for (int c = 0; c < 0x10; c++) {
                memory.write(0x00, cpu.index + c);
            }

            cpu.operand = n << 8;
            int indexBefore = cpu.index;
            cpu.storeRegistersInMemory();
            assertEquals(indexBefore, cpu.index);

            for (int c = 0; c < 0x10; c++) {
                if (c > n) {
                    assertEquals(0, memory.read(index + c));
                } else {
                    assertEquals(0x89 + c, memory.read(index + c));
                }
            }
        }
    }

    @Test
    public void testGetOpShortDescReturnsADescription() {
        cpu.returnFromSubroutine();
        assertEquals("RTS", cpu.getOpShortDesc());
    }

    @Test
    public void testGetOpReturnsHexValueOfOp() {
        cpu.operand = 0xABCD;
        assertEquals("ABCD", cpu.getOp());
    }

    @Test
    public void testToHex() {
        assertEquals("ABCD", CentralProcessingUnit.toHex(43981, 4));
    }

    @Test
    public void testJumpToAddressInvoked() {
        cpuSpy.executeInstruction(0x1);
        verify(cpuSpy).jumpToAddress();
    }

    @Test
    public void testJumpToSubroutineInvoked() {
        cpuSpy.executeInstruction(0x2);
        verify(cpuSpy).jumpToSubroutine();
    }

    @Test
    public void testSkipIfRegisterEqualValueInvoked() {
        cpuSpy.executeInstruction(0x3);
        verify(cpuSpy).skipIfRegisterEqualValue();
    }

    @Test
    public void testSkipIfRegisterNotEqualValueInvoked() {
        cpuSpy.executeInstruction(0x4);
        verify(cpuSpy).skipIfRegisterNotEqualValue();
    }

    @Test
    public void testSkipIfRegisterEqualRegisterInvoked() {
        cpuSpy.executeInstruction(0x5);
        verify(cpuSpy).skipIfRegisterEqualRegister();
    }

    @Test
    public void testMoveValueToRegisterInvoked() {
        cpuSpy.executeInstruction(0x6);
        verify(cpuSpy).moveValueToRegister();
    }

    @Test
    public void testAddValueToRegisterInvoked() {
        cpuSpy.executeInstruction(0x7);
        verify(cpuSpy).addValueToRegister();
    }

    @Test
    public void testSkipIfRegisterNotEqualRegisterInvoked() {
        cpuSpy.executeInstruction(0x9);
        verify(cpuSpy).skipIfRegisterNotEqualRegister();
    }

    @Test
    public void testLoadIndexWithValueInvoked() {
        cpuSpy.executeInstruction(0xA);
        verify(cpuSpy).loadIndexWithValue();
    }

    @Test
    public void testJumpToRegisterPlusValueInvoked() {
        cpuSpy.executeInstruction(0xB);
        verify(cpuSpy).jumpToRegisterPlusValue();
    }

    @Test
    public void testGenerateRandomNumberInvoked() {
        cpuSpy.executeInstruction(0xC);
        verify(cpuSpy).generateRandomNumber();
    }

    @Test
    public void testDrawSpriteInvoked() {
        cpuSpy.executeInstruction(0xD);
        verify(cpuSpy).drawSprite();
    }
    
    @Test
    public void testReturnFromSubroutineInvoked() {
        cpuSpy.operand = 0xEE;
        cpuSpy.executeInstruction(0x0);
        verify(cpuSpy).returnFromSubroutine();
    }
    
    @Test
    public void testMoveRegisterIntoRegisterInvoked() {
        cpuSpy.operand = 0x0;
        cpuSpy.executeInstruction(0x8);
        verify(cpuSpy).moveRegisterIntoRegister();
    }
    
    @Test
    public void testLogicalOrInvoked() {
        cpuSpy.operand = 0x1;
        cpuSpy.executeInstruction(0x8);
        verify(cpuSpy).logicalOr();
    }
    
    @Test
    public void testLogicalAndInvoked() {
        cpuSpy.operand = 0x2;
        cpuSpy.executeInstruction(0x8);
        verify(cpuSpy).logicalAnd();
    }
    
    @Test
    public void testExclusiveOrInvoked() {
        cpuSpy.operand = 0x3;
        cpuSpy.executeInstruction(0x8);
        verify(cpuSpy).exclusiveOr();
    }
    
    @Test
    public void testAddRegisterToRegisterInvoked() {
        cpuSpy.operand = 0x4;
        cpuSpy.executeInstruction(0x8);
        verify(cpuSpy).addRegisterToRegister();
    }
    
    @Test
    public void testSubtractRegisterFromRegisterInvoked() {
        cpuSpy.operand = 0x5;
        cpuSpy.executeInstruction(0x8);
        verify(cpuSpy).subtractRegisterFromRegister();
    }
    
    @Test
    public void testRightShiftInvoked() {
        cpuSpy.operand = 0x6;
        cpuSpy.executeInstruction(0x8);
        verify(cpuSpy).rightShift();
    }
    
    @Test
    public void testSubtractRegisterFromRegister1Invoked() {
        cpuSpy.operand = 0x7;
        cpuSpy.executeInstruction(0x8);
        verify(cpuSpy).subtractRegisterFromRegister1();
    }
    
    @Test
    public void testLeftShiftInvoked() {
        cpuSpy.operand = 0xE;
        cpuSpy.executeInstruction(0x8);
        verify(cpuSpy).leftShift();
    }
    
    @Test
    public void testLogicalOperationsNotSupported() {
        cpu.operand = 0x8008;
        cpu.executeInstruction(0x8);
        assertEquals("Operation 8008 not supported", cpu.getOpShortDesc());

        cpu.operand = 0x8009;
        cpu.executeInstruction(0x8);
        assertEquals("Operation 8009 not supported", cpu.getOpShortDesc());

        cpu.operand = 0x800A;
        cpu.executeInstruction(0x8);
        assertEquals("Operation 800A not supported", cpu.getOpShortDesc());
     
        cpu.operand = 0x800B;
        cpu.executeInstruction(0x8);
        assertEquals("Operation 800B not supported", cpu.getOpShortDesc());

        cpu.operand = 0x800C;
        cpu.executeInstruction(0x8);
        assertEquals("Operation 800C not supported", cpu.getOpShortDesc());

        cpu.operand = 0x800D;
        cpu.executeInstruction(0x8);
        assertEquals("Operation 800D not supported", cpu.getOpShortDesc());
    }
    
    @Test
    public void testSkipIfKeyPressedInvoked() {
        cpuSpy.operand = 0x9E;
        cpuSpy.executeInstruction(0xE);
        verify(cpuSpy).skipIfKeyPressed();
    }
    
    @Test
    public void testSkipIfKeyNotPressedInvoked() {
        cpuSpy.operand = 0xA1;
        cpuSpy.executeInstruction(0xE);
        verify(cpuSpy).skipIfKeyNotPressed();
    }
    
    @Test
    public void testKeyPressedSubroutinesNotSupported() {
        for (int subfunction = 0; subfunction < 0xFF; subfunction++) {
            if ((subfunction != 0x9E) && (subfunction != 0xA1)) {
                cpu.operand = 0xE000;
                cpu.operand += subfunction;
                cpu.executeInstruction(0xE);
                assertEquals("Operation " + String.format("%04X", cpu.operand) + " not supported", cpu.getOpShortDesc());
            }
        }
    }
    
    @Test
    public void testMoveDelayTimerIntoRegisterInvoked() {
        cpuSpy.operand = 0x07;
        cpuSpy.executeInstruction(0xF);
        verify(cpuSpy).moveDelayTimerIntoRegister();
    }
    
    @Test
    public void testWaitForKeypressInvoked() {
        cpuSpy.operand = 0x0A;
        cpuSpy.executeInstruction(0xF);
        verify(cpuSpy).waitForKeypress();
    }
    
    @Test
    public void testMoveRegisterIntoDelayRegisterInvoked() {
        cpuSpy.operand = 0x15;
        cpuSpy.executeInstruction(0xF);
        verify(cpuSpy).moveRegisterIntoDelayRegister();
    }
    
    @Test
    public void testMoveRegisterIntoSoundRegisterInvoked() {
        cpuSpy.operand = 0x18;
        cpuSpy.executeInstruction(0xF);
        verify(cpuSpy).moveRegisterIntoSoundRegister();
    }
    
    @Test
    public void testAddRegisterIntoIndexInvoked() {
        cpuSpy.operand = 0x1E;
        cpuSpy.executeInstruction(0xF);
        verify(cpuSpy).addRegisterIntoIndex();
    }
    
    @Test
    public void testLoadIndexWithSpriteInvoked() {
        cpuSpy.operand = 0x29;
        cpuSpy.executeInstruction(0xF);
        verify(cpuSpy).loadIndexWithSprite();
    }

    @Test
    public void testLoadIndexWithExtendedSpriteInvoked() {
        cpuSpy.operand = 0x30;
        cpuSpy.executeInstruction(0xF);
        verify(cpuSpy).loadIndexWithExtendedSprite();
    }

    @Test
    public void testStoreRegisterInRPLInvoked() {
        cpuSpy.operand = 0x75;
        cpuSpy.executeInstruction(0xF);
        verify(cpuSpy).storeRegistersInRPL();
    }

    @Test
    public void testReadRegistersFromRPLInvoked() {
        cpuSpy.operand = 0x85;
        cpuSpy.executeInstruction(0xF);
        verify(cpuSpy).readRegistersFromRPL();
    }

    @Test
    public void testStoreBCDInMemoryInvoked() {
        cpuSpy.operand = 0x33;
        cpuSpy.executeInstruction(0xF);
        verify(cpuSpy).storeBCDInMemory();
    }
    
    @Test
    public void testStoreRegistersInMemoryInvoked() {
        cpuSpy.operand = 0x55;
        cpuSpy.executeInstruction(0xF);
        verify(cpuSpy).storeRegistersInMemory();
    }
    
    @Test
    public void testReadRegistersFromMemoryInvoked() {
        cpuSpy.operand = 0x65;
        cpuSpy.executeInstruction(0xF);
        verify(cpuSpy).readRegistersFromMemory();
    }
    
    @Test
    public void testIOSubroutinesNotSupported() {
        for (int subfunction = 0; subfunction < 0xFF; subfunction++) {
            if ((subfunction != 0x07) && (subfunction != 0x0A) &&
                    (subfunction != 0x15) && (subfunction != 0x18) &&
                    (subfunction != 0x1E) && (subfunction != 0x29) &&
                    (subfunction != 0x3A) && (subfunction != 0x01) &&
                    (subfunction != 0x33) && (subfunction != 0x55) &&
                    (subfunction != 0x65) && (subfunction != 0x30) &&
                    (subfunction != 0x75) && (subfunction != 0x85) &&
                    (subfunction != 0x00)
            ) {
                if (((subfunction & 0xF) != 0x2) && ((subfunction & 0xF) != 0x3)) {
                    cpu.operand = 0xF000;
                    cpu.operand += subfunction;
                    cpu.executeInstruction(0xF);
                    assertEquals("Operation " + String.format("%04X", cpu.operand) + " not supported", cpu.getOpShortDesc());
                }
            }
        }
    }

    @Test
    public void testStoreSubsetOneTwo() {
        cpu.v[1] = 5;
        cpu.v[2] = 6;
        cpu.index = 0x5000;
        cpu.operand = 0x5122;
        cpu.storeSubsetOfRegistersInMemory();
        assertEquals(5, memory.read(0x5000));
        assertEquals(6, memory.read(0x5001));
    }

    @Test
    public void testStoreSubsetOneOne() {
        cpu.v[1] = 5;
        cpu.v[2] = 6;
        cpu.index = 0x5000;
        cpu.operand = 0x5112;
        cpu.storeSubsetOfRegistersInMemory();
        assertEquals(5, memory.read(0x5000));
        assertEquals(0, memory.read(0x5001));
    }

    @Test
    public void testStoreSubsetThreeOne() {
        cpu.v[1] = 5;
        cpu.v[2] = 6;
        cpu.v[3] = 7;
        cpu.index = 0x5000;
        cpu.operand = 0x5312;
        cpu.storeSubsetOfRegistersInMemory();
        assertEquals(7, memory.read(0x5000));
        assertEquals(6, memory.read(0x5001));
        assertEquals(5, memory.read(0x5002));
    }

    @Test
    public void testStoreSubsetIntegration() {
        cpu.v[1] = 5;
        cpu.v[2] = 6;
        cpu.v[3] = 7;
        cpu.index = 0x5000;
        memory.write(0x53, 0x0200);
        memory.write(0x12, 0x0201);
        cpu.fetchIncrementExecute();
        assertEquals(7, memory.read(0x5000));
        assertEquals(6, memory.read(0x5001));
        assertEquals(5, memory.read(0x5002));
    }

    @Test
    public void testLoadSubsetOneTwo() {
        cpu.v[1] = 5;
        cpu.v[2] = 6;
        cpu.index = 0x5000;
        cpu.operand = 0x5123;
        memory.write(7, 0x5000);
        memory.write(8, 0x5001);
        cpu.loadSubsetOfRegistersFromMemory();
        assertEquals(7, cpu.v[1]);
        assertEquals(8, cpu.v[2]);
    }

    @Test
    public void testLoadSubsetOneOne() {
        cpu.v[1] = 5;
        cpu.v[2] = 6;
        cpu.index = 0x5000;
        cpu.operand = 0x5113;
        memory.write(7, 0x5000);
        memory.write(8, 0x5001);
        cpu.loadSubsetOfRegistersFromMemory();
        assertEquals(7, cpu.v[1]);
        assertEquals(6, cpu.v[2]);
    }

    @Test
    public void testLoadSubsetThreeOne() {
        cpu.v[1] = 5;
        cpu.v[2] = 6;
        cpu.v[3] = 7;
        cpu.index = 0x5000;
        cpu.operand = 0x5313;
        memory.write(8, 0x5000);
        memory.write(9, 0x5001);
        memory.write(10, 0x5002);
        cpu.loadSubsetOfRegistersFromMemory();
        assertEquals(10, cpu.v[1]);
        assertEquals(9, cpu.v[2]);
        assertEquals(8, cpu.v[3]);
    }

    @Test
    public void testLoadSubsetIntegration() {
        cpu.v[1] = 5;
        cpu.v[2] = 6;
        cpu.v[3] = 7;
        cpu.index = 0x5000;
        memory.write(0x53, 0x0200);
        memory.write(0x13, 0x0201);
        memory.write(8, 0x5000);
        memory.write(9, 0x5001);
        memory.write(10, 0x5002);
        cpu.fetchIncrementExecute();
        assertEquals(10, cpu.v[1]);
        assertEquals(9, cpu.v[2]);
        assertEquals(8, cpu.v[3]);
    }

    @Test
    public void testIndexLoadLong() {
        cpu.index = 0x5000;
        memory.write(0x12, 0x0200);
        memory.write(0x34, 0x0201);
        cpu.indexLoadLong();
        assertEquals(0x1234, cpu.index);
        assertEquals(0x0202, cpu.pc);
    }

    @Test
    public void testIndexLoadLongIntegration() {
        cpu.index = 0x5000;
        memory.write(0xF0, 0x0200);
        memory.write(0x00, 0x0201);
        memory.write(0x12, 0x0202);
        memory.write(0x34, 0x0203);
        cpu.fetchIncrementExecute();
        assertEquals(0x1234, cpu.index);
        assertEquals(0x0204, cpu.pc);
    }

    @Test
    public void testScreenSubroutinesNotSupported() {
        for (int subfunction = 0; subfunction <= 0xFF; subfunction++) {
            if ((subfunction != 0xE0) && (subfunction != 0xEE) &&
                    (subfunction != 0xFE) && (subfunction != 0xFF) &&
                    (subfunction != 0xFB) && (subfunction != 0xFC) &&
                    (subfunction != 0xFD) && ((subfunction & 0xF0) != 0xC0) &&
                    ((subfunction & 0xF0) != 0xD0))
            {
                cpu.operand = 0x0000;
                cpu.operand += subfunction;
                cpu.executeInstruction(0x0);
                assertEquals("Operation " + String.format("%04X", cpu.operand) + " not supported", cpu.getOpShortDesc());
            }
        }
    }
    
    @Test
    public void testScreenClearInvoked() {
        cpu.operand = 0xE0;
        cpu.executeInstruction(0x0);
        verify(screenMock, times(2)).clearScreen(1);
        assertEquals("CLS", cpu.getOpShortDesc());
    }

    @Test
    public void testDrawSpriteNormalBitplane1IntegrationCorrect() throws IOException, FontFormatException {
        setUpCanvas();
        cpu = new CentralProcessingUnit(memory, keyboardMock, screen);

        memory.write(0xD0, 0x0200);
        memory.write(0x01, 0x0201);
        memory.write(0xAA, 0x5000);
        cpu.index = 0x5000;
        cpu.bitplane = 1;
        cpu.fetchIncrementExecute();

        // First bitplane pattern
        assertTrue(screen.getPixel(0, 0, 1));
        assertFalse(screen.getPixel(1, 0, 1));
        assertTrue(screen.getPixel(2, 0, 1));
        assertFalse(screen.getPixel(3, 0, 1));
        assertTrue(screen.getPixel(4, 0, 1));
        assertFalse(screen.getPixel(5, 0, 1));
        assertTrue(screen.getPixel(6, 0, 1));
        assertFalse(screen.getPixel(7, 0, 1));

        // Second bitplane pattern
        assertFalse(screen.getPixel(0, 0, 2));
        assertFalse(screen.getPixel(1, 0, 2));
        assertFalse(screen.getPixel(2, 0, 2));
        assertFalse(screen.getPixel(3, 0, 2));
        assertFalse(screen.getPixel(4, 0, 2));
        assertFalse(screen.getPixel(5, 0, 2));
        assertFalse(screen.getPixel(6, 0, 2));
        assertFalse(screen.getPixel(7, 0, 2));
    }

    @Test
    public void testDrawSpriteExtendedBitplane1IntegrationCorrect() throws IOException, FontFormatException {
        setUpCanvas();
        cpu = new CentralProcessingUnit(memory, keyboardMock, screen);
        cpu.enableExtendedMode();

        memory.write(0xD0, 0x0200);
        memory.write(0x00, 0x0201);
        memory.write(0xAA, 0x5000);
        memory.write(0x55, 0x5001);
        memory.write(0xAA, 0x5002);
        memory.write(0x55, 0x5003);
        memory.write(0xAA, 0x5004);
        memory.write(0x55, 0x5005);
        memory.write(0xAA, 0x5006);
        memory.write(0x55, 0x5007);
        memory.write(0xAA, 0x5008);
        memory.write(0x55, 0x5009);
        memory.write(0xAA, 0x500A);
        memory.write(0x55, 0x500B);
        memory.write(0xAA, 0x500C);
        memory.write(0x55, 0x500D);
        memory.write(0xAA, 0x500E);
        memory.write(0x55, 0x500F);

        cpu.index = 0x5000;
        cpu.bitplane = 1;
        cpu.fetchIncrementExecute();

        // First bitplane pattern
        assertTrue(screen.getPixel(0, 0, 1));
        assertFalse(screen.getPixel(1, 0, 1));
        assertTrue(screen.getPixel(2, 0, 1));
        assertFalse(screen.getPixel(3, 0, 1));
        assertTrue(screen.getPixel(4, 0, 1));
        assertFalse(screen.getPixel(5, 0, 1));
        assertTrue(screen.getPixel(6, 0, 1));
        assertFalse(screen.getPixel(7, 0, 1));
        assertFalse(screen.getPixel(8, 0, 1));
        assertTrue(screen.getPixel(9, 0, 1));
        assertFalse(screen.getPixel(10, 0, 1));
        assertTrue(screen.getPixel(11, 0, 1));
        assertFalse(screen.getPixel(12, 0, 1));
        assertTrue(screen.getPixel(13, 0, 1));
        assertFalse(screen.getPixel(14, 0, 1));
        assertTrue(screen.getPixel(15, 0, 1));

        // Second bitplane pattern
        assertFalse(screen.getPixel(0, 0, 2));
        assertFalse(screen.getPixel(1, 0, 2));
        assertFalse(screen.getPixel(2, 0, 2));
        assertFalse(screen.getPixel(3, 0, 2));
        assertFalse(screen.getPixel(4, 0, 2));
        assertFalse(screen.getPixel(5, 0, 2));
        assertFalse(screen.getPixel(6, 0, 2));
        assertFalse(screen.getPixel(7, 0, 2));
        assertFalse(screen.getPixel(8, 0, 2));
        assertFalse(screen.getPixel(9, 0, 2));
        assertFalse(screen.getPixel(10, 0, 2));
        assertFalse(screen.getPixel(11, 0, 2));
        assertFalse(screen.getPixel(12, 0, 2));
        assertFalse(screen.getPixel(13, 0, 2));
        assertFalse(screen.getPixel(14, 0, 2));
        assertFalse(screen.getPixel(15, 0, 2));
    }

    @Test
    public void testDrawSpriteNormalBitplane2IntegrationCorrect() throws IOException, FontFormatException {
        setUpCanvas();
        cpu = new CentralProcessingUnit(memory, keyboardMock, screen);

        memory.write(0xD0, 0x0200);
        memory.write(0x01, 0x0201);
        memory.write(0x55, 0x5000);
        cpu.index = 0x5000;
        cpu.bitplane = 2;
        cpu.fetchIncrementExecute();

        // First bitplane pattern
        assertFalse(screen.getPixel(0, 0, 1));
        assertFalse(screen.getPixel(1, 0, 1));
        assertFalse(screen.getPixel(2, 0, 1));
        assertFalse(screen.getPixel(3, 0, 1));
        assertFalse(screen.getPixel(4, 0, 1));
        assertFalse(screen.getPixel(5, 0, 1));
        assertFalse(screen.getPixel(6, 0, 1));
        assertFalse(screen.getPixel(7, 0, 1));

        // Second bitplane pattern
        assertFalse(screen.getPixel(0, 0, 2));
        assertTrue(screen.getPixel(1, 0, 2));
        assertFalse(screen.getPixel(2, 0, 2));
        assertTrue(screen.getPixel(3, 0, 2));
        assertFalse(screen.getPixel(4, 0, 2));
        assertTrue(screen.getPixel(5, 0, 2));
        assertFalse(screen.getPixel(6, 0, 2));
        assertTrue(screen.getPixel(7, 0, 2));
    }

    @Test
    public void testDrawSpriteExtendedBitplane2IntegrationCorrect() throws IOException, FontFormatException {
        setUpCanvas();
        cpu = new CentralProcessingUnit(memory, keyboardMock, screen);
        cpu.enableExtendedMode();

        memory.write(0xD0, 0x0200);
        memory.write(0x00, 0x0201);
        memory.write(0xAA, 0x5000);
        memory.write(0x55, 0x5001);
        memory.write(0xAA, 0x5002);
        memory.write(0x55, 0x5003);
        memory.write(0xAA, 0x5004);
        memory.write(0x55, 0x5005);
        memory.write(0xAA, 0x5006);
        memory.write(0x55, 0x5007);
        memory.write(0xAA, 0x5008);
        memory.write(0x55, 0x5009);
        memory.write(0xAA, 0x500A);
        memory.write(0x55, 0x500B);
        memory.write(0xAA, 0x500C);
        memory.write(0x55, 0x500D);
        memory.write(0xAA, 0x500E);
        memory.write(0x55, 0x500F);

        cpu.index = 0x5000;
        cpu.bitplane = 2;
        cpu.fetchIncrementExecute();

        // Second bitplane pattern
        assertTrue(screen.getPixel(0, 0, 2));
        assertFalse(screen.getPixel(1, 0, 2));
        assertTrue(screen.getPixel(2, 0, 2));
        assertFalse(screen.getPixel(3, 0, 2));
        assertTrue(screen.getPixel(4, 0, 2));
        assertFalse(screen.getPixel(5, 0, 2));
        assertTrue(screen.getPixel(6, 0, 2));
        assertFalse(screen.getPixel(7, 0, 2));
        assertFalse(screen.getPixel(8, 0, 2));
        assertTrue(screen.getPixel(9, 0, 2));
        assertFalse(screen.getPixel(10, 0, 2));
        assertTrue(screen.getPixel(11, 0, 2));
        assertFalse(screen.getPixel(12, 0, 2));
        assertTrue(screen.getPixel(13, 0, 2));
        assertFalse(screen.getPixel(14, 0, 2));
        assertTrue(screen.getPixel(15, 0, 2));

        // First bitplane pattern
        assertFalse(screen.getPixel(0, 0, 1));
        assertFalse(screen.getPixel(1, 0, 1));
        assertFalse(screen.getPixel(2, 0, 1));
        assertFalse(screen.getPixel(3, 0, 1));
        assertFalse(screen.getPixel(4, 0, 1));
        assertFalse(screen.getPixel(5, 0, 1));
        assertFalse(screen.getPixel(6, 0, 1));
        assertFalse(screen.getPixel(7, 0, 1));
        assertFalse(screen.getPixel(8, 0, 1));
        assertFalse(screen.getPixel(9, 0, 1));
        assertFalse(screen.getPixel(10, 0, 1));
        assertFalse(screen.getPixel(11, 0, 1));
        assertFalse(screen.getPixel(12, 0, 1));
        assertFalse(screen.getPixel(13, 0, 1));
        assertFalse(screen.getPixel(14, 0, 1));
        assertFalse(screen.getPixel(15, 0, 1));
    }

    @Test
    public void testDrawSpriteNormalBitplane3IntegrationCorrect() throws IOException, FontFormatException {
        setUpCanvas();
        cpu = new CentralProcessingUnit(memory, keyboardMock, screen);

        memory.write(0xD0, 0x0200);
        memory.write(0x01, 0x0201);
        memory.write(0xAA, 0x5000);
        memory.write(0x55, 0x5001);

        cpu.index = 0x5000;
        cpu.bitplane = 3;
        cpu.fetchIncrementExecute();

        // First bitplane pattern
        assertTrue(screen.getPixel(0, 0, 1));
        assertFalse(screen.getPixel(1, 0, 1));
        assertTrue(screen.getPixel(2, 0, 1));
        assertFalse(screen.getPixel(3, 0, 1));
        assertTrue(screen.getPixel(4, 0, 1));
        assertFalse(screen.getPixel(5, 0, 1));
        assertTrue(screen.getPixel(6, 0, 1));
        assertFalse(screen.getPixel(7, 0, 1));

        // Second bitplane pattern
        assertFalse(screen.getPixel(0, 0, 2));
        assertTrue(screen.getPixel(1, 0, 2));
        assertFalse(screen.getPixel(2, 0, 2));
        assertTrue(screen.getPixel(3, 0, 2));
        assertFalse(screen.getPixel(4, 0, 2));
        assertTrue(screen.getPixel(5, 0, 2));
        assertFalse(screen.getPixel(6, 0, 2));
        assertTrue(screen.getPixel(7, 0, 2));
    }

    @Test
    public void testDrawSpriteExtendedBitplane3IntegrationCorrect() throws IOException, FontFormatException {
        setUpCanvas();
        cpu = new CentralProcessingUnit(memory, keyboardMock, screen);
        cpu.enableExtendedMode();

        memory.write(0xD0, 0x0200);
        memory.write(0x00, 0x0201);

        memory.write(0xAA, 0x5000);
        memory.write(0x55, 0x5001);
        memory.write(0xAA, 0x5002);
        memory.write(0x55, 0x5003);
        memory.write(0xAA, 0x5004);
        memory.write(0x55, 0x5005);
        memory.write(0xAA, 0x5006);
        memory.write(0x55, 0x5007);
        memory.write(0xAA, 0x5008);
        memory.write(0x55, 0x5009);
        memory.write(0xAA, 0x500A);
        memory.write(0x55, 0x500B);
        memory.write(0xAA, 0x500C);
        memory.write(0x55, 0x500D);
        memory.write(0xAA, 0x500E);
        memory.write(0x55, 0x500F);

        memory.write(0x55, 0x5010);
        memory.write(0xAA, 0x5011);
        memory.write(0x55, 0x5012);
        memory.write(0xAA, 0x5013);
        memory.write(0x55, 0x5014);
        memory.write(0xAA, 0x5015);
        memory.write(0x55, 0x5016);
        memory.write(0xAA, 0x5017);
        memory.write(0x55, 0x5018);
        memory.write(0xAA, 0x5019);
        memory.write(0x55, 0x501A);
        memory.write(0xAA, 0x501B);
        memory.write(0x55, 0x501C);
        memory.write(0xAA, 0x501D);
        memory.write(0x55, 0x501E);
        memory.write(0xAA, 0x501F);

        memory.write(0x55, 0x5020);
        memory.write(0xAA, 0x5021);
        memory.write(0x55, 0x5022);
        memory.write(0xAA, 0x5023);
        memory.write(0x55, 0x5024);
        memory.write(0xAA, 0x5025);
        memory.write(0x55, 0x5026);
        memory.write(0xAA, 0x5027);
        memory.write(0x55, 0x5028);
        memory.write(0xAA, 0x5029);
        memory.write(0x55, 0x502A);
        memory.write(0xAA, 0x502B);
        memory.write(0x55, 0x502C);
        memory.write(0xAA, 0x502D);
        memory.write(0x55, 0x502E);
        memory.write(0xAA, 0x502F);

        memory.write(0xAA, 0x5030);
        memory.write(0x55, 0x5031);
        memory.write(0xAA, 0x5032);
        memory.write(0x55, 0x5033);
        memory.write(0xAA, 0x5034);
        memory.write(0x55, 0x5035);
        memory.write(0xAA, 0x5036);
        memory.write(0x55, 0x5037);
        memory.write(0xAA, 0x5038);
        memory.write(0x55, 0x5039);
        memory.write(0xAA, 0x503A);
        memory.write(0x55, 0x503B);
        memory.write(0xAA, 0x503C);
        memory.write(0x55, 0x503D);
        memory.write(0xAA, 0x503E);
        memory.write(0x55, 0x503F);

        cpu.index = 0x5000;
        cpu.bitplane = 3;
        cpu.fetchIncrementExecute();

        // Second bitplane pattern
        assertTrue(screen.getPixel(0, 0, 1));
        assertFalse(screen.getPixel(1, 0, 1));
        assertTrue(screen.getPixel(2, 0, 1));
        assertFalse(screen.getPixel(3, 0, 1));
        assertTrue(screen.getPixel(4, 0, 1));
        assertFalse(screen.getPixel(5, 0, 1));
        assertTrue(screen.getPixel(6, 0, 1));
        assertFalse(screen.getPixel(7, 0, 1));
        assertFalse(screen.getPixel(8, 0, 1));
        assertTrue(screen.getPixel(9, 0, 1));
        assertFalse(screen.getPixel(10, 0, 1));
        assertTrue(screen.getPixel(11, 0, 1));
        assertFalse(screen.getPixel(12, 0, 1));
        assertTrue(screen.getPixel(13, 0, 1));
        assertFalse(screen.getPixel(14, 0, 1));
        assertTrue(screen.getPixel(15, 0, 1));

        // First bitplane pattern
        assertFalse(screen.getPixel(0, 0, 2));
        assertTrue(screen.getPixel(1, 0, 2));
        assertFalse(screen.getPixel(2, 0, 2));
        assertTrue(screen.getPixel(3, 0, 2));
        assertFalse(screen.getPixel(4, 0, 2));
        assertTrue(screen.getPixel(5, 0, 2));
        assertFalse(screen.getPixel(6, 0, 2));
        assertTrue(screen.getPixel(7, 0, 2));
        assertTrue(screen.getPixel(8, 0, 2));
        assertFalse(screen.getPixel(9, 0, 2));
        assertTrue(screen.getPixel(10, 0, 2));
        assertFalse(screen.getPixel(11, 0, 2));
        assertTrue(screen.getPixel(12, 0, 2));
        assertFalse(screen.getPixel(13, 0, 2));
        assertTrue(screen.getPixel(14, 0, 2));
        assertFalse(screen.getPixel(15, 0, 2));
    }

    @Test
    public void testKillInvoked() {
        cpu.operand = 0xFD;
        cpu.executeInstruction(0x0);
        assertFalse(cpu.isAlive());
    }

    @Test
    public void testEnableExtendedScreenMode() {
        cpu.operand = 0xFF;
        cpu.executeInstruction(0x0);
        verify(screenMock, times(1)).setExtendedScreenMode();
        assertEquals(MODE_EXTENDED, cpu.mode);
    }

    @Test
    public void testDisableExtendedScreenMode() {
        cpu.operand = 0xFE;
        cpu.executeInstruction(0x0);
        verify(screenMock, times(1)).setNormalScreenMode();
        assertEquals(CentralProcessingUnit.MODE_NORMAL, cpu.mode);
    }

    @Test
    public void testStoreRegistersInRPL() {
        cpu.operand = 0xF00;
        for (int regNumber = 0; regNumber < 16; regNumber++) {
            cpu.v[regNumber] = (short) regNumber;
        }
        cpu.storeRegistersInRPL();
        for (int regNumber = 0; regNumber < 16; regNumber++) {
            assertEquals(regNumber, cpu.rpl[regNumber]);
        }
    }

    @Test
    public void testReadRegistersFromRPL() {
        cpu.operand = 0xF00;
        for (int regNumber = 0; regNumber < 16; regNumber++) {
            cpu.rpl[regNumber] = (short) regNumber;
        }
        cpu.readRegistersFromRPL();
        cpu.readRegistersFromRPL();
        for (int regNumber = 0; regNumber < 16; regNumber++) {
            assertEquals(regNumber, cpu.v[regNumber]);
        }
    }

    @Test
    public void testScrollDownCalledCorrectOperands() {
        cpu.operand = 0xC8;
        cpu.executeInstruction(0x0);
        verify(screenMock, times(1)).scrollDown(8, 1);
        assertEquals("Scroll Down 8", cpu.lastOpDesc);
    }

    @Test
    public void testScrollUpCalledCorrectOperands() {
        cpu.operand = 0xD8;
        cpu.executeInstruction(0x0);
        verify(screenMock, times(1)).scrollUp(8, 1);
        assertEquals("Scroll Up 8", cpu.lastOpDesc);
    }

    @Test
    public void testScrollLeft() {
        cpu.operand = 0xFC;
        cpu.executeInstruction(0x0);
        verify(screenMock, times(1)).scrollLeft(1);
        assertEquals("Scroll Left", cpu.lastOpDesc);
    }

    @Test
    public void testScrollRight() {
        cpu.operand = 0xFB;
        cpu.executeInstruction(0x0);
        verify(screenMock, times(1)).scrollRight(1);
        assertEquals("Scroll Right", cpu.lastOpDesc);
    }

    @Test
    public void testPitchInit64() {
        assertEquals(64, cpu.pitch);
        assertEquals(4000.0, cpu.playbackRate, 0.001);
    }

    @Test
    public void testLoadPitch() {
        cpu.v[1] = (short) 112;
        cpu.operand = 0xF13A;
        cpu.loadPitch();
        assertEquals(112, cpu.pitch);
        assertEquals(8000.0, cpu.playbackRate, 0.001);
    }

    @Test
    public void testLoadPitchIntegration() {
        cpu.v[1] = 112;
        memory.write(0xF1, 0x0200);
        memory.write(0x3A, 0x0201);
        cpu.fetchIncrementExecute();
        assertEquals(112, cpu.pitch);
        assertEquals(8000.0, cpu.playbackRate, 0.001);
    }

    @Test
    public void testBitplane1SetOnInit() {
        assertEquals(1, cpu.bitplane);
    }

    @Test
    public void testSetBitplane() {
        cpu.operand = 0xF201;
        cpu.setBitplane();
        assertEquals(2, cpu.bitplane);
    }

    @Test
    public void testSetBitplaneIntegration() {
        memory.write(0xF2, 0x0200);
        memory.write(0x01, 0x0201);
        cpu.fetchIncrementExecute();
        assertEquals(2, cpu.bitplane);
    }

    @Test
    public void testIsAwaitingKeypressFalseAtInit() {
        assertFalse(cpu.isAwaitingKeypress());
    }

    @Test
    public void testWaitForKeypressSetsAwaitingKeypress() {
        int register = 1;
        cpu.operand = register << 8;
        cpu.waitForKeypress();
        assertTrue(cpu.isAwaitingKeypress());
    }
    
    @Test
    public void testCPUStatusLine1() {
        cpu.index = 0x10;
        cpu.delay = 0x9;
        cpu.sound = 0x3;
        cpu.pc = 0x1234;
        cpu.operand = 0x9876;
        cpu.lastOpDesc = "none";
        String expected = "I:0010 DT:09 ST:03 PC:1234 9876 none";
        assertEquals(expected, cpu.cpuStatusLine1());
    }
    
    @Test
    public void testCPUStatusLine2() {
        cpu.v[0] = 0x10;
        cpu.v[1] = 0x11;
        cpu.v[2] = 0x12;
        cpu.v[3] = 0x13;
        cpu.v[4] = 0x14;
        cpu.v[5] = 0x15;
        cpu.v[6] = 0x16;
        cpu.v[7] = 0x17;
        String expected = "V0:10 V1:11 V2:12 V3:13 V4:14 V5:15 V6:16 V7:17";
        assertEquals(expected, cpu.cpuStatusLine2());
    }
    
    @Test
    public void testCPUStatusLine3() {
        cpu.v[8] = 0x18;
        cpu.v[9] = 0x19;
        cpu.v[10] = 0x20;
        cpu.v[11] = 0x21;
        cpu.v[12] = 0x22;
        cpu.v[13] = 0x23;
        cpu.v[14] = 0x24;
        cpu.v[15] = 0x25;
        String expected = "V8:18 V9:19 VA:20 VB:21 VC:22 VD:23 VE:24 VF:25";
        assertEquals(expected, cpu.cpuStatusLine3());
    }
    
    @Test
    public void testFetchIncrementExecute() {
        cpu.pc = 0;
        memory.write(0x02, 0);
        memory.write(0x34, 1);
        cpu.fetchIncrementExecute();
        assertEquals(2, cpu.pc);
        assertEquals(0x0234, cpu.operand);
    }
    
    @Test
    public void testExecuteInstructionDoesNotFireOnNegativeOpcode() {
        cpu.executeInstruction(-1);
        assertEquals("Operation 0000 not supported", cpu.lastOpDesc);
    }
    
    @Test
    public void testDrawSpriteDrawsCorrectPattern() throws FontFormatException, IOException {
        setUpCanvas();
        cpu = new CentralProcessingUnit(memory, keyboardMock, screen);
        cpu.index = 0x200;
        memory.write(0xAA, 0x200);
        cpu.v[0] = 0;
        cpu.v[1] = 0;
        cpu.operand = 0x11;
        cpu.drawSprite();
        assertTrue(screen.getPixel(0, 0, 1));
        assertFalse(screen.getPixel(1, 0, 1));
        assertTrue(screen.getPixel(2, 0, 1));
        assertFalse(screen.getPixel(3, 0, 1));
        assertTrue(screen.getPixel(4, 0, 1));
        assertFalse(screen.getPixel(5, 0, 1));
        assertTrue(screen.getPixel(6, 0, 1));
        assertFalse(screen.getPixel(7, 0, 1));
        tearDownCanvas();
    }

    @Test
    public void testDrawSpriteExtendedDrawsCorrectPattern() throws FontFormatException, IOException {
        setUpCanvas();
        cpu = new CentralProcessingUnit(memory, keyboardMock, screen);
        cpu.index = 0x200;
        for (short x = 0; x < 32; x++) {
            memory.write(0xAA, cpu.index + x);
        }
        cpu.v[0] = 0;
        cpu.v[1] = 0;
        cpu.operand = 0x10;
        cpu.enableExtendedMode();
        cpu.drawSprite();
        for (int byteOffset = 0; byteOffset < 16; byteOffset++) {
            assertTrue(screen.getPixel(0, byteOffset, 1));
            assertFalse(screen.getPixel(1, byteOffset, 1));
            assertTrue(screen.getPixel(2, byteOffset, 1));
            assertFalse(screen.getPixel(3, byteOffset, 1));
            assertTrue(screen.getPixel(4, byteOffset, 1));
            assertFalse(screen.getPixel(5, byteOffset, 1));
            assertTrue(screen.getPixel(6, byteOffset, 1));
            assertFalse(screen.getPixel(7, byteOffset, 1));
            assertTrue(screen.getPixel(8, byteOffset, 1));
            assertFalse(screen.getPixel(9, byteOffset, 1));
            assertTrue(screen.getPixel(10, byteOffset, 1));
            assertFalse(screen.getPixel(11, byteOffset, 1));
            assertTrue(screen.getPixel(12, byteOffset, 1));
            assertFalse(screen.getPixel(13, byteOffset, 1));
            assertTrue(screen.getPixel(14, byteOffset, 1));
            assertFalse(screen.getPixel(15, byteOffset, 1));
        }
        tearDownCanvas();
    }

    @Test
    public void testDrawSpriteOverTopSpriteTurnsOff() throws FontFormatException, IOException {
        setUpCanvas();
        cpu = new CentralProcessingUnit(memory, keyboardMock, screen);
        cpu.index = 0x200;
        memory.write(0xFF, 0x200);
        cpu.v[0] = 0;
        cpu.v[1] = 0;
        cpu.operand = 0x11;
        cpu.drawSprite();
        cpu.drawSprite();
        assertEquals(1, cpu.v[0xF]);
        assertFalse(screen.getPixel(0, 0, 1));
        assertFalse(screen.getPixel(1, 0, 1));
        assertFalse(screen.getPixel(2, 0, 1));
        assertFalse(screen.getPixel(3, 0, 1));
        assertFalse(screen.getPixel(4, 0, 1));
        assertFalse(screen.getPixel(5, 0, 1));
        assertFalse(screen.getPixel(6, 0, 1));
        assertFalse(screen.getPixel(7, 0, 1));
        tearDownCanvas();
    }

    @Test
    public void testDrawSpriteExtendedOvertopSpriteTurnsOff() throws FontFormatException, IOException {
        setUpCanvas();
        cpu = new CentralProcessingUnit(memory, keyboardMock, screen);
        cpu.index = 0x200;
        for (short x = 0; x < 32; x++) {
            memory.write(0xAA, cpu.index + x);
        }
        cpu.v[0] = 0;
        cpu.v[1] = 0;
        cpu.operand = 0x10;
        cpu.enableExtendedMode();
        cpu.drawSprite();
        cpu.drawSprite();
        for (int byteOffset = 0; byteOffset < 16; byteOffset++) {
            assertFalse(screen.getPixel(0, byteOffset, 1));
            assertFalse(screen.getPixel(1, byteOffset, 1));
            assertFalse(screen.getPixel(2, byteOffset, 1));
            assertFalse(screen.getPixel(3, byteOffset, 1));
            assertFalse(screen.getPixel(4, byteOffset, 1));
            assertFalse(screen.getPixel(5, byteOffset, 1));
            assertFalse(screen.getPixel(6, byteOffset, 1));
            assertFalse(screen.getPixel(7, byteOffset, 1));
            assertFalse(screen.getPixel(8, byteOffset, 1));
            assertFalse(screen.getPixel(9, byteOffset, 1));
            assertFalse(screen.getPixel(10, byteOffset, 1));
            assertFalse(screen.getPixel(11, byteOffset, 1));
            assertFalse(screen.getPixel(12, byteOffset, 1));
            assertFalse(screen.getPixel(13, byteOffset, 1));
            assertFalse(screen.getPixel(14, byteOffset, 1));
            assertFalse(screen.getPixel(15, byteOffset, 1));
        }
        tearDownCanvas();
    }

    @Test
    public void testDrawNoSpriteOverTopSpriteLeavesOn() throws FontFormatException, IOException {
        setUpCanvas();
        cpu = new CentralProcessingUnit(memory, keyboardMock, screen);
        cpu.index = 0x200;
        memory.write(0xFF, 0x200);
        cpu.v[0] = 0;
        cpu.v[1] = 0;
        cpu.operand = 0x11;
        cpu.drawSprite();
        memory.write(0x00, 0x200);
        cpu.drawSprite();
        assertEquals(0, cpu.v[0xF]);
        assertTrue(screen.getPixel(0, 0, 1));
        assertTrue(screen.getPixel(1, 0, 1));
        assertTrue(screen.getPixel(2, 0, 1));
        assertTrue(screen.getPixel(3, 0, 1));
        assertTrue(screen.getPixel(4, 0, 1));
        assertTrue(screen.getPixel(5, 0, 1));
        assertTrue(screen.getPixel(6, 0, 1));
        assertTrue(screen.getPixel(7, 0, 1));
        tearDownCanvas();
    }
}
