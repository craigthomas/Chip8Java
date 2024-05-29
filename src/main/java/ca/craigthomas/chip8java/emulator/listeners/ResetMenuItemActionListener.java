/*
 * Copyright (C) 2013-2018 Craig Thomas
 * This project uses an MIT style license - see LICENSE for details.
 */
package ca.craigthomas.chip8java.emulator.listeners;

import ca.craigthomas.chip8java.emulator.components.CentralProcessingUnit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * An ActionListener that will reset the specified CPU when triggered.
 */
public class ResetMenuItemActionListener implements ActionListener
{
    // The CPU that the ActionListener will reset when triggered
    private CentralProcessingUnit cpu;

    public ResetMenuItemActionListener(CentralProcessingUnit cpu) {
        super();
        this.cpu = cpu;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        cpu.reset();
    }
}
