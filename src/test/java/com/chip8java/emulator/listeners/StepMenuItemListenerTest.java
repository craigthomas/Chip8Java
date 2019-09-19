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
import static org.mockito.Mockito.*;

/**
 * Tests for the StepMenuItemListener.
 */
public class StepMenuItemListenerTest
{
    private Emulator emulator;
    private StepMenuItemListener traceMenuItemListener;
    private ItemEvent mockItemEvent;

    @Before
    public void setUp() {
        emulator = mock(Emulator.class);
        traceMenuItemListener = new StepMenuItemListener(emulator);
        ButtonModel buttonModel = mock(ButtonModel.class);
        Mockito.when(buttonModel.isSelected()).thenReturn(true).thenReturn(false);
        AbstractButton button = mock(AbstractButton.class);
        Mockito.when(button.getModel()).thenReturn(buttonModel);
        mockItemEvent = mock(ItemEvent.class);
        Mockito.when(mockItemEvent.getSource()).thenReturn(button);
    }

    @Test
    public void testCPUInStepModeWhenItemListenerTriggered() {
        traceMenuItemListener.itemStateChanged(mockItemEvent);
        verify(emulator, times(1)).setStep(true);
    }

    @Test
    public void testCPUNotInStepModeWhenItemListenerTriggeredTwice() {
        traceMenuItemListener.itemStateChanged(mockItemEvent);
        traceMenuItemListener.itemStateChanged(mockItemEvent);
        verify(emulator, times(1)).setStep(true);
        verify(emulator, times(1)).setStep(false);
    }

    @Test
    public void testCPUStaysInTraceModeWhenStepModeStopped() {
        traceMenuItemListener.itemStateChanged(mockItemEvent);
        traceMenuItemListener.itemStateChanged(mockItemEvent);
        verify(emulator, times(1)).setStep(true);
        verify(emulator, times(1)).setStep(false);
        verify(emulator, times(1)).setTrace(true);
    }
}
