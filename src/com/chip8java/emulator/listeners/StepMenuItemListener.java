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
 * The StepMenuItemListener will trace execution on the CPU when it is selected.
 */
public class StepMenuItemListener implements ItemListener {

    // The CPU that the ItemListener will update when clicked
    private CentralProcessingUnit mCPU;
    private JCheckBoxMenuItem mTraceMenuItem;

    public StepMenuItemListener(CentralProcessingUnit cpu, JCheckBoxMenuItem traceMenuItem) {
        super();
        mCPU = cpu;
        mTraceMenuItem = traceMenuItem;
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        AbstractButton button = (AbstractButton)e.getSource();
        if (!button.getModel().isSelected()) {
            mCPU.setStep(false);
        } else {
            mCPU.setStep(true);
            mTraceMenuItem.setSelected(true);
        }
        mCPU.getScreen().updateScreen();
    }
}
