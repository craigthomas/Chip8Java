/*
 * Copyright (C) 2013-2019 Craig Thomas
 * This project uses an MIT style license - see LICENSE for details.
 */
package com.chip8java.emulator.listeners;

import com.chip8java.emulator.components.CentralProcessingUnit;
import com.chip8java.emulator.components.Emulator;
import com.chip8java.emulator.components.Memory;
import org.junit.Before;
import org.junit.Test;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;

import static org.mockito.Mockito.*;

public class OpenROMFileActionListenerTest
{
    private OpenROMFileActionListener listenerSpy;
    private ActionEvent mockItemEvent;
    private JFileChooser fileChooser;

    @Before
    public void setUp() {
        Emulator emulator = mock(Emulator.class);
        Memory memory = mock(Memory.class);
        CentralProcessingUnit cpu = mock(CentralProcessingUnit.class);
        when(memory.loadStreamIntoMemory(anyObject(), anyInt())).thenReturn(true);

        fileChooser = mock(JFileChooser.class);
        when(fileChooser.getSelectedFile()).thenReturn(new File("test.chip8"));

        OpenROMFileActionListener listener = new OpenROMFileActionListener(emulator);
        listenerSpy = spy(listener);
        ButtonModel buttonModel = mock(ButtonModel.class);
        when(buttonModel.isSelected()).thenReturn(true);
        AbstractButton button = mock(AbstractButton.class);
        when(button.getModel()).thenReturn(buttonModel);
        mockItemEvent = mock(ActionEvent.class);
        when(mockItemEvent.getSource()).thenReturn(button);
        when(listenerSpy.createFileChooser()).thenReturn(fileChooser);
        when(emulator.getMemory()).thenReturn(memory);
        when(emulator.getCPU()).thenReturn(cpu);
    }

    @Test
    public void testOpenMenuItemActionListenerShowsWhenClicked() {
        listenerSpy.actionPerformed(mockItemEvent);
        listenerSpy.actionPerformed(mockItemEvent);
        verify(listenerSpy, times(2)).createFileChooser();
        verify(fileChooser, times(2)).showOpenDialog(any());
    }
}
