/*
 * Copyright (C) 2013-2018 Craig Thomas
 * This project uses an MIT style license - see LICENSE for details.
 */
package ca.craigthomas.chip8java.emulator.listeners;

import ca.craigthomas.chip8java.emulator.components.Emulator;
import jdk.nashorn.internal.ir.annotations.Ignore;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.swing.*;
import java.awt.event.ItemEvent;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

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
        emulator = mock(Emulator.class);
        traceMenuItemListener = new TraceMenuItemListener(emulator);
        ButtonModel buttonModel = mock(ButtonModel.class);
        Mockito.when(buttonModel.isSelected()).thenReturn(true).thenReturn(false);
        AbstractButton button = mock(AbstractButton.class);
        Mockito.when(button.getModel()).thenReturn(buttonModel);
        mockItemEvent = mock(ItemEvent.class);
        Mockito.when(mockItemEvent.getSource()).thenReturn(button);
    }

    @Test
    @Ignore
    public void testCPUInTraceModeWhenItemListenerTriggered() {
        traceMenuItemListener.itemStateChanged(mockItemEvent);
        Mockito.verify(emulator, times(1)).setTrace(true);
    }

    @Test
    @Ignore
    public void testCPUNotInTraceModeWhenItemListenerTriggeredTwice() {
        traceMenuItemListener.itemStateChanged(mockItemEvent);
        traceMenuItemListener.itemStateChanged(mockItemEvent);
        Mockito.verify(emulator, times(1)).setTrace(true);
        Mockito.verify(emulator, times(1)).setTrace(false);
    }
}
