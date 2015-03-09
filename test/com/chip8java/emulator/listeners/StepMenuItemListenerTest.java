/*
 * Copyright (C) 2013-2015 Craig Thomas
 * This project uses an MIT style license - see LICENSE for details.
 */
package com.chip8java.emulator.listeners;

import com.chip8java.emulator.*;
import org.junit.After;
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

    private Emulator mEmulator;
    private StepMenuItemListener mTraceMenuItemListener;
    private ItemEvent mMockItemEvent;

    @Before
    public void setUp() {
        mEmulator = new Emulator.Builder().build();
        JCheckBoxMenuItem mockTraceMenuItem = mock(JCheckBoxMenuItem.class);
        mTraceMenuItemListener = new StepMenuItemListener(mEmulator, mockTraceMenuItem);
        ButtonModel buttonModel = mock(ButtonModel.class);
        Mockito.when(buttonModel.isSelected()).thenReturn(true).thenReturn(false);
        AbstractButton button = mock(AbstractButton.class);
        Mockito.when(button.getModel()).thenReturn(buttonModel);
        mMockItemEvent = mock(ItemEvent.class);
        Mockito.when(mMockItemEvent.getSource()).thenReturn(button);
    }

    @After
    public void tearDown() {
        mEmulator.dispose();
    }

    @Test
    public void testCPUInStepModeWhenItemListenerTriggered() {
        mTraceMenuItemListener.itemStateChanged(mMockItemEvent);
        assertTrue(mEmulator.getStep());
    }

    @Test
    public void testCPUNotInStepModeWhenItemListenerTriggeredTwice() {
        mTraceMenuItemListener.itemStateChanged(mMockItemEvent);
        mTraceMenuItemListener.itemStateChanged(mMockItemEvent);
        assertFalse(mEmulator.getStep());
    }

    @Test
    public void testCPUStaysInTraceModeWhenStepModeTriggered() {
        mEmulator.setTrace(true);
        mTraceMenuItemListener.itemStateChanged(mMockItemEvent);
        assertTrue(mEmulator.getTrace());
        assertTrue(mEmulator.getStep());
    }

    @Test
    public void testCPUStaysInTraceModeWhenStepModeStopped() {
        mEmulator.setTrace(true);
        mTraceMenuItemListener.itemStateChanged(mMockItemEvent);
        mTraceMenuItemListener.itemStateChanged(mMockItemEvent);
        assertTrue(mEmulator.getTrace());
        assertFalse(mEmulator.getStep());
    }
}
