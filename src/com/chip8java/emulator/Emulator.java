/*
 * Copyright (C) 2013-2015 Craig Thomas
 * This project uses an MIT style license - see LICENSE for details.
 */
package com.chip8java.emulator;

import com.chip8java.emulator.listeners.PauseMenuItemListener;
import com.chip8java.emulator.listeners.ResetMenuItemActionListener;
import com.chip8java.emulator.listeners.StepMenuItemListener;
import com.chip8java.emulator.listeners.TraceMenuItemListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.logging.Logger;

/**
 * The main Emulator class.
 */
public class Emulator {

    // The number of buffers to use for bit blitting
    private static final int DEFAULT_NUMBER_OF_BUFFERS = 2;
    // The default title for the emulator window
    private static final String DEFAULT_TITLE = "Yet Another Chip8 Emulator";
    // The logger for the class
    private final static Logger LOGGER = Logger.getLogger(Runner.class.getName());
    // The font file for the Chip 8
    private static final String FONT_FILE = "src/resources/FONTS.chip8";

    private CentralProcessingUnit mCPU;
    private Screen mScreen;
    private Keyboard mKeyboard;
    private Memory mMemory;

    private boolean mTraceOverlay;

    private static Canvas mCanvas;


    public static class Builder {
        private boolean mTrace;
        private boolean mStep;
        private int mScale;
        private String mRom;

        public Builder() {
            mTrace = false;
            mStep = false;
            mScale = Runner.SCALE_DEFAULT;
            mRom = null;
        }

        public Builder setTrace() {
            mTrace = true;
            return this;
        }

        public Builder setStep() {
            mStep = true;
            return this;
        }

        public Builder setScale(int scale) {
            mScale = scale;
            return this;
        }

        public Builder setRom(String rom) {
            mRom = rom;
            return this;
        }

        public Emulator build() {
            return new Emulator(this);
        }
    }

    private Emulator (Builder builder) {
        mKeyboard = new Keyboard();
        mMemory = new Memory(Memory.MEMORY_4K);

        if (!mMemory.loadRomIntoMemory(FONT_FILE, 0)) {
            LOGGER.severe("Could not load font file [" + FONT_FILE + "]");
            System.exit(1);
        }

        try {
            mScreen = new Screen(builder.mScale);
        } catch (Exception e) {
            LOGGER.severe("Could not initialize screen");
            LOGGER.severe(e.getMessage());
            System.exit(1);
        }
        mCPU = new CentralProcessingUnit(mMemory, mKeyboard, mScreen);

        if (!mMemory.loadRomIntoMemory(builder.mRom,
                CentralProcessingUnit.PROGRAM_COUNTER_START)) {
            LOGGER.severe("Could not load ROM file [" + builder.mRom + "]");
            return;
        }

        // Initialize the screen and keyboard listeners
        JFrame jFrame = initEmulatorJFrame(mScreen, mCPU);
        mCanvas.addKeyListener(mKeyboard);
    }

    public void start() {
        mCPU.start();

        java.util.Timer timer = new java.util.Timer();
        TimerTask task = new TimerTask() {
            public void run() {
                paint(mScreen);
            }
        };

        timer.scheduleAtFixedRate(task, 0l, 33l);
    }

    /**
     * Initializes the JFrame that the emulator will use to draw onto. Will set up the menu system and
     * link the action listeners to the menu items. Returns the JFrame that contains all of the emulator
     * screen elements.
     *
     * @param screen the Chip8 Screen to bind to the JFrame
     * @return the initialized JFrame
     */
    public static JFrame initEmulatorJFrame(Screen screen, CentralProcessingUnit cpu) {
        JFrame container = new JFrame(DEFAULT_TITLE);
        JMenuBar menuBar = new JMenuBar();

        // File menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        JMenuItem openFile = new JMenuItem("Open", KeyEvent.VK_O);
        fileMenu.add(openFile);

        JMenuItem closeFile = new JMenuItem("Close", KeyEvent.VK_C);
        fileMenu.add(closeFile);

        fileMenu.addSeparator();

        JMenuItem exitFile = new JMenuItem("Exit", KeyEvent.VK_X);
        fileMenu.add(exitFile);

        menuBar.add(fileMenu);

        // CPU menu
        JMenu debugMenu = new JMenu("CPU");
        debugMenu.setMnemonic(KeyEvent.VK_C);

        // Reset CPU menu item
        JMenuItem resetCPU = new JMenuItem("Reset", KeyEvent.VK_R);
        resetCPU.addActionListener(new ResetMenuItemActionListener(cpu));
        debugMenu.add(resetCPU);

        // Pause menu item
        JCheckBoxMenuItem pauseCPU = new JCheckBoxMenuItem("Pause");
        pauseCPU.setMnemonic(KeyEvent.VK_P);
        pauseCPU.addItemListener(new PauseMenuItemListener(cpu));
        pauseCPU.setSelected(cpu.getPaused());
        debugMenu.add(pauseCPU);

        debugMenu.addSeparator();

        // Trace menu item
        JCheckBoxMenuItem traceCPU = new JCheckBoxMenuItem("Trace Mode");
        traceCPU.setMnemonic(KeyEvent.VK_T);
        traceCPU.addItemListener(new TraceMenuItemListener(cpu));
        debugMenu.add(traceCPU);

        // Step menu item
        JCheckBoxMenuItem stepCPU = new JCheckBoxMenuItem("Step Mode");
        stepCPU.setMnemonic(KeyEvent.VK_S);
        stepCPU.addItemListener(new StepMenuItemListener(cpu, traceCPU));
        debugMenu.add(stepCPU);

        menuBar.add(debugMenu);

        int scaledWidth = screen.getWidth() * screen.getScale();
        int scaledHeight = screen.getHeight() * screen.getScale();

        JPanel panel = (JPanel) container.getContentPane();
        panel.setPreferredSize(new Dimension(scaledWidth, scaledHeight));
        panel.setLayout(null);

        mCanvas = new Canvas();
        mCanvas.setBounds(0, 0, scaledWidth, scaledHeight);
        mCanvas.setIgnoreRepaint(true);

        panel.add(mCanvas);

        container.setJMenuBar(menuBar);
        container.pack();
        container.setResizable(false);
        container.setVisible(true);

        mCanvas.createBufferStrategy(DEFAULT_NUMBER_OF_BUFFERS);

        return container;
    }

    public static void paint(Screen screen) {
        Graphics2D graphics = (Graphics2D) mCanvas.getBufferStrategy()
                .getDrawGraphics();
        graphics.drawImage(screen.getBuffer(), null, 0, 0);
//        if (mWriteOverlay) {
//            Composite composite = AlphaComposite.getInstance(
//                    AlphaComposite.SRC_OVER, 0.7f);
//            graphics.setComposite(composite);
//            graphics.drawImage(overlay, null, 5, (height * scaleFactor) - 57);
//        }
        graphics.dispose();
        mCanvas.getBufferStrategy().show();
    }
}
