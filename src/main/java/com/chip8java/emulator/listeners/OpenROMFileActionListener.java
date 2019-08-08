/*
 * Copyright (C) 2013-2019 Craig Thomas
 * This project uses an MIT style license - see LICENSE for details.
 */
package com.chip8java.emulator.listeners;

import com.chip8java.emulator.common.IO;
import com.chip8java.emulator.components.CentralProcessingUnit;
import com.chip8java.emulator.components.Emulator;
import com.chip8java.emulator.components.Memory;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.InputStream;

/**
 * An ActionListener that will quit the emulator.
 */
public class OpenROMFileActionListener implements ActionListener
{
    private Emulator emulator;

    public OpenROMFileActionListener(Emulator emulator) {
        super();
        this.emulator = emulator;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        openROMFileDialog();
    }

    public void openROMFileDialog() {
        CentralProcessingUnit cpu = emulator.getCPU();
        Memory memory = emulator.getMemory();
        JFrame container = emulator.getEmulatorFrame();
        JFileChooser fileChooser = createFileChooser();
        if (fileChooser.showOpenDialog(container) == JFileChooser.APPROVE_OPTION) {
            InputStream inputStream = IO.openInputStream(fileChooser.getSelectedFile().toString());
            if (!memory.loadStreamIntoMemory(inputStream, CentralProcessingUnit.PROGRAM_COUNTER_START)) {
                JOptionPane.showMessageDialog(container, "Error reading file.", "File Read Problem",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            cpu.reset();
        }
    }

    public JFileChooser createFileChooser() {
        JFileChooser fileChooser = new JFileChooser();
        FileFilter filter1 = new FileNameExtensionFilter("CHIP8 Rom File (*.ch8)", "ch8");
        FileFilter filter2 = new FileNameExtensionFilter("Generic Rom File (*.rom)", "rom");
        fileChooser.setCurrentDirectory(new File("."));
        fileChooser.setDialogTitle("Open ROM file");
        fileChooser.setAcceptAllFileFilterUsed(true);
        fileChooser.setFileFilter(filter1);
        fileChooser.setFileFilter(filter2);
        return fileChooser;
    }
}
