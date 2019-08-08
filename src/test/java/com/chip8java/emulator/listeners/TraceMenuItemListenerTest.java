/*
 * Copyright (C) 2013-2018 Craig Thomas
 * This project uses an MIT style license - see LICENSE for details.
 */
package com.chip8java.emulator.listeners;

import com.chip8java.emulator.components.Emulator;
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
 * Tests for the TraceMenuItemListenerTest.
 */
public class TraceMenuItemListenerTest
{
    private Emulator emulator;
    private TraceMenuItemListener traceMenuItemListener;
    private ItemEvent mockItemEvent;

    @Before
    public void setUp() {
        emulator = new Emulator();
        traceMenuItemListener = new TraceMenuItemListener(emulator);
        ButtonModel buttonModel = mock(ButtonModel.class);
        Mockito.when(buttonModel.isSelected()).thenReturn(true).thenReturn(false);
        AbstractButton button = mock(AbstractButton.class);
        Mockito.when(button.getModel()).thenReturn(buttonModel);
        mockItemEvent = mock(ItemEvent.class);
        Mockito.when(mockItemEvent.getSource()).thenReturn(button);
    }

    @After
    public void tearDown() {
        emulator.dispose();
    }

    @Test
    public void testCPUInTraceModeWhenItemListenerTriggered() {
        traceMenuItemListener.itemStateChanged(mockItemEvent);
        assertTrue(emulator.inTraceMode);
    }

    @Test
    public void testCPUNotInTraceModeWhenItemListenerTriggeredTwice() {
        traceMenuItemListener.itemStateChanged(mockItemEvent);
        traceMenuItemListener.itemStateChanged(mockItemEvent);
        assertFalse(emulator.inTraceMode);
    }
}
