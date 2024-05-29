/*
 * Copyright (C) 2013-2019 Craig Thomas
 * This project uses an MIT style license - see LICENSE for details.
 */
package ca.craigthomas.chip8java.emulator.listeners;

import ca.craigthomas.chip8java.emulator.components.Emulator;
import org.junit.Before;
import org.junit.Test;

import javax.swing.*;
import java.awt.event.ActionEvent;

import static org.mockito.Mockito.*;

public class QuitActionListenerTest
{
    private QuitActionListener listenerSpy;
    private ActionEvent mockItemEvent;
    private Emulator emulator;

    @Before
    public void setUp() {
        emulator = mock(Emulator.class);

        QuitActionListener listener = new QuitActionListener(emulator);
        listenerSpy = spy(listener);
        ButtonModel buttonModel = mock(ButtonModel.class);
        when(buttonModel.isSelected()).thenReturn(true);
        AbstractButton button = mock(AbstractButton.class);
        when(button.getModel()).thenReturn(buttonModel);
        mockItemEvent = mock(ActionEvent.class);
        when(mockItemEvent.getSource()).thenReturn(button);
    }

    @Test
    public void testQuitActionListenerShowsWhenClicked() {
        listenerSpy.actionPerformed(mockItemEvent);
        verify(emulator, times(1)).kill();
    }
}
