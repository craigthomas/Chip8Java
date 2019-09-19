/*
 * Copyright (C) 2013-2019 Craig Thomas
 * This project uses an MIT style license - see LICENSE for details.
 */
package com.chip8java.emulator.components;

import com.chip8java.emulator.common.IO;
import com.chip8java.emulator.listeners.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.*;
import java.util.Timer;
import java.util.logging.Logger;

/**
 * The main Emulator class.
 */
public class Emulator
{
    // The number of buffers to use for bit blitting
    private static final int DEFAULT_NUMBER_OF_BUFFERS = 2;

    // The default title for the emulator window
    private static final String DEFAULT_TITLE = "Yet Another Super Chip 8 Emulator";

    // The logger for the class
    private final static Logger LOGGER = Logger.getLogger(Emulator.class.getName());

    // The font file for the Chip 8
    private static final String FONT_FILE = "FONTS.chip8";

    // The Chip8 components
    private CentralProcessingUnit cpu;
    private Screen screen;
    private Keyboard keyboard;
    private Memory memory;

    // The overlay screen background color
    private final Color overlayBackColor = new Color(0.0f, 0.27f, 0.0f, 1.0f);

    // The overlay screen border color
    private final Color overlayBorderColor = new Color(0.0f, 0.70f, 0.0f, 1.0f);

    // The font file to use for the overlay screen
    private static final String DEFAULT_FONT = "VeraMono.ttf";

    // Whether the Emulator is in trace mode
    public volatile boolean inTraceMode;

    // Whether the Emulator is in step mode
    public volatile boolean inStepMode;

    // Emulator window and frame elements
    private JCheckBoxMenuItem traceMenuItem;
    private JCheckBoxMenuItem mStepMenuItem;
    private JMenuBar menuBar;
    private Canvas canvas;
    private JFrame container;
    private BufferedImage overlayScreen;
    private Font overlayFont;

    // The current state of the emulator and associated tasks
    private volatile EmulatorState state;
    private int cpuCycleTime;
    private Timer timer;
    private TimerTask timerTask;
    private volatile boolean doSingleStep;

    /**
     * Convenience constructor that sets the emulator running with a 1x
     * screen scale, a cycle time of 0, a null rom, and trace mode off.
     */
    public Emulator() {
        this(1, 0, null, false, false);
    }

    /**
     * Initializes an Emulator based on the parameters passed.
     *
     * @param scale the screen scaling to apply to the emulator window
     * @param cycleTime the cycle time delay for the emulator
     * @param rom the rom filename to load
     * @param traceMode whether to enable trace mode
     */
    public Emulator(int scale, int cycleTime, String rom, boolean traceMode, boolean stepMode) {
        keyboard = new Keyboard(this);
        memory = new Memory(Memory.MEMORY_4K);
        screen = new Screen(scale);
        cpu = new CentralProcessingUnit(memory, keyboard, screen);
        doSingleStep = false;

        // Load the font file into memory
        InputStream fontFileStream = IO.openInputStreamFromResource(FONT_FILE);
        if (!memory.loadStreamIntoMemory(fontFileStream, 0)) {
            LOGGER.severe("Could not load font file");
            kill();
        }
        IO.closeStream(fontFileStream);

        // Attempt to load specified ROM file
        setPaused();
        if (rom != null) {
            InputStream romFileStream = IO.openInputStream(rom);
            if (!memory.loadStreamIntoMemory(romFileStream,
                    CentralProcessingUnit.PROGRAM_COUNTER_START)) {
                LOGGER.severe("Could not load ROM file [" + rom + "]");
            } else {
                setRunning();
            }
            IO.closeStream(romFileStream);
        }

        // Initialize the screen, keyboard listeners, and overlayScreen information
        initEmulatorJFrame();
        initializeOverlay();
        setTrace(traceMode || stepMode);
        setStep(stepMode);
        cpuCycleTime = cycleTime;
        start();
    }

    /**
     * Starts the main emulator loop running. Fires at the rate of 60Hz,
     * will repaint the screen and listen for any debug key presses.
     */
    public void start() {
        timer = new Timer();
        timerTask = new TimerTask() {
            public void run() {
                refreshScreen();
                interpretDebugKey();
            }
        };
        timer.scheduleAtFixedRate(timerTask, 0L, 33L);

        while (state != EmulatorState.KILLED) {
            if (state != EmulatorState.PAUSED) {
                if (!inStepMode || doSingleStep) {
                    cpu.fetchIncrementExecute();
                    try {
                        Thread.sleep(cpuCycleTime);
                    } catch (InterruptedException e) {
                        LOGGER.warning("CPU sleep interrupted");
                    }
                    doSingleStep = false;
                } else {
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        LOGGER.warning("Pause interrupted");
                    }
                }
            }
        }
        kill();
        System.exit(0);
    }

    /**
     * Returns the main frame for the emulator.
     *
     * @return the JFrame containing the emulator
     */
    public JFrame getEmulatorFrame() {
        return container;
    }

    public CentralProcessingUnit getCPU() {
        return this.cpu;
    }

    public Memory getMemory() {
        return memory;
    }

    /**
     * Initializes the overlay buffer.
     */
    private void initializeOverlay() {
        try {
            InputStream fontFile = IO.openInputStreamFromResource(DEFAULT_FONT);
            overlayFont = Font.createFont(Font.TRUETYPE_FONT, fontFile);
            overlayFont = overlayFont.deriveFont(11F);
            overlayScreen = new BufferedImage(342, 53, BufferedImage.TYPE_4BYTE_ABGR);
            fontFile.close();
        } catch (Exception e) {
            LOGGER.severe("Could not initialize overlayScreen");
            LOGGER.severe(e.getLocalizedMessage());
            kill();
        }
    }

    /**
     * Initializes the JFrame that the emulator will use to draw onto. Will set up the menu system and
     * link the action listeners to the menu items. Returns the JFrame that contains all of the emulator
     * screen elements.
     */
    private void initEmulatorJFrame() {
        container = new JFrame(DEFAULT_TITLE);
        menuBar = new JMenuBar();

        // File menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        JMenuItem openFile = new JMenuItem("Open", KeyEvent.VK_O);
        openFile.addActionListener(new OpenROMFileActionListener(this));
        fileMenu.add(openFile);
        fileMenu.addSeparator();

        JMenuItem quitFile = new JMenuItem("Quit", KeyEvent.VK_Q);
        quitFile.addActionListener(new QuitActionListener(this));
        fileMenu.add(quitFile);
        menuBar.add(fileMenu);

        // CPU menu
        JMenu debugMenu = new JMenu("CPU");
        debugMenu.setMnemonic(KeyEvent.VK_C);

        // Reset CPU menu item
        JMenuItem resetCPU = new JMenuItem("Reset", KeyEvent.VK_R);
        resetCPU.addActionListener(new ResetMenuItemActionListener(cpu));
        debugMenu.add(resetCPU);
        debugMenu.addSeparator();

        // Trace menu item
        traceMenuItem = new JCheckBoxMenuItem("Trace Mode");
        traceMenuItem.setMnemonic(KeyEvent.VK_T);
        traceMenuItem.addItemListener(new TraceMenuItemListener(this));
        debugMenu.add(traceMenuItem);

        // Step menu item
        mStepMenuItem = new JCheckBoxMenuItem("Step Mode");
        mStepMenuItem.setMnemonic(KeyEvent.VK_S);
        mStepMenuItem.addItemListener(new StepMenuItemListener(this));
        debugMenu.add(mStepMenuItem);
        menuBar.add(debugMenu);

        attachCanvas();
    }

    /**
     * Generates the canvas of the appropriate size and attaches it to the
     * main jFrame for the emulator.
     */
    private void attachCanvas() {
        int scaleFactor = screen.getScale();
        int scaledWidth = screen.getWidth() * scaleFactor;
        int scaledHeight = screen.getHeight() * scaleFactor;

        JPanel panel = (JPanel) container.getContentPane();
        panel.removeAll();
        panel.setPreferredSize(new Dimension(scaledWidth, scaledHeight));
        panel.setLayout(null);

        canvas = new Canvas();
        canvas.setBounds(0, 0, scaledWidth, scaledHeight);
        canvas.setIgnoreRepaint(true);

        panel.add(canvas);

        container.setJMenuBar(menuBar);
        container.pack();
        container.setResizable(false);
        container.setVisible(true);
        container.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        canvas.createBufferStrategy(DEFAULT_NUMBER_OF_BUFFERS);
        canvas.setFocusable(true);
        canvas.requestFocus();

        canvas.addKeyListener(keyboard);
    }

    /**
     * Will redraw the contents of the screen to the emulator window. Optionally, if
     * isInTraceMode is True, will also draw the contents of the overlayScreen to the screen.
     */
    private void refreshScreen() {
        if (screen.getStateChanged()) {
            attachCanvas();
            screen.clearStateChanged();
            screen.clearScreen();
        }

        Graphics2D graphics = (Graphics2D) canvas.getBufferStrategy().getDrawGraphics();
        graphics.drawImage(screen.getBuffer(), null, 0, 0);

        if (inTraceMode) {
            updateOverlayInformation();
            Composite composite = AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, 0.7f);
            graphics.setComposite(composite);
            graphics.drawImage(overlayScreen, null, 5, (screen.getHeight() * screen.getScale()) - 57);
        }

        graphics.dispose();
        canvas.getBufferStrategy().show();
    }

    /**
     * Write the current status of the CPU to the overlayScreen window.
     */
    private void updateOverlayInformation() {
        Graphics2D graphics = overlayScreen.createGraphics();

        graphics.setColor(overlayBorderColor);
        graphics.fillRect(0, 0, 342, 53);

        graphics.setColor(overlayBackColor);
        graphics.fillRect(1, 1, 340, 51);

        graphics.setColor(Color.white);
        graphics.setFont(overlayFont);

        String line1 = cpu.cpuStatusLine1();
        String line2 = cpu.cpuStatusLine2();
        String line3 = cpu.cpuStatusLine3();

        graphics.drawString(line1, 5, 16);
        graphics.drawString(line2, 5, 31);
        graphics.drawString(line3, 5, 46);
        graphics.dispose();
    }

    /**
     * Sets whether or not the overlayScreen information for the CPU should be turned
     * off or on. If set to true, writes CPU information.
     *
     * @param trace Whether or not to print CPU information
     */
    public void setTrace(boolean trace) {
        inTraceMode = trace;
        traceMenuItem.setState(trace);
    }

    /**
     * Sets or clears step mode.
     *
     * @param step whether step mode should be enabled
     */
    public void setStep(boolean step) {
        inStepMode = step;
        mStepMenuItem.setState(step);
        if (step) {
            setTrace(true);
        }
    }

    /**
     * Will check to see if a debugging key was pressed. Will return true if
     * one was pressed. Will also set the correct trace and step flags
     * depending on what debug key was pressed.
     */
    private void interpretDebugKey() {
        int key = keyboard.getDebugKey();
        switch (key) {
            case Keyboard.CHIP8_NORMAL:
                setTrace(false);
                setStep(false);
                break;

            case Keyboard.CHIP8_STEP:
                setStep(!inStepMode);
                break;

            case Keyboard.CHIP8_TRACE:
                setTrace(!inTraceMode);
                break;

            case Keyboard.CHIP8_NEXT:
                doSingleStep = true;
                break;

            default:
                break;
        }
    }

    /**
     * Kills the CPU, any emulator based timers, and disposes the main
     * emulator JFrame before calling System.exit.
     */
    public void kill() {
        cpu.kill();
        timer.cancel();
        timer.purge();
        timerTask.cancel();
        dispose();
        state = EmulatorState.KILLED;
    }

    /**
     * Disposes of the main emulator JFrame.
     */
    public void dispose() {
        container.dispose();
    }

    /**
     * Sets the emulator running.
     */
    public void setRunning() {
        state = EmulatorState.RUNNING;
    }

    /**
     * Pauses the emulator.
     */
    public void setPaused() {
        state = EmulatorState.PAUSED;
    }
}
