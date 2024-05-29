/*
 * Copyright (C) 2013-2018 Craig Thomas
 * This project uses an MIT style license - see LICENSE for details.
 */
package ca.craigthomas.chip8java.emulator.listeners;

import ca.craigthomas.chip8java.emulator.components.CentralProcessingUnit;
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
public class ResetMenuItemActionListenerTest
{
    private @Spy CentralProcessingUnit cpu;
    private ResetMenuItemActionListener resetMenuItemActionListener;
    private ActionEvent mockItemEvent;

    @Before
    public void setUp() {
        cpu = mock(CentralProcessingUnit.class);
        resetMenuItemActionListener = new ResetMenuItemActionListener(cpu);
        ButtonModel buttonModel = mock(ButtonModel.class);
        Mockito.when(buttonModel.isSelected()).thenReturn(true).thenReturn(false);
        AbstractButton button = mock(AbstractButton.class);
        Mockito.when(button.getModel()).thenReturn(buttonModel);
        mockItemEvent = mock(ActionEvent.class);
        Mockito.when(mockItemEvent.getSource()).thenReturn(button);
    }

    @Test
    public void testCPUResetWhenItemActionListenerTriggered() {
        resetMenuItemActionListener.actionPerformed(mockItemEvent);
        verify(cpu, times(1)).reset();
    }
}
