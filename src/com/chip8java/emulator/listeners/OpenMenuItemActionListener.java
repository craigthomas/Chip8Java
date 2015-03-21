/*
 * Copyright (C) 2013-2015 Craig Thomas
 * This project uses an MIT style license - see LICENSE for details.
 */
package com.chip8java.emulator.listeners;

import com.chip8java.emulator.Emulator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * An ActionListener that will quit the emulator.
 */
public class OpenMenuItemActionListener implements ActionListener {

    private Emulator mEmulator;

    public OpenMenuItemActionListener(Emulator emulator) {
        super();
        mEmulator = emulator;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        mEmulator.loadFile();
    }
}
