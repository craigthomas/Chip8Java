/*
 * Copyright (C) 2013-2018 Craig Thomas
 * This project uses an MIT style license - see LICENSE for details.
 */
package ca.craigthomas.chip8java.emulator.listeners;

import ca.craigthomas.chip8java.emulator.components.Emulator;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * The StepMenuItemListener will trace execution on the CPU when it is selected.
 */
public class StepMenuItemListener implements ItemListener
{
    // The Emulator that the ItemListener will update when clicked
    private Emulator emulator;

    public StepMenuItemListener(Emulator emulator) {
        super();
        this.emulator = emulator;
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        AbstractButton button = (AbstractButton)e.getSource();
        if (!button.getModel().isSelected()) {
            emulator.setStep(false);
        } else {
            emulator.setStep(true);
            emulator.setTrace(true);
        }
    }
}
