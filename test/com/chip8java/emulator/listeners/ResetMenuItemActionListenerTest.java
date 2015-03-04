/*
 * Copyright (C) 2013-2015 Craig Thomas
 * This project uses an MIT style license - see LICENSE for details.
 */
package com.chip8java.emulator.listeners;

import com.chip8java.emulator.CentralProcessingUnit;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.Spy;

import javax.swing.*;
import java.awt.event.ActionEvent;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Tests for the PauseMenuItemListener.
 */
public class ResetMenuItemActionListenerTest {

    private @Spy CentralProcessingUnit mCPU;
    private ResetMenuItemActionListener mResetMenuItemActionListener;
    private ActionEvent mMockItemEvent;

    @Before
    public void setUp() {
        mCPU = mock(CentralProcessingUnit.class);
        mResetMenuItemActionListener = new ResetMenuItemActionListener(mCPU);
        ButtonModel buttonModel = mock(ButtonModel.class);
        Mockito.when(buttonModel.isSelected()).thenReturn(true).thenReturn(false);
        AbstractButton button = mock(AbstractButton.class);
        Mockito.when(button.getModel()).thenReturn(buttonModel);
        mMockItemEvent = mock(ActionEvent.class);
        Mockito.when(mMockItemEvent.getSource()).thenReturn(button);
    }

    @Test
    public void testCPUResetWhenItemActionListenerTriggered() {
        mResetMenuItemActionListener.actionPerformed(mMockItemEvent);
        verify(mCPU, times(1)).reset();
    }
}
