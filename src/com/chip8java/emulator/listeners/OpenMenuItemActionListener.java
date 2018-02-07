/*
 * Copyright (C) 2013-2018 Craig Thomas
 * This project uses an MIT style license - see LICENSE for details.
 */
package com.chip8java.emulator.listeners;

import com.chip8java.emulator.components.Emulator;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * An ActionListener that will quit the emulator.
 */
public class OpenMenuItemActionListener implements ActionListener
{
    private Emulator emulator;

    public OpenMenuItemActionListener(Emulator emulator) {
        super();
        this.emulator = emulator;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        emulator.loadFile();
    }
}
