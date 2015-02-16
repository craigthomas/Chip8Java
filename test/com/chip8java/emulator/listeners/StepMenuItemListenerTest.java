/*
 * Copyright (C) 2013-2015 Craig Thomas
 * This project uses an MIT style license - see LICENSE for details.
 */
package com.chip8java.emulator.listeners;

import com.chip8java.emulator.CentralProcessingUnit;
import com.chip8java.emulator.Keyboard;
import com.chip8java.emulator.Memory;
import com.chip8java.emulator.Screen;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.swing.*;
import java.awt.event.ItemEvent;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Tests for the StepMenuItemListener.
 */
public class StepMenuItemListenerTest {

    private Memory mMemoryMock;
    private Keyboard mKeyboardMock;
    private Screen mScreenMock;
    private CentralProcessingUnit mCPU;
    private StepMenuItemListener mTraceMenuItemListener;
    private ItemEvent mMockItemEvent;

    @Before
    public void setUp() {
        mMemoryMock = mock(Memory.class);
        mKeyboardMock = mock(Keyboard.class);
        mScreenMock = mock(Screen.class);
        mCPU = new CentralProcessingUnit(mMemoryMock, mKeyboardMock);
        mCPU.setScreen(mScreenMock);
        JCheckBoxMenuItem mockTraceMenuItem = mock(JCheckBoxMenuItem.class);
        mTraceMenuItemListener = new StepMenuItemListener(mCPU, mockTraceMenuItem);
        ButtonModel buttonModel = mock(ButtonModel.class);
        Mockito.when(buttonModel.isSelected()).thenReturn(true).thenReturn(false);
        AbstractButton button = mock(AbstractButton.class);
        Mockito.when(button.getModel()).thenReturn(buttonModel);
        mMockItemEvent = mock(ItemEvent.class);
        Mockito.when(mMockItemEvent.getSource()).thenReturn(button);
    }

    @Test
    public void testCPUInStepModeWhenItemListenerTriggered() {
        mTraceMenuItemListener.itemStateChanged(mMockItemEvent);
        assertTrue(mCPU.getStep());
    }

    @Test
    public void testCPUNotInStepModeWhenItemListenerTriggeredTwice() {
        mTraceMenuItemListener.itemStateChanged(mMockItemEvent);
        mTraceMenuItemListener.itemStateChanged(mMockItemEvent);
        assertFalse(mCPU.getStep());
    }

    @Test
    public void testCPUStaysInTraceModeWhenStepModeTriggered() {
        mCPU.setTrace(true);
        mTraceMenuItemListener.itemStateChanged(mMockItemEvent);
        assertTrue(mCPU.getTrace());
        assertTrue(mCPU.getStep());
    }

    @Test
    public void testCPUStaysInTraceModeWhenStepModeStopped() {
        mCPU.setTrace(true);
        mTraceMenuItemListener.itemStateChanged(mMockItemEvent);
        mTraceMenuItemListener.itemStateChanged(mMockItemEvent);
        assertTrue(mCPU.getTrace());
        assertFalse(mCPU.getStep());
    }
}
