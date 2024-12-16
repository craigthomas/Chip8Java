/*
 * Copyright (C) 2013-2024 Craig Thomas
 * This project uses an MIT style license - see LICENSE for details.
 */
package ca.craigthomas.chip8java.emulator.components;

import ca.craigthomas.chip8java.emulator.common.IO;
import ca.craigthomas.chip8java.emulator.listeners.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
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

    // Emulator window and frame elements
    private JMenuBar menuBar;
    private Canvas canvas;
    private JFrame container;

    // The current state of the emulator and associated tasks
    private volatile EmulatorState state;
    private int cpuCycleTime;
    private Timer timer;
    private TimerTask timerTask;

    /**
     * Convenience constructor that sets the emulator running with a 1x
     * screen scale, a cycle time of 0, a null rom, and trace mode off.
     */
    public Emulator() {
        this(1, 0, null, false, "#000000", "#666666", "#BBBBBB", "#FFFFFF", false, false);
    }

    /**
     * Initializes an Emulator based on the parameters passed.
     *
     * @param scale the screen scaling to apply to the emulator window
     * @param cycleTime the cycle time delay for the emulator
     * @param rom the rom filename to load
     * @param memSize4k whether to set memory size to 4k
     * @param color0 the bitplane 0 color
     * @param color1 the bitplane 1 color
     * @param color2 the bitplane 2 color
     * @param color3 the bitplane 3 color
     * @param shiftQuirks whether to enable shift quirks or not
     * @param logicQuirks whether to enable logic quirks or not
     */
    public Emulator(
            int scale,
            int cycleTime,
            String rom,
            boolean memSize4k,
            String color0,
            String color1,
            String color2,
            String color3,
            boolean shiftQuirks,
            boolean logicQuirks
    ) {
        if (color0.length() != 6) {
            System.out.println("color_0 parameter must be 6 characters long");
            System.exit(1);
        }

        if (color1.length() != 6) {
            System.out.println("color_1 parameter must be 6 characters long");
            System.exit(1);
        }

        if (color2.length() != 6) {
            System.out.println("color_2 parameter must be 6 characters long");
            System.exit(1);
        }

        if (color3.length() != 6) {
            System.out.println("color_3 parameter must be 6 characters long");
            System.exit(1);
        }

        Color converted_color0 = null;
        try {
            converted_color0 = Color.decode("#" + color0);
        } catch (NumberFormatException e) {
            System.out.println("color_0 parameter could not be decoded (" + e.getMessage() +")");
            System.exit(1);
        }

        Color converted_color1 = null;
        try {
            converted_color1 = Color.decode("#" + color1);
        } catch (NumberFormatException e) {
            System.out.println("color_1 parameter could not be decoded (" + e.getMessage() +")");
            System.exit(1);
        }

        Color converted_color2 = null;
        try {
            converted_color2 = Color.decode("#" + color2);
        } catch (NumberFormatException e) {
            System.out.println("color_2 parameter could not be decoded (" + e.getMessage() +")");
            System.exit(1);
        }

        Color converted_color3 = null;
        try {
            converted_color3 = Color.decode("#" + color3);
        } catch (NumberFormatException e) {
            System.out.println("color_3 parameter could not be decoded (" + e.getMessage() +")");
            System.exit(1);
        }

        cpuCycleTime = cycleTime;
        keyboard = new Keyboard(this);
        memory = new Memory(memSize4k);
        screen = new Screen(scale, converted_color0, converted_color1, converted_color2, converted_color3);
        cpu = new CentralProcessingUnit(memory, keyboard, screen);
        cpu.setShiftQuirks(shiftQuirks);
        cpu.setLogicQuirks(logicQuirks);

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

        // Initialize the screen
        initEmulatorJFrame();
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
            }
        };
        timer.scheduleAtFixedRate(timerTask, 0L, 33L);

        while (state != EmulatorState.KILLED) {
            if (state != EmulatorState.PAUSED) {
                if (!cpu.isAwaitingKeypress()) {
                    cpu.fetchIncrementExecute();
                    try {
                        Thread.sleep(cpuCycleTime);
                    } catch (InterruptedException e) {
                        LOGGER.warning("CPU sleep interrupted");
                    }
                } else {
                    cpu.decodeKeypressAndContinue();
                }
            }

            if (keyboard.getRawKeyPressed() == Keyboard.CHIP8_QUIT) {
                break;
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
        JMenu cpuMenu = new JMenu("CPU");
        cpuMenu.setMnemonic(KeyEvent.VK_C);

        // Reset CPU menu item
        JMenuItem resetCPU = new JMenuItem("Reset", KeyEvent.VK_R);
        resetCPU.addActionListener(new ResetMenuItemActionListener(cpu));
        cpuMenu.add(resetCPU);
        cpuMenu.addSeparator();
        menuBar.add(cpuMenu);

        attachCanvas();
    }

    /**
     * Generates the canvas of the appropriate size and attaches it to the
     * main jFrame for the emulator.
     */
    private void attachCanvas() {
        int scaleFactor = screen.getScale();
        int scaledWidth = Screen.WIDTH * scaleFactor;
        int scaledHeight = Screen.HEIGHT * scaleFactor;

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
        Graphics2D graphics = (Graphics2D) canvas.getBufferStrategy().getDrawGraphics();
        graphics.drawImage(screen.getBuffer(), null, 0, 0);
        graphics.dispose();
        canvas.getBufferStrategy().show();
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
