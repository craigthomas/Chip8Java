/*
 * Copyright (C) 2013-2018 Craig Thomas
 * This project uses an MIT style license - see LICENSE for details.
 */
package com.chip8java.emulator.listeners;

import com.chip8java.emulator.components.Emulator;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * The TraceMenuItemListener will trace execution on the CPU when it is selected.
 */
public class TraceMenuItemListener implements ItemListener
{
    // The Emulator that the ItemListener will update when clicked
    private Emulator emulator;

    public TraceMenuItemListener(Emulator emulator) {
        super();
        this.emulator = emulator;
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        AbstractButton button = (AbstractButton)e.getSource();
        if (!button.getModel().isSelected()) {
            emulator.setTrace(false);
        } else {
            emulator.setTrace(true);
        }
    }
}
