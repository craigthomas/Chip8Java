/*
 * Copyright (C) 2013-2015 Craig Thomas
 * This project uses an MIT style license - see LICENSE for details.
 */
package com.chip8java.emulator.listeners;

import com.chip8java.emulator.CentralProcessingUnit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * An ActionListener that will reset the specified CPU when triggered.
 */
public class ResetMenuItemActionListener implements ActionListener {

    // The CPU that the ActionListener will reset when triggered
    private CentralProcessingUnit mCPU;

    public ResetMenuItemActionListener(CentralProcessingUnit cpu) {
        super();
        mCPU = cpu;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        mCPU.reset();
    }
}
