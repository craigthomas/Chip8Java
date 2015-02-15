/*
 * Copyright (C) 2013-2015 Craig Thomas
 * This project uses an MIT style license - see LICENSE for details.
 */
package com.chip8java.emulator.listeners;

import com.chip8java.emulator.CentralProcessingUnit;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * The PauseMenuActionListener will trace execution on the CPU when it is selected.
 */
public class TraceMenuItemListener implements ItemListener {

    // The CPU that the ItemListener will update when clicked
    private CentralProcessingUnit mCPU;

    public TraceMenuItemListener(CentralProcessingUnit cpu) {
        super();
        mCPU = cpu;
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        AbstractButton button = (AbstractButton)e.getSource();
        if (!button.getModel().isSelected()) {
            mCPU.setTrace(false);
        } else {
            mCPU.setTrace(true);
        }
        mCPU.getScreen().updateScreen();
    }
}
